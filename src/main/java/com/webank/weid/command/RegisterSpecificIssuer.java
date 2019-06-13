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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import com.beust.jcommander.JCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.AuthorityIssuerService;
import com.webank.weid.service.impl.AuthorityIssuerServiceImpl;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

/**
 * @author chaoxinhu 2019.5.26
 */
public class RegisterSpecificIssuer {

    private static final Logger logger = LoggerFactory.getLogger(RegisterSpecificIssuer.class);

    private static AuthorityIssuerService authorityIssuerService = new AuthorityIssuerServiceImpl();

    /**
     *
     */
    public static void main(String[] args) {

        if (args == null || args.length < 4) {
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
        String privateKeyFile = commandArgs.getPrivateKey();//privateKey
        System.out.println("private key file:" + privateKeyFile);
        String removedIssuer = commandArgs.getRemovedIssuer();

        if (StringUtils.isEmpty(type)) {
        	System.out.println("[RegisterIssuer] Please input your issued type.");
        	System.exit(1);
        }
        
        if(StringUtils.isEmpty(weid) && StringUtils.isEmpty(removedIssuer)) {
        	System.out.println("[RegisterIssuer] Please input your issuer weid.");
        	System.exit(1);
        }
        
        if(StringUtils.isNotEmpty(weid) && StringUtils.isNotEmpty(removedIssuer)) {
        	System.out.println("[RegisterIssuer] issuer weid and removed issuer can not be both iuput.");
        	System.exit(1);
        } 

        String privateKey = FileUtils.readFile(privateKeyFile);
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey);
        WeIdAuthentication callerAuth = new WeIdAuthentication();
        callerAuth.setWeIdPrivateKey(weIdPrivateKey);
        callerAuth.setWeId(WeIdUtils.convertPublicKeyToWeId(
            DataToolUtils.publicKeyFromPrivate(new BigInteger(privateKey)).toString()));

        // Register this issuer type anyway
        logger.info("[RegisterIssuer] registering issuer type: " + type);
        ResponseData<Boolean> responseData = authorityIssuerService
            .registerIssuerType(callerAuth, type);

        if (!StringUtils.isEmpty(weid)) {
            // Add the DIDs into this type
            List<String> weIdList = Arrays.asList(weid.split(";"));
            for (String weId : weIdList) {
                System.out.println("[RegisterIssuer] Adding WeIdentity DID " + weId + " in type: " + type);
                ResponseData<Boolean> response = authorityIssuerService
                    .addIssuerIntoIssuerType(callerAuth, type, weId);
                if (!response.getResult()) {
                    System.out.println(
                        "[RegisterIssuer] Add issuer into type FAILED: " + response.getErrorMessage());
                    System.exit(1);
                }
            }
        }
        
        if (!StringUtils.isEmpty(removedIssuer)) {
            // Remove the DIDs from this type
            List<String> weIdList = Arrays.asList(removedIssuer.split(";"));
            for (String weId : weIdList) {
                System.out.println("[RegisterIssuer] Removing WeIdentity DID " + weId + " from type: " + type);
                ResponseData<Boolean> response = authorityIssuerService
                    .removeIssuerFromIssuerType(callerAuth, type, weId);
                if (!response.getResult()) {
                    System.out.println(
                        "[RegisterIssuer] Remove issuer from type FAILED: " + response.getErrorMessage());
                    System.exit(1);
                }
            }
        }

        System.exit(0);
    }
}
