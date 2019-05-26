/*
 *       Copyright© (2018-2019) WeBank Co., Ltd.
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

package com.webank.weid.command;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.bcos.channel.client.Service;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.crypto.ECKeyPair;
import org.bcos.web3j.crypto.Keys;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.AuthorityIssuerController;
import com.webank.weid.contract.AuthorityIssuerData;
import com.webank.weid.contract.CommitteeMemberController;
import com.webank.weid.contract.CommitteeMemberData;
import com.webank.weid.contract.CptController;
import com.webank.weid.contract.CptData;
import com.webank.weid.contract.EvidenceFactory;
import com.webank.weid.contract.RoleController;
import com.webank.weid.contract.SpecificIssuerController;
import com.webank.weid.contract.SpecificIssuerData;
import com.webank.weid.contract.WeIdContract;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

/**
 * The Class DeployContract.
 *
 * @author tonychen
 */
public class DeployContract {

    /**
     * The context.
     */
    protected static final ApplicationContext context;
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(DeployContract.class);
    /**
     * The Constant for default deploy contracts timeout.
     */
    private static final Integer DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS = 15;
    /**
     * The credentials.
     */
    private static Credentials credentials;

    /**
     * web3j object.
     */
    private static Web3j web3j;

    static {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        loadConfig();
        deployContract();
        if (!registerSystemCpt()) {
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Deploy System CPTs.
     *
     * @return true if succeed, false otherwise
     */
    public static boolean registerSystemCpt() {
        CptStringArgs cptStringArgs = new CptStringArgs();
        WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
        BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
        String weId = WeIdUtils
            .convertPublicKeyToWeId(DataToolUtils.publicKeyFromPrivate(privateKey).toString());
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey.toString());
        weIdAuthentication.setWeIdPrivateKey(weIdPrivateKey);
        weIdAuthentication.setWeId(weId);
        cptStringArgs.setWeIdAuthentication(weIdAuthentication);

        List<Integer> cptIdList = Arrays.asList(101, 102, 103);
        CptServiceImpl cptService = new CptServiceImpl();
        for (Integer cptId : cptIdList) {
            String cptJsonSchema = DataToolUtils.generateDefaultCptJsonSchema(cptId);
            if (cptJsonSchema.isEmpty()) {
                logger.info("[DeployContract] Cannot generate CPT json schema with ID: " + cptId);
                return false;
            }
            cptStringArgs.setCptJsonSchema(cptJsonSchema);
            ResponseData responseData = cptService.registerCpt(cptStringArgs, cptId);
            if (responseData.getResult() == null) {
                logger.info("[DeployContract] Register System CPT failed with ID: " + cptId);
                return false;
            }
        }
        return true;
    }

    /**
     * Load config.
     *
     * @return true, if successful
     */
    private static boolean loadConfig() {

        Service service = context.getBean(Service.class);
        try {
            service.run();
        } catch (Exception e) {
            logger.error("[DeployContract] Service init failed.", e);
        }

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        web3j = Web3j.build(channelEthereumService);

        logger.info("[DeployContract] begin init credentials");

        ECKeyPair keyPair = null;

        try {
            keyPair = Keys.createEcKeyPair();
        } catch (Exception e) {
            logger.error("[DeployContract] Create weId failed.", e);
            return false;
        }

        String publicKey = String.valueOf(keyPair.getPublicKey());
        String privateKey = String.valueOf(keyPair.getPrivateKey());

        //将公私钥输出到output
        FileUtils.writeToFile(publicKey, "ecdsa_key.pub", FileOperator.OVERWRITE);
        FileUtils.writeToFile(privateKey, "ecdsa_key", FileOperator.OVERWRITE);
        credentials = Credentials.create(keyPair);

        if (null == credentials) {
            logger.error("[DeployContract] credentials init failed.");
            return false;
        }
        return true;
    }

    private static void deployContract() {
        String weIdContractAddress = deployWeIdContract();
        String roleControllerAddress = deployRoleControllerContracts();
        Map<String, String> addrList = deployIssuerContracts(roleControllerAddress);
        if (addrList.containsKey("AuthorityIssuerData")) {
            String authorityIssuerDataAddress = addrList.get("AuthorityIssuerData");
            deployCptContracts(authorityIssuerDataAddress, weIdContractAddress,
                roleControllerAddress);
        }
        deployEvidenceContracts();
    }

    private static String deployWeIdContract() {

        Future<WeIdContract> f =
            WeIdContract.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE
            );
        try {
            WeIdContract weIdContract =
                f.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            String contractAddress = weIdContract.getContractAddress();
            FileUtils.writeToFile(contractAddress, "weIdContract.address", FileOperator.OVERWRITE);
            return contractAddress;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[DeployContract] WeIdContract deploy exception.", e);
        }
        return StringUtils.EMPTY;
    }

