

package com.webank.weid.command;

import com.beust.jcommander.JCommander;
import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.service.ContractService;
import com.webank.weid.service.GuideService;
import com.webank.weid.util.WeIdSdkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class DeployEvidence extends StaticConfig {

    private static ContractService contractService = new ContractService();
    private static GuideService guideService = new GuideService();

    public static void main(String[] args) {
        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);
        String goupIdStr = commandArgs.getGroupId();
        if(StringUtils.isBlank(goupIdStr)) {
            System.out.println("[DeployEvidence] input error, the groupId is null. Abort.");
            System.exit(1);
        }
        //由于3.+的链默认群组名为group0 所以暂时丢弃该检查
//        if (!NumberUtils.isDigits(goupIdStr)) {
//            System.out.println("[DeployEvidence] input error, the groupId does not digits. Abort.");
//            System.exit(1);
//        }
        System.out.println("[DeployEvidence] begin deploy the evidence by groupId, groupId = " + goupIdStr);
        String groupId = goupIdStr;
        // 检查是否有admin账户，如果没有则创建admin账户
        String adminAddress = guideService.checkAdmin();
        if (StringUtils.isBlank(adminAddress)) {
            System.out.println("[DeployEvidence] begin create admin...");
            guideService.createAdmin(null);
        }
        FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
        String hash = contractService.deployEvidence(fiscoConfig, groupId, DataFrom.COMMAND);
        if (StringUtils.isNotBlank(hash)) {
            System.out.println("[DeployEvidence] the evidence deploy successfully, cns --> " + hash);
            System.exit(0);
        } else {
            System.out.println("[DeployEvidence] the evidence deploy fail， please check the log.");
            System.exit(1);
        }
        
    }

}
