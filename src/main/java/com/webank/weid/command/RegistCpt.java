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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.CptService;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.PropertyUtils;

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

		if (args == null || args.length < 3) {
			logger.error("[RegistCpt] input parameters error, please check your input!");
			System.exit(1);
		}

		String config = args[0];
		String cptDir = args[1];
		String privateKeyFile = args[2];
		File file = new File(cptDir);

		try {
			PropertyUtils.loadProperties(config);

		} catch (IOException e) {
			logger.error("[RegistCpt] load config faild. ", e);
			System.exit(1);
		}

		if (!file.isDirectory()) {
			logger.error("failed.");
			System.exit(1);
		}

		String weId = PropertyUtils.getProperty("weId");
//		String weId = FileUtils.readFile("weId");
		WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
		weIdAuthentication.setWeId(weId);
		String privateKey = FileUtils.readFile(privateKeyFile);
		WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
		weIdPrivateKey.setPrivateKey(privateKey);
		weIdAuthentication.setWeIdPrivateKey(weIdPrivateKey);

		for (File f : file.listFiles()) {
			JsonNode jsonNode;
			try {
				String fileName = f.getName();
				if (!fileName.endsWith(".json")) {
					continue;
				}
				jsonNode = JsonLoader.fromFile(f);
				String cptJsonSchema = jsonNode.toString();
				CptStringArgs cptStringArgs = new CptStringArgs();
				cptStringArgs.setCptJsonSchema(cptJsonSchema);
				cptStringArgs.setWeIdAuthentication(weIdAuthentication);
				ResponseData<CptBaseInfo> response = cptService.registerCpt(cptStringArgs);
				if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
					logger.error("[RegistCpt] load config faild. ErrorCode is:{}, msg :{}", response.getErrorCode(),
							response.getErrorMessage());
					System.exit(1);
				}
			} catch (IOException e) {
				logger.error("[RegistCpt] load config faild. ", e);
				System.exit(1);
			}

		}
		System.exit(0);

	}

}
