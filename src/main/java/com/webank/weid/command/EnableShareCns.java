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

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.service.DeployService;

public class EnableShareCns extends StaticConfig {
    
    private static DeployService deployService = new DeployService();
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);
        String hash = commandArgs.getCns();
        if(StringUtils.isBlank(hash)) {
            System.out.println("[EnableShareCns] input error, the cns is null. Abort.");
            System.exit(1);
        }
        System.out.println("[EnableShareCns] beign enable new cns for evidence, cns = " + hash);
        String restult = deployService.enableShareCns(hash);
        if (restult.equals("success")) {
            System.out.println("[EnableShareCns] new cns enable successfully."); 
            System.exit(0);
        } else {
            System.out.println("[EnableShareCns] new cns enable fail, please check the log.");
            System.exit(1);
        }
    }

}
