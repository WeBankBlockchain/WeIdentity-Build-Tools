

package com.webank.weid.command;

import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.blockchain.constant.CnsType;
import com.webank.weid.blockchain.protocol.response.CnsInfo;
import com.webank.weid.blockchain.service.fisco.BaseServiceFisco;
import com.webank.weid.blockchain.service.fisco.CryptoFisco;
import com.webank.weid.constant.ChainVersion;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.util.WeIdSdkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;

import java.math.BigInteger;

/**
 * todo 支持v3
 * 升级dataBucket.
 * @author v_wbgyang
 *
 */
@Slf4j
public class UpgradeDataBucket {
    
    public static void main(String[] args) {
        try {
            System.out.println("begin upgrade DataBucket...");
            // 获取私钥
            WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getCurrentPrivateKey();
            if(StringUtils.isBlank(currentPrivateKey.getPrivateKey())) {
                System.out.println("the DataBucket upgrade fail: can not found the private key.");
                System.exit(1);
            }

            FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
            // v2 chain
            if (ChainVersion.FISCO_V2.getVersion() == Integer.parseInt(fiscoConfig.getVersion())) {
                // 根据私钥获取Credentials
                CryptoKeyPair credentials = CryptoFisco.cryptoSuite.getKeyPairFactory()
                    .createKeyPair(new BigInteger(currentPrivateKey.getPrivateKey()));
                // 重新部署所有的DataBucket
                for (CnsType cnsType : CnsType.values()) {
                    CnsInfo cnsInfo = BaseServiceFisco.getBucketByCns(cnsType);
                    String oldVersion = cnsType.getVersion();
                    if (cnsInfo != null) {
                        oldVersion = cnsInfo.getVersion();
                    }
                    String newVersion = newVersion(cnsType, cnsInfo);
                    //部署DataBucket
                    DataBucket dataBucket = DataBucket.deploy(
                        (Client) BaseServiceFisco.getClient(),
                        credentials);
                    RetCode result = new CnsService((Client) BaseServiceFisco.getClient(), credentials)
                        .registerCNS(cnsType.getName(), newVersion, dataBucket.getContractAddress(),
                            DataBucket.ABI);
                    if (result.getCode() != 0) {
                        throw new WeIdBaseException(result.getCode() + "-" + result.getMessage());
                    }
                    System.out
                        .println(cnsType.getName() + ": " + oldVersion + " --> " + newVersion);
                }
            } else {
                // 根据私钥获取Credentials
                org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair credentials =
                    new CryptoSuite(CryptoFisco.cryptoSuite.getCryptoTypeConfig()).getKeyPairFactory()
                        .createKeyPair(new BigInteger(currentPrivateKey.getPrivateKey()));
                // 重新部署所有的DataBucket
                for (CnsType cnsType : CnsType.values()) {
                    CnsInfo cnsInfo = BaseServiceFisco.getBucketByCns(cnsType);
                    String oldVersion = cnsType.getVersion();
                    if (cnsInfo != null) {
                        oldVersion = cnsInfo.getVersion();
                    }
                    String newVersion = newVersion(cnsType, cnsInfo);
                    //部署DataBucket
                    org.fisco.bcos.sdk.v3.client.Client client =
                        (org.fisco.bcos.sdk.v3.client.Client) BaseServiceFisco.getClient();
                    com.webank.weid.contract.v3.DataBucket dataBucket =
                        com.webank.weid.contract.v3.DataBucket.deploy(
                            client, credentials);
                    org.fisco.bcos.sdk.v3.model.RetCode result =
                        new BFSService(client, credentials)
                        .link(cnsType.getName(), newVersion, dataBucket.getContractAddress(),
                            DataBucket.ABI);
                    if (result.getCode() != 0) {
                        throw new WeIdBaseException(result.getCode() + "-" + result.getMessage());
                    }
                    System.out
                        .println(cnsType.getName() + ": " + oldVersion + " --> " + newVersion);
                }
            }
            System.out.println("the DataBucket upgrade successfully.");
            System.exit(0);
        } catch (Exception e) {
            log.error("the DataBucket upgrade fail.", e);
            System.out.println("the DataBucket upgrade fail.");
            System.exit(1);
        }
    }
    
    private static String newVersion(CnsType cnsType, CnsInfo cnsInfo) {
        if (cnsInfo == null) {
            return cnsType.getVersion();
        }
        String version = cnsInfo.getVersion();
        String preVersion = version.substring(0, version.indexOf(".") + 1);
        String lastVersion = version.substring(version.indexOf(".") + 1);
        Integer value = Integer.parseInt(lastVersion);
        value++;
        return preVersion + value;
    }

}
