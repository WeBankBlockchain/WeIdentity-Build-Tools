package com.webank.weid.service.v2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.controller.BuildToolController;
import com.webank.weid.exception.InitWeb3jException;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.service.CheckNodeFace;

public class CheckNodeServiceV2 implements CheckNodeFace {
    
    private static final Logger logger = LoggerFactory.getLogger(BuildToolController.class);
    
    private Channel2Connections channelConnections = new Channel2Connections();
    
    public boolean check(FiscoConfig fiscoConfig) {
        try {
            Service service = buildFiscoBcosService(fiscoConfig);
            Set<String> topics = new HashSet<String>();
            topics.add(fiscoConfig.getCurrentOrgId());
            service.setTopics(topics);
            try {
                service.run();
            } catch (Exception e) {
                logger.error("[check] the service run fail.", e);
                throw e;
            }
            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setChannelService(service);
            Web3j web3j = Web3j.build(channelEthereumService, service.getGroupId());
            if (web3j == null) {
                logger.error("[check] build the web3j fail.");
                throw new WeIdBaseException("init web3j fail.");
            }
            // 检查群组配置
            List<String> groupList = web3j.getGroupList().send().getGroupList();
            if (!groupList.contains(fiscoConfig.getGroupId())) {
                throw new WeIdBaseException("the groupId does not exist.");
            }
            Credentials  credentials = GenCredential.create();
            if (credentials == null) {
                logger.error("[check] create the Credentials fail.");
                throw new WeIdBaseException("the Credentials create fail.");
            }
            int number = 0;
            try {
                number = this.getBlockNumber(web3j);
                logger.info("[check] the current blockNumber is {}", number);
                return true;
            } catch (Exception e) {
                logger.error("[check] get the BlockNumber fail.", e);
                throw new WeIdBaseException("can not get the blockNumber.");
            } finally {
                channelConnections.stopWork();
            }
        } catch (WeIdBaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[check] check the node fail.", e);
            return false;
        }
    }
    
    private Service buildFiscoBcosService(FiscoConfig fiscoConfig) {

        Service service = new Service();
        service.setOrgID(fiscoConfig.getCurrentOrgId());
        service.setConnectSeconds(Integer.valueOf(fiscoConfig.getWeb3sdkTimeout()));
        // group info
        Integer groupId = Integer.valueOf(fiscoConfig.getGroupId());
        service.setGroupId(groupId);

        // connect key and string
        channelConnections.setGroupId(groupId);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        channelConnections
            .setCaCert(resolver.getResource("classpath:" + fiscoConfig.getV2CaCrtPath()));
        channelConnections
            .setSslCert(resolver.getResource("classpath:" + fiscoConfig.getV2NodeCrtPath()));
        channelConnections
            .setSslKey(resolver.getResource("classpath:" + fiscoConfig.getV2NodeKeyPath()));
        channelConnections.setConnectionsStr(Arrays.asList(fiscoConfig.getNodes().split(",")));
        GroupChannelConnectionsConfig connectionsConfig = new GroupChannelConnectionsConfig();
        connectionsConfig.setAllChannelConnections(Arrays.asList(channelConnections));
        service.setAllChannelConnections(connectionsConfig);
        // thread pool params
        service.setThreadPool(initializePool(fiscoConfig));
        return service;
    }
    
    protected ThreadPoolTaskExecutor initializePool(FiscoConfig fiscoConfig) {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setBeanName("web3sdk");
        pool.setCorePoolSize(Integer.valueOf(fiscoConfig.getWeb3sdkCorePoolSize()));
        pool.setMaxPoolSize(Integer.valueOf(fiscoConfig.getWeb3sdkMaxPoolSize()));
        pool.setQueueCapacity(Integer.valueOf(fiscoConfig.getWeb3sdkQueueSize()));
        pool.setKeepAliveSeconds(Integer.valueOf(fiscoConfig.getWeb3sdkKeepAliveSeconds()));
        pool.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
        pool.initialize();
        return pool;
    }
    
    public int getBlockNumber(Web3j web3j) throws Exception {
        BlockNumber response = web3j.getBlockNumber().sendAsync().get(5, TimeUnit.SECONDS);
        return response.getBlockNumber().intValue();
    }
}
