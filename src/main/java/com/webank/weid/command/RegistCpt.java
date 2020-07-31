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

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.dto.CptFile;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.BuildToolService;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019/4/11
 */
public class RegistCpt extends StaticConfig {

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(RegistCpt.class);

    private static BuildToolService buildToolService = new BuildToolService();

    private static WeIdService weIdService = new WeIdServiceImpl();

    /**
     * @param args 入参
     */
    public static void main(String[] args) {

        if (args == null || args.length < 6) {
            logger.error("[RegisterCpt] input parameters error, please check your input!");
            System.exit(1);
        }

        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);

        String weId = commandArgs.getWeid();
        
        ResponseData<Boolean> resp = weIdService.isWeIdExist(weId);
        if(!resp.getResult()) {
        	logger.error("[RegisterCpt] weid ---> {} does not exist.", weId);
        	System.out.println("[RegisterCpt] Error: the WeId ---> " + weId + " does not exist.");
            System.exit(1);
        }
        
        String cptDir = commandArgs.getCptDir();
        String privateKeyFile = commandArgs.getPrivateKey();
        String cptId = commandArgs.getCptId();
        String cptFile = commandArgs.getCptFile();
        WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
        weIdAuthentication.setWeId(weId);
        String osName = System.getProperty("os.name").toLowerCase();
        if(!privateKeyFile.startsWith("/") && !osName.contains("windows")) {
            //相对路径
            String temp = "tools/" + privateKeyFile;
            privateKeyFile = temp;
        }
        // 替换cnshash
        String mainHash = buildToolService.getMainHash();
        privateKeyFile = privateKeyFile.replace("{cns_contract_follow}", mainHash);
        String privateKey = FileUtils.readFile(privateKeyFile);
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey);
        weIdAuthentication.setWeIdPrivateKey(weIdPrivateKey);

        if (StringUtils.isNotEmpty(cptFile)) {
            File cptFile1 = new File(cptFile);
            try {
                registerCpt(cptFile1, weIdAuthentication, cptId);
            } catch (Exception e) {
                logger.error("register Cpt has error.", e);
                System.exit(1);
            }
        }

        if (StringUtils.isNotEmpty(cptDir)) {
        	if(!cptDir.startsWith("/")) {
        		//相对路径
        		String temp = "tools/" + cptDir;
        		cptDir = temp;
        	}
            File file = new File(cptDir);
            if (!file.isDirectory()) {
                logger.error("no CPT was found in dir :{}, please check your input.", file);
                System.out.println("[RegisterCpt] no CPT was found in dir :" + file + ", please check your input.");
                System.exit(1);
            }
            for (File f : file.listFiles()) {
                try {
                    registerCpt(f, weIdAuthentication, cptId);
                } catch (Exception e) {
                    logger.error("register Cpt has error.", e);
                    System.exit(1);
                }
            }
        }
        System.exit(0);
    }
    
    private static void registerCpt(
        File cptFile,
        WeIdAuthentication callerAuth, 
        String cptId) throws IOException {
        
        CptFile result = buildToolService.registerCpt(cptFile, callerAuth, cptId, DataFrom.COMMAND);
        System.out.print("[RegisterCpt] register cpt file:" + result.getCptFileName());
        if (BuildToolsConstant.SUCCESS.equals(result.getMessage())) {
            System.out.println("result ---> success. cpt id ---> " + result.getCptId());
        } else {
            System.out.println("result ---> fail. message ---> " + result.getMessage());
        }
    }
}
