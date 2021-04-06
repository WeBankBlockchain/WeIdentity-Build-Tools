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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.FiscoConfig;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.service.ContractService;
import com.webank.weid.service.GuideService;
import com.webank.weid.util.WeIdSdkUtils;

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
        if (!NumberUtils.isDigits(goupIdStr)) {
            System.out.println("[DeployEvidence] input error, the groupId does not digits. Abort.");
            System.exit(1);
        }
        System.out.println("[DeployEvidence] begin deploy the evidence by groupId, groupId = " + goupIdStr);
        Integer groupId = Integer.parseInt(goupIdStr);
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
            System.out.println("[DeployEvidence] the evidence deploy faile， please check the log.");
            System.exit(1);
        }
        
    }

}
