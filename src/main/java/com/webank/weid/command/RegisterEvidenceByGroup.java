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

package com.webank.weid.command;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.deploy.AddressProcess;
import com.webank.weid.contract.deploy.v2.RegisterAddressV2;
import com.webank.weid.contract.v2.DataBucket;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.service.BaseService;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.DeployService;
import com.webank.weid.service.fisco.WeServer;
import com.webank.weid.util.DataToolUtils;

public class RegisterEvidenceByGroup {
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(RegisterEvidenceByGroup.class);
    
    private static ConfigService configService = new ConfigService();
    
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
            int groupId = Integer.parseInt(goupIdStr);
            boolean checkGroupId = BaseService.checkGroupId(groupId);
            if (!checkGroupId) {
                System.out.println("[RegisterEvidenceByGroup] input error, the group does not exists, Abort.");
                System.exit(1);
            }
            // 获取当前私钥账户
            WeIdPrivateKey currentPrivateKey = DeployService.getCurrentPrivateKey();
            String  privatekey = currentPrivateKey.getPrivateKey();
            // 检查输入cns 地址是否正确存在Evidence地址
            FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
            WeServer<?, ?, ?> weServer = WeServer.getInstance(fiscoConfig, groupId);
            Credentials credentials = GenCredential.create(new BigInteger(privatekey).toString(16));
            // 加载DataBucket
            DataBucket dataBucket = DataBucket.load(
                weServer.getBucketByCns(CnsType.DEFAULT).getAddress(), 
                (Web3j)weServer.getWeb3j(), 
                credentials, 
                new StaticGasProvider(WeIdConstant.GAS_PRICE, WeIdConstant.GAS_LIMIT)
            );
            Bytes32 keyByte32 = DataToolUtils.bytesArrayToBytes32(WeIdConstant.CNS_EVIDENCE_ADDRESS.getBytes());
            // 根据cns hash值从dataBucket中获取evidenceAddress
            Tuple2<BigInteger, String> tuple = dataBucket.get(cns, keyByte32.getValue()).send();
            int code = tuple.getValue1().intValue();
            if (code == 102) {
                System.out.println("[RegisterEvidenceByGroup] can not find evidenceAddress by cns = " + cns + ", Abort.");
                System.exit(1);
            }
            String evidenceAddress = tuple.getValue2();
            System.out.println("[RegisterEvidenceByGroup] get the evidence address is: " + evidenceAddress);
            // 将地址注册到cns中
            CnsType cnsType = CnsType.SHARE;
            // 注册SHARE CNS 默认主群组
            RegisterAddressV2.registerBucketToCns(cnsType, currentPrivateKey);
            // 根据群组和evidence Address获取hash
            String hash = AddressProcess.getHashForShare(groupId, evidenceAddress);
            // 将evidence地址注册到cns中 默认主群组
            RegisterAddressV2.registerAddress(
                cnsType, 
                hash, 
                evidenceAddress, 
                WeIdConstant.CNS_EVIDENCE_ADDRESS, 
                currentPrivateKey
            );
            // 将群组编号注册到cns中 默认主群组
            RegisterAddressV2.registerAddress(
                cnsType, 
                hash, 
                String.valueOf(groupId), 
                WeIdConstant.CNS_GROUP_ID, 
                currentPrivateKey
            );
            System.out.println("[RegisterEvidenceByGroup] register address into cns by group has successfully.");
            System.exit(0);
        } catch (Exception e) {
            logger.error(
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