    private static String deployCptContracts(
        String authorityIssuerDataAddress,
        String weIdContractAddress,
        String roleControllerAddress) {
        if (web3j == null) {
            loadConfig();
        }

        try {
            Future<CptData> f1 =
                CptData.deploy(
                    web3j,
                    credentials,
                    WeIdConstant.GAS_PRICE,
                    WeIdConstant.GAS_LIMIT,
                    WeIdConstant.INILITIAL_VALUE,
                    new Address(authorityIssuerDataAddress));
            CptData cptData = f1.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            String cptDataAddress = cptData.getContractAddress();

            Future<CptController> f2 =
                CptController.deploy(
                    web3j,
                    credentials,
                    WeIdConstant.GAS_PRICE,
                    WeIdConstant.GAS_LIMIT,
                    WeIdConstant.INILITIAL_VALUE,
                    new Address(cptDataAddress),
                    new Address(weIdContractAddress)
                );
            CptController cptController =
                f2.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            String cptControllerAddress = cptController.getContractAddress();
            FileUtils
                .writeToFile(cptControllerAddress, "cptController.address", FileOperator.OVERWRITE);

            Future<TransactionReceipt> f3 = cptController
                .setRoleController(new Address(roleControllerAddress));
            f3.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[DeployContract] CptController deploy exception", e);
        }
        return StringUtils.EMPTY;
    }

    private static String deployRoleControllerContracts() {
        if (web3j == null) {
            loadConfig();
        }

        try {
            Future<RoleController> f1 =
                RoleController.deploy(
                    web3j,
                    credentials,
                    WeIdConstant.GAS_PRICE,
                    WeIdConstant.GAS_LIMIT,
                    WeIdConstant.INILITIAL_VALUE);
            RoleController roleController =
                f1.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            return roleController.getContractAddress();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[DeployContract] RoleController deploy exception", e);
            return StringUtils.EMPTY;
        }
    }

