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
        if(StringUtils.isNotEmpty(hash)) {
            System.out.println("[EnableShareCns] input error, the cns is null. Abort.");
            System.exit(1);
        }
        System.out.println("[EnableShareCns] beign enable new cns for evidence, cns = " + hash);
        boolean restult = deployService.enableShareCns(hash);
        if (restult) {
            System.out.println("[EnableShareCns] new cns enable successfully."); 
            System.exit(0);
        } else {
            System.out.println("[EnableShareCns] new cns enable fail, please check the log.");
            System.exit(1);
        }
    }

}
