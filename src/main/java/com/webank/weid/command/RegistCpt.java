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
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.CptService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;

/**
 * @author tonychen 2019年4月11日
 *
 */
public class RegistCpt {

	 /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(RegistCpt.class);
	
	private static CptService cptService = new CptServiceImpl();
	
	/**
	 * @param args
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
    	
		String weid = commandArgs.getWeid();
		String cptDir = commandArgs.getCptDir();
		String privateKeyFile = commandArgs.getPrivateKey();
		String cptId = commandArgs.getCptId();
		String cptFile = commandArgs.getCptFile();
		
		WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
		weIdAuthentication.setWeId(weid);
		String privateKey = FileUtils.readFile(privateKeyFile);
		WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
		weIdPrivateKey.setPrivateKey(privateKey);
		weIdAuthentication.setWeIdPrivateKey(weIdPrivateKey);

		if(StringUtils.isNotEmpty(cptFile)) {
			File cptFile1 = new File(cptFile);
			registerCpt(cptFile1, cptId, weIdAuthentication);
		}
		
		if(StringUtils.isNotEmpty(cptDir)) {
			File file = new File(cptDir);

			if (!file.isDirectory()) {
				logger.error("failed.");
				System.exit(1);
			}
			for (File f : file.listFiles()) {
				registerCpt(f,cptId, weIdAuthentication);
			}
		}
		
		System.exit(0);
	}

	private static void registerCpt(File cptFile, String cptId, WeIdAuthentication weIdAuthentication) {
		JsonNode jsonNode;
		try {
			String fileName = cptFile.getName();
			if (!fileName.endsWith(".json")) {
				return;
			}
			System.out.println("[registerCpt] begin to register cpt file:"+fileName);
			jsonNode = JsonLoader.fromFile(cptFile);
			String cptJsonSchema = jsonNode.toString();
			CptStringArgs cptStringArgs = new CptStringArgs();
			cptStringArgs.setCptJsonSchema(cptJsonSchema);
			cptStringArgs.setWeIdAuthentication(weIdAuthentication);
			
			ResponseData<CptBaseInfo> response;
			if(StringUtils.isEmpty(cptId)) {
				response = cptService.registerCpt(cptStringArgs);
			}else {
				Integer cptId1 = Integer.valueOf(cptId);
				response = cptService.registerCpt(cptStringArgs, cptId1);
			}
			System.out.println("[RegisterCpt] result:" + DataToolUtils.serialize(response));
			if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
				logger.error("[RegisterCpt] load config faild. ErrorCode is:{}, msg :{}", response.getErrorCode(),
						response.getErrorMessage());
				System.out.println("[RegisterCpt] register cpt file:"+fileName+" failed, errorcode is "+ response.getErrorCode());
			}
			String content = new StringBuffer()
					.append(fileName)
					.append("=")
					.append(String.valueOf(response.getResult().getCptId()))
					.append("\r\n")
					.toString();
			FileUtils.writeToFile(content, "regist_cpt.out", FileOperator.APPEND);
			System.out.println("[RegisterCpt] register cpt file:"+fileName+" successfully, errorcode is \"");
		} catch (IOException e) {
			logger.error("[RegisterCpt] load config faild. ", e);
			System.exit(1);
		}
	}
	
}
