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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.service.BuildToolService;

/**
 * @author tonychen 2019/4/11
 */
public class RegistAuthorityIssuer extends StaticConfig {


    private static final Logger logger = LoggerFactory.getLogger(RegistAuthorityIssuer.class);

    private static BuildToolService buildToolService = new BuildToolService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        if (args == null || args.length < 2) {
            logger.error(
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
        String result = null;
        if (StringUtils.isNotEmpty(weid)) {
            System.out.println("registering authorityissuer ---> " + weid + ", name is :"+ orgId);
            String description = commandArgs.getDesc();
            result = buildToolService.registerIssuer(weid, orgId, description, DataFrom.COMMAND);
            if (!BuildToolsConstant.SUCCESS.equals(result)) {
                System.out.println("register authority issuer " + weid + " failed :" + result);
                logger.error("register authority issuer " + weid + " failed :" + result);
                System.exit(1);
            }
        } else if (StringUtils.isNotEmpty(removedIssuer)) {
            System.out.println("removing authority issuer ---> " + removedIssuer + "...");
            result = buildToolService.removeIssuer(removedIssuer);
            if (!BuildToolsConstant.SUCCESS.equals(result)) {
                System.out.println("remove faild. result is : " + result);
                logger.error("remove faild. result is : " + result);
                System.exit(1);
            }
            
        }
        System.exit(0);
    }

}
