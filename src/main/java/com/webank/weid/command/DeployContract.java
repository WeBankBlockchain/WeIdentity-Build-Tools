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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.AuthorityIssuerController;
import com.webank.weid.contract.AuthorityIssuerData;
import com.webank.weid.contract.CptController;
import com.webank.weid.contract.CptData;
import com.webank.weid.contract.EvidenceFactory;
import com.webank.weid.contract.RoleController;
import com.webank.weid.contract.WeIdContract;
import com.webank.weid.util.FileUtils;

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
        System.exit(0);
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
            logger.error("[BaseService] Service init failed.", e);
        }

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        web3j = Web3j.build(channelEthereumService);

        logger.info("begin init credentials");
        
        ECKeyPair keyPair = null;

        try {
            keyPair = Keys.createEcKeyPair();
        } catch (Exception e) {
            logger.error("Create weId failed.", e);
            return false;
        }

        String publicKey = String.valueOf(keyPair.getPublicKey());
        String privateKey = String.valueOf(keyPair.getPrivateKey());
        
        //将公私钥输出到output
        FileUtils.writeToFile(publicKey, "ecdsa_key.pub", FileOperator.OVERWRITE);
        FileUtils.writeToFile(privateKey, "ecdsa_key", FileOperator.OVERWRITE);
        credentials = Credentials.create(keyPair);

        if (null == credentials) {
            logger.error("[BaseService] credentials init failed.");
            return false;
        }
        return true;
    }

    private static void deployContract() {
        String weIdContractAddress = deployWeIdContract();
        String authorityIssuerDataAddress = deployAuthorityIssuerContracts();
        deployCptContracts(authorityIssuerDataAddress, weIdContractAddress);
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
            logger.error("WeIdContract deploy exception.", e);
        }
        return StringUtils.EMPTY;
    }

    private static String deployCptContracts(
        String authorityIssuerDataAddress, String weIdContractAddress) {

        try {
            Future<CptData> f1 =
                CptData.deploy(
                    web3j,
                    credentials,
                    WeIdConstant.GAS_PRICE,
                    WeIdConstant.GAS_LIMIT,
                    WeIdConstant.INILITIAL_VALUE,
                    new Address(authorityIssuerDataAddress)
                );
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
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("CptController deploy exception.", e);
        }
        return StringUtils.EMPTY;
    }

    private static String deployAuthorityIssuerContracts() {

        // Step 1: Deploy RoleController sol => [addr1]
        String authorityIssuerDataAddress = StringUtils.EMPTY;
        Future<RoleController> f1 =
            RoleController.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE
            );
        String roleControllerAddress = StringUtils.EMPTY;
        try {
            RoleController roleController =
                f1.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            roleControllerAddress = roleController.getContractAddress();

            // Step 2: Deploy AuthorityIssuerData sol => [addr1]
            Future<AuthorityIssuerData> f2 = null;
            f2 = AuthorityIssuerData.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(roleControllerAddress)
            );

            AuthorityIssuerData authorityIssuerData =
                f2.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            authorityIssuerDataAddress = authorityIssuerData.getContractAddress();

            // Step 3: Deploy AuthorityIssuerController sol => [addr1]
            Future<AuthorityIssuerController> f3 = null;
            f3 = AuthorityIssuerController.deploy(
                web3j,
                credentials,
                WeIdConstant.GAS_PRICE,
                WeIdConstant.GAS_LIMIT,
                WeIdConstant.INILITIAL_VALUE,
                new Address(authorityIssuerDataAddress),
                new Address(roleControllerAddress)
            );

            AuthorityIssuerController authorityIssuerController =
                f3.get(DEFAULT_DEPLOY_CONTRACTS_TIMEOUT_IN_SECONDS,
                    TimeUnit.SECONDS);
            String authorityIssuerControllerAddress =
                authorityIssuerController.getContractAddress();
            FileUtils.writeToFile(authorityIssuerControllerAddress, "authorityIssuer.address",
                FileOperator.OVERWRITE);

        } catch (Exception e) {
            logger.error("AuthorityIssuerData deployment error:", e);
            return authorityIssuerDataAddress;
        }
        return authorityIssuerDataAddress;
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
            logger.error("EvidenceFactory deploy exception", e);
        }
        return StringUtils.EMPTY;
    }

}