    private static Map<String, String> deployIssuerContracts(String roleControllerAddress) {
        if (web3j == null) {
            loadConfig();
        }
        Map<String, String> issuerAddressList = new HashMap<>();

        Future<CommitteeMemberData> f2;
        String committeeMemberDataAddress;
        try {
            f2 = CommitteeMemberData.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(roleControllerAddress));
            CommitteeMemberData committeeMemberData =
                f2.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            committeeMemberDataAddress = committeeMemberData.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(committeeMemberDataAddress))) {
                issuerAddressList.put("CommitteeMemberData", committeeMemberDataAddress);
            }
        } catch (Exception e) {
            logger.error("[DeployContract] CommitteeMemberData deployment error:", e);
            return issuerAddressList;
        }

        Future<CommitteeMemberController> f3;
        String committeeMemberControllerAddress;
        try {
            f3 = CommitteeMemberController.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(committeeMemberDataAddress),
                new Address(roleControllerAddress)
            );
            CommitteeMemberController committeeMemberController =
                f3.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            committeeMemberControllerAddress = committeeMemberController.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(committeeMemberControllerAddress))) {
                issuerAddressList
                    .put("CommitteeMemberController", committeeMemberControllerAddress);
            }
        } catch (Exception e) {
            logger.error("[DeployContract] CommitteeMemberController deployment error:", e);
            return issuerAddressList;
        }

        Future<AuthorityIssuerData> f4;
        String authorityIssuerDataAddress;
        try {
            f4 = AuthorityIssuerData.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(roleControllerAddress)
            );
            AuthorityIssuerData authorityIssuerData =
                f4.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            authorityIssuerDataAddress = authorityIssuerData.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(authorityIssuerDataAddress))) {
                issuerAddressList.put("AuthorityIssuerData", authorityIssuerDataAddress);
            }
        } catch (Exception e) {
            logger.error("[DeployContract] AuthorityIssuerData deployment error:", e);
            return issuerAddressList;
        }

        Future<AuthorityIssuerController> f5;
        String authorityIssuerControllerAddress;
        try {
            f5 = AuthorityIssuerController.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(authorityIssuerDataAddress),
                new Address(roleControllerAddress));
            AuthorityIssuerController authorityIssuerController =
                f5.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            authorityIssuerControllerAddress = authorityIssuerController.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(authorityIssuerControllerAddress))) {
                issuerAddressList
                    .put("AuthorityIssuerController", authorityIssuerControllerAddress);
            }
        } catch (Exception e) {
            logger.error("[DeployContract] AuthorityIssuerController deployment error:", e);
            return issuerAddressList;
        }

        try {
            FileUtils.writeToFile(authorityIssuerControllerAddress, "authorityIssuer.address",
                FileOperator.OVERWRITE);
        } catch (Exception e) {
            logger.error("[DeployContract] Write error:", e);
        }

        String specificIssuerDataAddress = StringUtils.EMPTY;
        try {
            Future<SpecificIssuerData> f6 = SpecificIssuerData.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE
            );
            SpecificIssuerData specificIssuerData = f6.get(
                DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS,
                TimeUnit.SECONDS
            );
            specificIssuerDataAddress = specificIssuerData.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(specificIssuerDataAddress))) {
                issuerAddressList.put("SpecificIssuerData", specificIssuerDataAddress);
            }
        } catch (Exception e) {
            logger.error("[DeployContract] SpecificIssuerData deployment error:", e);
        }

        try {
            Future<SpecificIssuerController> f7 = SpecificIssuerController.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(specificIssuerDataAddress),
                new Address(roleControllerAddress)
            );
            SpecificIssuerController specificIssuerController = f7.get(
                DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS,
                TimeUnit.SECONDS
            );
            String specificIssuerControllerAddress = specificIssuerController.getContractAddress();
            if (!WeIdUtils.isEmptyAddress(new Address(specificIssuerControllerAddress))) {
                issuerAddressList.put("SpecificIssuerController", specificIssuerControllerAddress);
            }
            try {
                FileUtils.writeToFile(specificIssuerControllerAddress, "specificIssuer.address",
                    FileOperator.OVERWRITE);
            } catch (Exception e) {
                logger.error("[DeployContract] Write error:", e);
            }
        } catch (Exception e) {
            logger.error("[DeployContract] SpecificIssuerController deployment error:", e);
        }
        return issuerAddressList;
    }

    private static String deployEvidenceContracts() {
        if (web3j == null) {
            loadConfig();
        }

        try {
            Future<EvidenceFactory> f =
                EvidenceFactory.deploy(
                    web3j,
                    credentials,
                    WeIdConstant.GAS_PRICE,
                    WeIdConstant.GAS_LIMIT,
                    WeIdConstant.INILITIAL_VALUE
                );
            EvidenceFactory evidenceFactory = f
                .get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            String evidenceFactoryAddress = evidenceFactory.getContractAddress();
            FileUtils.writeToFile(evidenceFactoryAddress, "evidenceController.address",
                FileOperator.OVERWRITE);
            return evidenceFactoryAddress;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[DeployContract] EvidenceFactory deploy exception", e);
        }
        return StringUtils.EMPTY;
    }

}
