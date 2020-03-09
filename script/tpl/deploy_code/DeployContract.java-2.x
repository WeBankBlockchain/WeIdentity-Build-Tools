/*
 *       Copyright© (2018-2019) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-java-sdk.
 *
 *       weidentity-java-sdk is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-java-sdk is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-java-sdk.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.webank.weid.command;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.v2.AuthorityIssuerController;
import com.webank.weid.contract.v2.AuthorityIssuerData;
import com.webank.weid.contract.v2.CommitteeMemberController;
import com.webank.weid.contract.v2.CommitteeMemberData;
import com.webank.weid.contract.v2.CptController;
import com.webank.weid.contract.v2.CptData;
import com.webank.weid.contract.v2.EvidenceContract;
import com.webank.weid.contract.v2.EvidenceFactory;
import com.webank.weid.contract.v2.RoleController;
import com.webank.weid.contract.v2.SpecificIssuerController;
import com.webank.weid.contract.v2.SpecificIssuerData;
import com.webank.weid.contract.v2.WeIdContract;
import com.webank.weid.exception.InitWeb3jException;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

/**
 * The Class DeployContract.
 *
 * @author tonychen
 */
public class DeployContract {


    /**
     * The Fisco Config bundle.
     */
    protected static final FiscoConfig fiscoConfig;

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(DeployContract.class);


    private static Service service;
    /**
     * The credentials.
     */
    private static Credentials credentials;

    /**
     * web3j object.
     */
    private static Web3j web3j;

    static {
    	fiscoConfig = new FiscoConfig();
        if (!fiscoConfig.load()) {
            logger.error("[DeployContract] Failed to load Fisco-BCOS blockchain node information.");
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        deployContract();
        System.exit(0);
    }

    /**
     * Load config.
     *
     * @return true, if successful
     */
    private static boolean loadConfig() {

    	logger.info("[WeServiceImplV2] begin to init web3j instance..");
        service = buildFiscoBcosService(fiscoConfig);
        try {
            service.run();
        } catch (Exception e) {
            logger.error("[WeServiceImplV2] Service init failed. ", e);
            System.out.println("[DeployContract] Error: init web3j failed, please check your FISCO BCOS node ip, channel_listen_port and node cert.");
            System.exit(1);
        }

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        web3j = Web3j.build(channelEthereumService);
        if (web3j == null) {
            logger.error("[WeServiceImplV2] web3j init failed. ");
            throw new InitWeb3jException();
        }

        credentials = GenCredential.create();      
        
        String publicKey = String.valueOf(credentials.getEcKeyPair().getPublicKey());
        String privateKey = String.valueOf(credentials.getEcKeyPair().getPrivateKey());

        //将公私钥输出到output
        FileUtils.writeToFile(publicKey, "ecdsa_key.pub", FileOperator.OVERWRITE);
        FileUtils.writeToFile(privateKey, "ecdsa_key", FileOperator.OVERWRITE);

        if (null == credentials) {
            logger.error("[BaseService] credentials init failed.");
            System.out.println("[DeployContract] Error: credentials init failed, try to re-run.");
            System.exit(1);
        }

        return true;
    }

    private static Service buildFiscoBcosService(FiscoConfig fiscoConfig) {

        Service service = new Service();
        service.setOrgID(fiscoConfig.getCurrentOrgId());
        service.setConnectSeconds(Integer.valueOf(fiscoConfig.getWeb3sdkTimeout()));
        // group info
        Integer groupId = Integer.valueOf(fiscoConfig.getGroupId());
        service.setGroupId(groupId);

        // connect key and string
        ChannelConnections channelConnections = new ChannelConnections();
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

        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setBeanName("web3sdk");
        pool.setCorePoolSize(Integer.valueOf(fiscoConfig.getWeb3sdkCorePoolSize()));
        pool.setMaxPoolSize(Integer.valueOf(fiscoConfig.getWeb3sdkMaxPoolSize()));
        pool.setQueueCapacity(Integer.valueOf(fiscoConfig.getWeb3sdkQueueSize()));
        pool.setKeepAliveSeconds(Integer.valueOf(fiscoConfig.getWeb3sdkKeepAliveSeconds()));
        pool.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
        pool.initialize();
        service.setThreadPool(pool);
        return service;
    }
    
    /**
     * Gets the web3j.
     *
     * @return the web3j instance
     */
    protected static Web3j getWeb3j() {
        if (web3j == null) {
            loadConfig();
        }
        return web3j;
    }

    private static void deployContract() {
        String weIdContractAddress = deployWeIdContract();
        String roleControllerAddress = deployRoleControllerContracts();
        Map<String, String> addrList = deployIssuerContracts(roleControllerAddress);
        if (addrList.containsKey("AuthorityIssuerData")) {
            String authorityIssuerDataAddress = addrList.get("AuthorityIssuerData");
            deployCptContracts(
                authorityIssuerDataAddress,
                weIdContractAddress,
                roleControllerAddress
            );
        }
        deployEvidenceContractsNew();
    }

    private static String deployEvidenceContractsNew() {
        if (web3j == null) {
            loadConfig();
        }
        try {
            EvidenceContract evidenceContract =
                EvidenceContract.deploy(
                    web3j,
                    credentials,
                    new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT)
                ).send();
            String evidenceContractAddress = evidenceContract.getContractAddress();
            FileUtils.writeToFile(evidenceContractAddress, "evidenceController.address", FileOperator.OVERWRITE);
            return evidenceContractAddress;
        } catch (Exception e) {
            logger.error("EvidenceFactory deploy exception", e);
        }
        return StringUtils.EMPTY;
    }

