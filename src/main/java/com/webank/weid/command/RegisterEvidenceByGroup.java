

package com.webank.weid.command;

import com.beust.jcommander.JCommander;
import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.blockchain.constant.CnsType;
import com.webank.weid.blockchain.deploy.v2.RegisterAddressV2;
import com.webank.weid.blockchain.deploy.v3.RegisterAddressV3;
import com.webank.weid.blockchain.service.fisco.BaseServiceFisco;
import com.webank.weid.blockchain.service.fisco.CryptoFisco;
import com.webank.weid.blockchain.service.fisco.server.WeServer;
import com.webank.weid.constant.ChainVersion;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.deploy.AddressProcess;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.blockchain.util.DataToolUtils;
import com.webank.weid.util.WeIdSdkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;

import java.math.BigInteger;

/**
 *  todo support v3 test
 * @author marsli
 */
@Slf4j
public class RegisterEvidenceByGroup {

    public static void main(String[] args) {
        String goupIdStr = null;
        String cns = null;
        try {
            CommandArgs commandArgs = new CommandArgs();
            JCommander.newBuilder()
                .addObject(commandArgs)
                .build()
                .parse(args);
            // 检查输入群组编号是否为空
            goupIdStr = commandArgs.getGroupId();
            if(StringUtils.isBlank(goupIdStr)) {
                System.out.println("[RegisterEvidenceByGroup] input error, the groupId is null. Abort.");
                System.exit(1);
            }
            // 检查输入群组编号是否为数字
            if (!NumberUtils.isDigits(goupIdStr)) {
                System.out.println("[RegisterEvidenceByGroup] input error, the groupId does not digits. Abort.");
                System.exit(1);
            }
            // 检查输入cns 地址是否为空
            cns = commandArgs.getCns();
            if(StringUtils.isBlank(cns)) {
                System.out.println("[RegisterEvidenceByGroup] input error, the cns is null. Abort.");
                System.exit(1);
            }
            System.out.println("[RegisterEvidenceByGroup] begin register evidenceAddress by cns and groupId, cns = "+ cns + ", groupId = " + goupIdStr);
            // 检查群组是否存在
            String groupId = goupIdStr;
            boolean checkGroupId = BaseServiceFisco.checkGroupId(goupIdStr);
            if (!checkGroupId) {
                System.out.println("[RegisterEvidenceByGroup] input error, the group does not exists, Abort.");
                System.exit(1);
            }
            // 获取当前私钥账户
            WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getCurrentPrivateKey();
            String privatekey = currentPrivateKey.getPrivateKey();
            // 检查输入cns 地址是否正确存在Evidence地址
            FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
            WeServer<?, ?, ?> weServer = WeServer.getInstance(fiscoConfig, groupId);
            // v2 chain
            if (ChainVersion.FISCO_V2.getVersion() == Integer.parseInt(fiscoConfig.getVersion())) {
                CryptoKeyPair credentials = CryptoFisco.cryptoSuite.getKeyPairFactory()
                    .createKeyPair(new BigInteger(privatekey));
                // 加载DataBucket
                DataBucket dataBucket = DataBucket.load(
                    weServer.getBucketByCns(CnsType.DEFAULT).getAddress(),
                    (Client) weServer.getWeb3j(),
                    credentials
                );
                Bytes32 keyByte32 = DataToolUtils
                    .bytesArrayToBytes32(WeIdConstant.CNS_EVIDENCE_ADDRESS.getBytes());
                // 根据cns hash值从dataBucket中获取evidenceAddress
                Tuple2<BigInteger, String> tuple = dataBucket.get(cns, keyByte32.getValue());
                int code = tuple.getValue1().intValue();
                if (code == 102) {
                    System.out.println(
                        "[RegisterEvidenceByGroup] can not find evidenceAddress by cns = " + cns
                            + ", Abort.");
                    System.exit(1);
                }
                String evidenceAddress = tuple.getValue2();
                System.out.println(
                    "[RegisterEvidenceByGroup] get the evidence address is: " + evidenceAddress);
                // 将地址注册到cns中
                CnsType cnsType = CnsType.SHARE;
                // 注册SHARE CNS 默认主群组
                RegisterAddressV2.registerBucketToCns(cnsType, currentPrivateKey.getPrivateKey());
                // 根据群组和evidence Address获取hash
                String hash = AddressProcess.getHashForShare(groupId, evidenceAddress);
                // 将evidence地址注册到cns中 默认主群组
                RegisterAddressV2.registerAddress(
                    cnsType,
                    hash,
                    evidenceAddress,
                    WeIdConstant.CNS_EVIDENCE_ADDRESS,
                    currentPrivateKey.getPrivateKey()
                );
                // 将群组编号注册到cns中 默认主群组
                RegisterAddressV2.registerAddress(
                    cnsType,
                    hash,
                    groupId,
                    WeIdConstant.CNS_GROUP_ID,
                    currentPrivateKey.getPrivateKey()
                );
            } else {
                org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair credentials =
                    new CryptoSuite(CryptoFisco.cryptoSuite.getCryptoTypeConfig()).getKeyPairFactory()
                    .createKeyPair(new BigInteger(privatekey));
                // 加载DataBucket
                com.webank.weid.contract.v3.DataBucket dataBucket =
                    com.webank.weid.contract.v3.DataBucket.load(
                    weServer.getBucketByCns(CnsType.DEFAULT).getAddress(),
                    (org.fisco.bcos.sdk.v3.client.Client) weServer.getWeb3j(),
                    credentials
                );
                Bytes32 keyByte32 = DataToolUtils
                    .bytesArrayToBytes32(WeIdConstant.CNS_EVIDENCE_ADDRESS.getBytes());
                // 根据cns hash值从dataBucket中获取evidenceAddress
                org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2<BigInteger, String>
                    tuple = dataBucket.get(cns, keyByte32.getValue());
                int code = tuple.getValue1().intValue();
                if (code == 102) {
                    System.out.println(
                        "[RegisterEvidenceByGroup] can not find evidenceAddress by cns = " + cns
                            + ", Abort.");
                    System.exit(1);
                }
                String evidenceAddress = tuple.getValue2();
                System.out.println(
                    "[RegisterEvidenceByGroup] get the evidence address is: " + evidenceAddress);
                // 将地址注册到cns中
                CnsType cnsType = CnsType.SHARE;
                // 注册SHARE CNS 默认主群组
                RegisterAddressV3.registerBucketToCns(cnsType, currentPrivateKey.getPrivateKey());
                // 根据群组和evidence Address获取hash
                String hash = AddressProcess.getHashForShare(groupId, evidenceAddress);
                // 将evidence地址注册到cns中 默认主群组
                RegisterAddressV3.registerAddress(
                    cnsType,
                    hash,
                    evidenceAddress,
                    WeIdConstant.CNS_EVIDENCE_ADDRESS,
                    currentPrivateKey.getPrivateKey()
                );
                // 将群组编号注册到cns中 默认主群组
                RegisterAddressV3.registerAddress(
                    cnsType,
                    hash,
                    groupId,
                    WeIdConstant.CNS_GROUP_ID,
                    currentPrivateKey.getPrivateKey()
                );

            }
            System.out.println(
                "[RegisterEvidenceByGroup] register address into cns by group has successfully.");
            System.exit(0);
        } catch (Exception e) {
            log.error(
                "[RegisterEvidenceByGroup] register address into cns by group has error. cns = {}, groupId = {}",
                cns, 
                goupIdStr,
                e
            );
            System.out.println("[RegisterEvidenceByGroup] register address into cns by group has error. please check the log.");
            System.exit(1);
        }
        
    }

}
