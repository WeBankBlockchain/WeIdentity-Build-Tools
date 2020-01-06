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
import java.util.List;

import org.bcos.channel.client.Service;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.crypto.ECKeyPair;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.TransactionUtils;
import com.webank.weid.util.WeIdUtils;

/**
 * The Class DeploySystemCpt.
 *
 * @author chaoxinhu
 */
public class DeploySystemCpt {

    /**
     * The Fisco Config bundle.
     */
    protected static final FiscoConfig fiscoConfig;

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(DeployContract.class);

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

        loadConfig();
        if (!registerSystemCpt()) {
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Load config.
     *
     * @return true, if successful
     */
    private static boolean loadConfig() {
        return initWeb3j();
    }

    private static boolean initWeb3j() {
        logger.info("[DeployContract] begin to init web3j instance..");
        Service service = TransactionUtils.buildFiscoBcosService(fiscoConfig);
        try {
            service.run();
        } catch (Exception e) {
            logger.error("[DeployContract] Service init failed. ", e);
            return false;
        }

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        web3j = Web3j.build(channelEthereumService);
        if (web3j == null) {
            logger.error("[DeployContract] web3j init failed. ");
            return false;
        }

        credentials = Credentials.create(
            ECKeyPair.create(new BigInteger(FileUtils.readFile("./output/admin/ecdsa_key"))));
        String publicKey = String.valueOf(credentials.getEcKeyPair().getPublicKey());
        String privateKey = String.valueOf(credentials.getEcKeyPair().getPrivateKey());
        CreateWeIdArgs arg = new CreateWeIdArgs();
        arg.setPublicKey(publicKey);
        WeIdPrivateKey pkey = new WeIdPrivateKey();
        pkey.setPrivateKey(privateKey);
        arg.setWeIdPrivateKey(pkey);
        WeIdService weIdService = new WeIdServiceImpl();
        weIdService.createWeId(arg);

        return true;
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

        List<Integer> cptIdList = Arrays.asList(107, 110, 111);
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
}