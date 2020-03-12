/*
 *       Copyright© (2019) WeBank Co., Ltd.
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.service.BuildToolService;

/**
 * @author chaoxinhu 2019.5.26
 */
public class RegisterSpecificIssuer {

    private static final Logger logger = LoggerFactory.getLogger(RegisterSpecificIssuer.class);

    private static BuildToolService buildToolService = new BuildToolService();

    /**
     *
     */
    public static void main(String[] args) {

        if (args == null || args.length < 2) {
            logger.error("[RegisterIssuer] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        //config file path
        String weid = commandArgs.getWeid();
        String type = commandArgs.getType();
        String removedIssuer = commandArgs.getRemovedIssuer();

        if (StringUtils.isEmpty(type)) {
        	System.out.println("[RegisterIssuer] Failed to load issued type. Abort.");
            logger.error("[RegisterIssuer] Failed to load issued type. Abort.");
        	System.exit(1);
        }
        
        if(StringUtils.isEmpty(weid) && StringUtils.isEmpty(removedIssuer)) {
        	System.out.println("[RegisterIssuer] Failed to load issuer weid or removed issuer. Abort.");
            logger.error("[RegisterIssuer] Failed to load issuer weid or removed issuer. Abort.");
        	System.exit(1);
        }
        
        if(StringUtils.isNotEmpty(weid) && StringUtils.isNotEmpty(removedIssuer)) {
        	System.out.println("[RegisterIssuer] issuer weid and removed issuer can not both exist. Abort.");
            logger.error("[RegisterIssuer] issuer weid and removed issuer can not both exist. Abort.");
        	System.exit(1);
        } 

        // Register this issuer type anyway
        logger.info("[RegisterIssuer] Registering issuer type with best effort: " + type);
        String message = buildToolService.registerIssuerType(type, DataFrom.COMMAND);

        if (!StringUtils.isEmpty(weid)) {
            // Add the DIDs into this type
            List<String> weIdList = Arrays.asList(weid.split(","));
            for (String weId : weIdList) {
                System.out.println("[RegisterIssuer] Adding WeIdentity DID " + weId + " in type: " + type);
                logger.info("[RegisterIssuer] Adding WeIdentity DID " + weId + " in type: " + type);
                message = buildToolService.addIssuerIntoIssuerType(type, weId);
                if (!BuildToolsConstant.SUCCESS.equals(message)) {
                    System.out.println("[RegisterIssuer] Add FAILED: " + message);
                    logger.error("[RegisterIssuer] Add FAILED: " + message);
                    System.exit(1);
                }
            }
        }
        
        if (!StringUtils.isEmpty(removedIssuer)) {
            // Remove the DIDs from this type
            List<String> weIdList = Arrays.asList(removedIssuer.split(","));
            for (String weId : weIdList) {
                System.out.println("[RegisterIssuer] Removing WeIdentity DID " + weId + " from type: " + type);
                logger.info("[RegisterIssuer] Removing WeIdentity DID " + weId + " from type: " + type);
                message = buildToolService.removeIssuerFromIssuerType(type, weId);
                if (!BuildToolsConstant.SUCCESS.equals(message)) {
                    System.out.println("[RegisterIssuer] Remove FAILED: " + message);
                    logger.error("[RegisterIssuer] Remove FAILED: " + message);
                    System.exit(1);
                }
            }
        }

        System.exit(0);
    }
}