    private static String deployRoleControllerContracts() {
        if (web3j == null) {
            loadConfig();
        }
        RoleController roleController = null;
        try {
            roleController =
                RoleController.deploy(
                    web3j,
                    credentials,
                    new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT))
					.send();
            return roleController.getContractAddress();
        } catch (Exception e) {
            logger.error("RoleController deploy exception", e);
            return StringUtils.EMPTY;
        }
    }

    private static String deployWeIdContract() {
        if (web3j == null) {
            loadConfig();
        }

        WeIdContract weIdContract = null;
		try {
			weIdContract = WeIdContract.deploy(
					web3j, 
					credentials, 
					new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT))
					.send();
		} catch (Exception e) {
			e.printStackTrace();
			return StringUtils.EMPTY;
		}

            String contractAddress = weIdContract.getContractAddress();
            FileUtils.writeToFile(contractAddress, "weIdContract.address", FileOperator.OVERWRITE);
            logger.info("[DeployContract] weid contract Address is {}.", contractAddress);
            return contractAddress;
        
    }

    private static String deployCptContracts(
        String authorityIssuerDataAddress,
        String weIdContractAddress,
        String roleControllerAddress) {
        if (web3j == null) {
            loadConfig();
        }

        try {
            CptData cptData =
                CptData.deploy(
                    web3j,
                    credentials,
                    new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT), 
                    authorityIssuerDataAddress).send();
            String cptDataAddress = cptData.getContractAddress();

            CptController cptController =
                CptController.deploy(
                    web3j,
                    credentials,
                    new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT), 
                    cptDataAddress,
                    weIdContractAddress
                ).send();
            String cptControllerAddress = cptController.getContractAddress();
            FileUtils.writeToFile(cptControllerAddress, "cptController.address", FileOperator.OVERWRITE);
			logger.info("[DeployContract] cptController contract Address is {}.", cptControllerAddress);
            TransactionReceipt receipt = 
            		cptController.setRoleController(roleControllerAddress).send();
            if (receipt == null) {
            	logger.error("CptController deploy exception");
            }
        } catch (Exception e) {
            logger.error("CptController deploy exception", e);
        }

        return StringUtils.EMPTY;
    }


    private static Map<String, String> deployIssuerContracts(String roleControllerAddress) {
        if (web3j == null) {
            loadConfig();
        }
        Map<String, String> issuerAddressList = new HashMap<>();

        String committeeMemberDataAddress;
        try {
            CommitteeMemberData committeeMemberData = CommitteeMemberData.deploy(
                web3j,
                credentials,
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT), 
                roleControllerAddress).send();
            committeeMemberDataAddress = committeeMemberData.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(committeeMemberDataAddress))) {
                issuerAddressList.put("CommitteeMemberData", committeeMemberDataAddress);
            }
        } catch (Exception e) {
            logger.error("CommitteeMemberData deployment error:", e);
            return issuerAddressList;
        }

        String committeeMemberControllerAddress;
        try {
            CommitteeMemberController committeeMemberController = CommitteeMemberController.deploy(
                web3j,
                credentials,
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT), 
                committeeMemberDataAddress,
                roleControllerAddress
            ).send();
            committeeMemberControllerAddress = committeeMemberController.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(committeeMemberControllerAddress))) {
                issuerAddressList
                    .put("CommitteeMemberController", committeeMemberControllerAddress);
            }
        } catch (Exception e) {
            logger.error("CommitteeMemberController deployment error:", e);
            return issuerAddressList;
        }

        String authorityIssuerDataAddress;
        try {
            AuthorityIssuerData authorityIssuerData = AuthorityIssuerData.deploy(
                web3j,
                credentials,
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT), 
                roleControllerAddress
            ).send();
            authorityIssuerDataAddress = authorityIssuerData.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(authorityIssuerDataAddress))) {
                issuerAddressList.put("AuthorityIssuerData", authorityIssuerDataAddress);
            }
        } catch (Exception e) {
            logger.error("AuthorityIssuerData deployment error:", e);
            return issuerAddressList;
        }

        String authorityIssuerControllerAddress;
        try {
            AuthorityIssuerController authorityIssuerController = AuthorityIssuerController.deploy(
                web3j,
                credentials,
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT), 
                authorityIssuerDataAddress,
                roleControllerAddress).send();
            authorityIssuerControllerAddress = authorityIssuerController.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(authorityIssuerControllerAddress))) {
                issuerAddressList
                    .put("AuthorityIssuerController", authorityIssuerControllerAddress);
            }
        } catch (Exception e) {
            logger.error("AuthorityIssuerController deployment error:", e);
            return issuerAddressList;
        }

        try {
            FileUtils.writeToFile(authorityIssuerControllerAddress, "authorityIssuer.address", FileOperator.OVERWRITE);
            logger.info("[DeployContract] authorityIssuer contract Address is {}.", authorityIssuerControllerAddress);
        } catch (Exception e) {
            logger.error("Write error:", e);
        }

        String specificIssuerDataAddress = StringUtils.EMPTY;
        try {
            SpecificIssuerData specificIssuerData = SpecificIssuerData.deploy(
                web3j,
                credentials,
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT)
            ).send();
            specificIssuerDataAddress = specificIssuerData.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(specificIssuerDataAddress))) {
                issuerAddressList.put("SpecificIssuerData", specificIssuerDataAddress);
            }
        } catch (Exception e) {
            logger.error("SpecificIssuerData deployment error:", e);
        }

        try {
            SpecificIssuerController specificIssuerController = SpecificIssuerController.deploy(
                web3j,
                credentials,
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT),
                specificIssuerDataAddress,
                roleControllerAddress
            ).send();
            String specificIssuerControllerAddress = specificIssuerController.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(specificIssuerControllerAddress))) {
                issuerAddressList.put("SpecificIssuerController", specificIssuerControllerAddress);
            }
            try {
                FileUtils.writeToFile(specificIssuerControllerAddress, "specificIssuer.address", FileOperator.OVERWRITE);
                logger.info("[DeployContract] specificIssuer contract Address is {}.", specificIssuerControllerAddress);
            } catch (Exception e) {
                logger.error("Write error:", e);
            }
        } catch (Exception e) {
            logger.error("SpecificIssuerController deployment error:", e);
        }
        return issuerAddressList;
    }

    private static String deployEvidenceContracts() {
        if (web3j == null) {
            loadConfig();
        }
        try {
            EvidenceFactory evidenceFactory =
                EvidenceFactory.deploy(
                		web3j, 
        				credentials, 
        				new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT)
                ).send();
            String evidenceFactoryAddress = evidenceFactory.getContractAddress();
            FileUtils.writeToFile(evidenceFactoryAddress, "evidenceController.address", FileOperator.OVERWRITE);
            logger.info("[DeployContract] evidence contract Address is {}.", evidenceFactoryAddress);
            return evidenceFactoryAddress;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("EvidenceFactory deploy exception", e);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return StringUtils.EMPTY;
    }
}
