

package com.webank.weid.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.blockchain.protocol.response.ResponseData;
import com.webank.weid.service.WeIdSdkService;

/**
 * @author tonychen 2019/4/11
 */
@Slf4j
public class RegistAuthorityIssuer extends StaticConfig {

    private static WeIdSdkService weIdSdkService = new WeIdSdkService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        if (args == null || args.length < 2) {
            log.error(
                "[RegisterAuthorityIssuer] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        //config file path
        String weid = commandArgs.getWeid();
        String orgId = commandArgs.getOrgId();
        String removedIssuer = commandArgs.getRemovedIssuer();
        if (StringUtils.isEmpty(weid) && StringUtils.isEmpty(removedIssuer)) {
            System.out.println("[RegisterAuthorityIssuer] Please input your issuer weid.");
            System.exit(1);
        }

        if (StringUtils.isNotEmpty(weid) && StringUtils.isNotEmpty(removedIssuer)) {
            System.out.println(
                "[RegisterAuthorityIssuer] issuer weid and removed issuer can not be both iuput.");
            System.exit(1);
        }
        ResponseData<Boolean> responseData = null;
        if (StringUtils.isNotEmpty(weid)) {
            System.out.println("registering authorityissuer ---> " + weid + ", name is :"+ orgId);
            String description = commandArgs.getDesc();
            responseData = weIdSdkService.registerIssuer(weid, orgId, description);
            if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                System.out.println("register authority issuer " + weid + " failed :" + responseData.getErrorMessage());
                log.error("register authority issuer " + weid + " failed :" + responseData.getErrorMessage());
                System.exit(1);
            }
        } else if (StringUtils.isNotEmpty(removedIssuer)) {
            System.out.println("removing authority issuer ---> " + removedIssuer + "...");
            responseData = weIdSdkService.removeIssuer(removedIssuer);
            if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                System.out.println("remove faild. result is : " + responseData.getErrorMessage());
                log.error("remove faild. result is : " + responseData.getErrorMessage());
                System.exit(1);
            }
            
        }
        System.exit(0);
    }

}
