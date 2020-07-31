/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-build-tools.
 *
 *       weidentity-build-tools is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-build-tools is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-build-tools.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.webank.weid.service.v2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.controller.BuildToolController;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.base.HashContract;
import com.webank.weid.service.CheckNodeFace;
import com.webank.weid.util.WeIdUtils;

public class CheckNodeServiceV2 implements CheckNodeFace {
    
    private static final Logger logger = LoggerFactory.getLogger(BuildToolController.class);
    
    private Channel2Connections channelConnections = new Channel2Connections();
    
    public boolean check(FiscoConfig fiscoConfig) {
        try {
            Web3j web3j = getWeb3j(fiscoConfig);
            if (web3j == null) {
                logger.error("[check] build the web3j fail.");
                throw new WeIdBaseException("init web3j fail.");
            }
//            // 检查群组配置
//            List<String> groupList = web3j.getGroupList().send().getGroupList();
//            if (!groupList.contains(fiscoConfig.getGroupId())) {
//                throw new WeIdBaseException("the groupId does not exist.");
//            }
//            Credentials  credentials = GenCredential.create();
//            if (credentials == null) {
//                logger.error("[check] create the Credentials fail.");
//                throw new WeIdBaseException("the Credentials create fail.");
//            }
            try {
                web3j.getGroupList().send().getGroupList();
                logger.info("[check] check node successfully.");
                return true;
            } catch (Exception e) {
                logger.error("[check] check node fail.", e);
                throw new WeIdBaseException("can not connection the node.");
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
    
    
    private Web3j getWeb3j(FiscoConfig fiscoConfig) throws Exception {
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
            Web3j web3j = Web3j.build(channelEthereumService, Integer.parseInt(fiscoConfig.getGroupId()));
            if (web3j == null) {
                logger.error("[check] build the web3j fail.");
                throw new WeIdBaseException("init web3j fail.");
            }
            return web3j;
        } catch (Exception e) {
            throw e;
        }
    }
    
    // 根据orgId获取org_config里面的hash数据.
    private HashContract getHashFromOrgCns(FiscoConfig fiscoConfig) throws Exception {
        List<HashContract> allHash = getAllHash(fiscoConfig);
        for (HashContract hashContract : allHash) {
            if (hashContract.getHash().equals(fiscoConfig.getCurrentOrgId())) {
                return hashContract;
            }
        }
        return null;
    }
    
    private DataBucket getDataBucket(FiscoConfig fiscoConfig) throws Exception {
        Credentials credentials = GenCredential.create();
        Web3j web3j = getWeb3j(fiscoConfig);
        String contractAddress = getDataBucketAddress(web3j, credentials, CnsType.ORG_CONFING);
        if (StringUtils.isBlank(contractAddress)) {
            return null;
        }
        return DataBucket.load(
            contractAddress, 
            web3j, 
            credentials, 
            new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT)
        );
    }
    
    // 获取所有的hash
    private List<HashContract> getAllHash(FiscoConfig fiscoConfig) throws Exception {
        int startIndex = 0;
        BigInteger num = BigInteger.valueOf(10);
        List<HashContract>  hashContractList = new ArrayList<HashContract>();
        try {
            DataBucket dataBucket = getDataBucket(fiscoConfig);
            if (dataBucket == null) {
                return hashContractList;
            }
            while (true) {
                BigInteger offset = BigInteger.valueOf(startIndex);
                Tuple4<List<String>, List<String>, List<BigInteger>, BigInteger> data = 
                    dataBucket.getAllHash(offset, num).send();
                List<String> hashList = data.getValue1();
                List<String> ownerList = data.getValue2();
                List<BigInteger> timesList = data.getValue3();
                BigInteger next = data.getValue4();
                for (int i = 0; i < hashList.size(); i++) {
                    if (WeIdUtils.isEmptyStringAddress(ownerList.get(i))) {
                        break;
                    }
                    HashContract hash = new HashContract();
                    hash.setHash(hashList.get(i));
                    hash.setOwner(ownerList.get(i));
                    hash.setTime(timesList.get(i).longValue());
                    hashContractList.add(hash);
                }
                if (next.intValue() == 0) {
                    break;
                }
                startIndex = next.intValue();
            }
            logger.info("[getAllHash] get the all hash success.");
            return hashContractList;
        } catch (Exception e) {
            logger.error("[getAllHash] get the all hash fail.", e);
            throw e;
        } finally {
            channelConnections.stopWork();
        }
    }
    
    // 判断是否存在机构配置
    public boolean checkOrgId(FiscoConfig fiscoConfig) throws Exception {
        //从机构配置中获取当前机构的hash
        HashContract hashFromOrgIdCns = getHashFromOrgCns(fiscoConfig);
        // 如果不存在机构配置，则返回前端进行私钥配置
        if (hashFromOrgIdCns == null) {
            logger.info("[checkOrgId] the orgId does not exist in orgConfig cns.");
            return false;//不存在
        }
        logger.info("[checkOrgId] the orgId exist in orgConfig cns.");
        return true;
    }
    
    // 获取databucket地址
    private String getDataBucketAddress(Web3j web3j, Credentials credentials, CnsType cnsType) {
        CnsInfo queryCnsInfo = queryCnsInfo(web3j, credentials, cnsType);
        if (queryCnsInfo != null) {
            return queryCnsInfo.getAddress();
        }
        return StringUtils.EMPTY;
    }
    
    // 查询cns地址
    private CnsInfo queryCnsInfo(Web3j web3j, Credentials credentials, CnsType cnsType) throws WeIdBaseException {
        try {
            logger.info("[queryBucketFromCns] query address by type = {}.", cnsType.getName());
            CnsService cnsService = new CnsService(web3j, credentials);
            List<CnsInfo> cnsInfoList = cnsService.queryCnsByName(cnsType.getName());
            if (cnsInfoList != null) {
                // 获取当前cnsType的大版本前缀
                String cnsTypeVersion = cnsType.getVersion();
                String preV = cnsTypeVersion.substring(0, cnsTypeVersion.indexOf(".") + 1);
                //从后往前找到相应大版本的数据
                for (int i = cnsInfoList.size() - 1; i >= 0; i--) {
                    CnsInfo cnsInfo = cnsInfoList.get(i);
                    if (cnsInfo.getVersion().startsWith(preV)) {
                        logger.info("[queryBucketFromCns] query address form CNS successfully.");
                        return cnsInfo;
                    }
                }
            }
            logger.warn("[queryBucketFromCns] can not find data from CNS.");
            return null;
        } catch (Exception e) {
            logger.error("[queryBucketFromCns] query address has error.", e);
            throw new WeIdBaseException(ErrorCode.UNKNOW_ERROR);
        }
    }
}
