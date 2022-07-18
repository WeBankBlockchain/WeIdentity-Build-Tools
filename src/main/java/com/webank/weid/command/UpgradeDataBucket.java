

package com.webank.weid.command;

import java.math.BigInteger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.CnsResponse;
import com.webank.weid.service.BaseService;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.WeIdSdkUtils;

/**
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
            EncryptType encryptType = new EncryptType(Integer.parseInt(fiscoConfig.getEncryptType()));

            // 根据私钥获取Credentials
            Credentials credentials = GenCredential.create(new BigInteger(currentPrivateKey.getPrivateKey()).toString(16));
            // 重新部署所有的DataBucket
            for (CnsType cnsType : CnsType.values()) {
                CnsInfo cnsInfo = BaseService.getBucketByCns(cnsType);
                String oldVersion = cnsType.getVersion();
                if (cnsInfo != null) {
                    oldVersion = cnsInfo.getVersion();
                }
                String newVersion = newVersion(cnsType, cnsInfo);
                //部署DataBucket
                DataBucket dataBucket = DataBucket.deploy(
                        (Web3j)BaseService.getWeb3j(),
                        credentials, 
                        new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT)).send();
                String resultJson = new CnsService((Web3j)BaseService.getWeb3j(), credentials)
                .registerCns(cnsType.getName(), newVersion, dataBucket.getContractAddress(), DataBucket.ABI);
                CnsResponse result = DataToolUtils.deserialize(resultJson, CnsResponse.class);
                if (result.getCode() != 0) {
                    throw new WeIdBaseException(result.getCode() + "-" + result.getMsg());
                }
                System.out.println(cnsType.getName() + ": " + oldVersion + " --> " + newVersion);
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
