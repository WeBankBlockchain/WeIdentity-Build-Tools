package com.webank.weid.command;

import java.math.BigInteger;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.CnsResponse;
import com.webank.weid.service.BaseService;
import com.webank.weid.service.DeployService;
import com.webank.weid.util.DataToolUtils;

/**
 * 升级dataBucket.
 * @author v_wbgyang
 *
 */
public class UpgradeDataBucket {
    
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(UpgradeDataBucket.class);
    
    public static void main(String[] args) {
        try {
            System.out.println("begin upgrade DataBucket...");
            // 获取私钥
            WeIdPrivateKey currentPrivateKey = DeployService.getCurrentPrivateKey();
            // 根据私钥获取Credentials
            Credentials credentials = GenCredential.create(new BigInteger(currentPrivateKey.getPrivateKey()).toString(16));
            // 重新部署所有的DataBucket
            for (CnsType cnsType : CnsType.values()) {
                CnsInfo cnsInfo = BaseService.getBucketByCns(cnsType);
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
                System.out.println(cnsType.getName() + ": " + cnsInfo.getVersion() + " --> " + newVersion);
            }
            System.out.println("the DataBucket upgrade successfully.");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("the DataBucket upgrade fail.", e);
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
