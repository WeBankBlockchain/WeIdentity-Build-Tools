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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.WeIdSdkService;

/**
 * @author chaoxinhu 2019.5.26
 */
@Slf4j
public class RegisterSpecificIssuer extends StaticConfig {
    
    private static WeIdSdkService weIdSdkService = new WeIdSdkService();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        if (args == null || args.length < 2) {
            log.error("[RegisterIssuer] input parameters error, please check your input!");
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
            log.error("[RegisterIssuer] Failed to load issued type. Abort.");
        	System.exit(1);
        }
        
        if(StringUtils.isEmpty(weid) && StringUtils.isEmpty(removedIssuer)) {
        	System.out.println("[RegisterIssuer] Failed to load issuer weid or removed issuer. Abort.");
            log.error("[RegisterIssuer] Failed to load issuer weid or removed issuer. Abort.");
        	System.exit(1);
        }
        
        if(StringUtils.isNotEmpty(weid) && StringUtils.isNotEmpty(removedIssuer)) {
        	System.out.println("[RegisterIssuer] issuer weid and removed issuer can not both exist. Abort.");
            log.error("[RegisterIssuer] issuer weid and removed issuer can not both exist. Abort.");
        	System.exit(1);
        } 
        // Register this issuer type anyway
        log.info("[RegisterIssuer] Registering issuer type with best effort: " + type);
        ResponseData<Boolean> responseData = weIdSdkService.registerIssuerType(type, DataFrom.COMMAND);
        if (!StringUtils.isEmpty(weid)) {
            // Add the DIDs into this type
            List<String> weIdList = Arrays.asList(weid.split(","));
            for (String weId : weIdList) {
                System.out.println("[RegisterIssuer] Adding WeIdentity DID " + weId + " in type: " + type);
                log.info("[RegisterIssuer] Adding WeIdentity DID " + weId + " in type: " + type);
                responseData = weIdSdkService.addIssuerIntoIssuerType(type, weId);
                if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                    System.out.println("[RegisterIssuer] Add FAILED: " + responseData.getErrorMessage());
                    log.error("[RegisterIssuer] Add FAILED: " + responseData.getErrorMessage());
                    System.exit(1);
                }
                System.out.println("[RegisterIssuer] Specific issuers and types have been successfully registered on blockchain.");
            }
        }
        
        if (!StringUtils.isEmpty(removedIssuer)) {
            // Remove the DIDs from this type
            List<String> weIdList = Arrays.asList(removedIssuer.split(","));
            for (String weId : weIdList) {
                System.out.println("[RemoveIssuer] Removing WeIdentity DID " + weId + " from type: " + type);
                log.info("[RemoveIssuer] Removing WeIdentity DID " + weId + " from type: " + type);
                responseData = weIdSdkService.removeIssuerFromIssuerType(type, weId);
                if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                    System.out.println("[RegisterIssuer] Remove FAILED: " + responseData.getErrorMessage());
                    log.error("[RemoveIssuer] Remove FAILED: " + responseData.getErrorMessage());
                    System.exit(1);
                }
                System.out.println("[RemoveIssuer] Removing WeIdentity DID successfully.");
            }
        }

        System.exit(0);
    }
}
