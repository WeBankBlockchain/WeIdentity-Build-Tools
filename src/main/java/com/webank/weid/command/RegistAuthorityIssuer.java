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
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.RegisterAuthorityIssuerArgs;
import com.webank.weid.protocol.request.RemoveAuthorityIssuerArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.AuthorityIssuerService;
import com.webank.weid.service.impl.AuthorityIssuerServiceImpl;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019/4/11
 */
public class RegistAuthorityIssuer {


    private static final Logger logger = LoggerFactory.getLogger(RegistAuthorityIssuer.class);

    private static AuthorityIssuerService authorityIssuerService = new AuthorityIssuerServiceImpl();

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args == null || args.length < 4) {
            logger.error("[RegisterAuthorityIssuer] input parameters error, please check your input!");
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
        String privateKeyFile = commandArgs.getPrivateKey();//privateKey
        String removedIssuer = commandArgs.getRemovedIssuer();
        if(StringUtils.isEmpty(weid) && StringUtils.isEmpty(removedIssuer)) {
        	System.out.println("[RegisterAuthorityIssuer] Please input your issuer weid.");
        	System.exit(1);
        }
        
        if(StringUtils.isNotEmpty(weid) && StringUtils.isNotEmpty(removedIssuer)) {
        	System.out.println("[RegisterAuthorityIssuer] issuer weid and removed issuer can not be both iuput.");
        	System.exit(1);
        } 
        
        System.out.println("private key file:" + privateKeyFile);

        String privateKey = FileUtils.readFile(privateKeyFile);
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey);
        
        System.out.println(
            "[RegisterAuthorityIssuer] registering authorityissuer:" + weid + ", name is :" + orgId);
        if(StringUtils.isNotEmpty(weid)) {
        	RegisterAuthorityIssuerArgs registerAuthorityIssuerArgs = new RegisterAuthorityIssuerArgs();
            AuthorityIssuer authorityIssuer = new AuthorityIssuer();
            authorityIssuer.setName(orgId);
            authorityIssuer.setWeId(weid);
            authorityIssuer.setAccValue("1");
            authorityIssuer.setCreated(System.currentTimeMillis());
            registerAuthorityIssuerArgs.setAuthorityIssuer(authorityIssuer);

            
            registerAuthorityIssuerArgs.setWeIdPrivateKey(weIdPrivateKey);

            ResponseData<Boolean> response = authorityIssuerService
                .registerAuthorityIssuer(registerAuthorityIssuerArgs);
            if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
                logger.error(
                    "[RegisterAuthorityIssuer] register wauthority issuer {} failed. error code : {}, error msg :{}",
                    weid,
                    response.getErrorCode(), 
                    response.getErrorMessage()
                    );
                System.out.println("[RegisterAuthorityIssuer] register authority issuer result ---> faild.");
                System.exit(1);
            }
            else {
            	logger.info(
                        "[RegisterAuthorityIssuer] register wauthority issuer {} success.", 
                        weid
                        );
            	System.out.println("[RegisterAuthorityIssuer] register authority issuer result ----> success.");
            }
        }
        
        if(StringUtils.isNotEmpty(removedIssuer)) {
        	RemoveAuthorityIssuerArgs removeAuthorityIssuerArgs = new RemoveAuthorityIssuerArgs();
        	removeAuthorityIssuerArgs.setWeId(removedIssuer);
        	removeAuthorityIssuerArgs.setWeIdPrivateKey(weIdPrivateKey);
        	
        	 ResponseData<Boolean> response = authorityIssuerService.removeAuthorityIssuer(removeAuthorityIssuerArgs);
                     
                 if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
                     logger.error(
                         "[RegisterAuthorityIssuer] remove authority issuer {} faild. error code : {}, error msg :{}",
                         removedIssuer, 
                         response.getErrorCode(), 
                         response.getErrorMessage()
                         );
                     System.out.println("[RegisterAuthorityIssuer] remove faild. result is : " + response);
                     System.exit(1);
                 }
                 System.out.println("[RegisterAuthorityIssuer] remove authority issuer with success. result is : " + response);
                 System.exit(0);
        	
        }
        System.exit(0);
        
    }

}
