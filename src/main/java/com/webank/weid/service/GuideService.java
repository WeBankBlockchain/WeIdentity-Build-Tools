package com.webank.weid.service;

import java.io.File;
import java.math.BigInteger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.springframework.stereotype.Service;

import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.util.FileUtils;

@Service
@Slf4j
public class GuideService {

	public boolean setGuideStatus(String step) {
		File guideFile = new File(BuildToolsConstant.OTHER_PATH, BuildToolsConstant.GUIDE_FILE);
		FileUtils.writeToFile(step, guideFile.getAbsolutePath(), FileOperator.OVERWRITE);
		return true;
	}

	public ResponseData<String> getGuideStatus() {
		File guideFile = new File(BuildToolsConstant.OTHER_PATH, BuildToolsConstant.GUIDE_FILE);
		if (!guideFile.exists()) {
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.UNKNOW_ERROR.getCode(), "guide file not exists");
		}
		return new ResponseData<>(FileUtils.readFile(guideFile.getAbsolutePath()), ErrorCode.SUCCESS);
	}

	public boolean setRoleType(String roleType) {
		File roleFile = new File(BuildToolsConstant.OTHER_PATH, BuildToolsConstant.ROLE_FILE);
		FileUtils.writeToFile(roleType, roleFile.getAbsolutePath(), FileOperator.OVERWRITE);
		return true;
	}

	public String createAdmin(String inputPrivateKey) {
		log.info("[createAdmin] begin create admin.");
		Credentials credentials;
		if (StringUtils.isNotBlank(inputPrivateKey)) {
			log.info("[createAdmin] create by private key.");
			credentials = GenCredential.create(new BigInteger(inputPrivateKey).toString(16));
		} else {
			log.info("[createAdmin] create by default.");
			credentials = GenCredential.create();
		}
		String privateKey = credentials.getEcKeyPair().getPrivateKey().toString();
		String publicKey = credentials.getEcKeyPair().getPublicKey().toString();
		File ecdsaFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ECDSA_KEY);
		FileUtils.writeToFile(privateKey, ecdsaFile.getAbsolutePath());
		File ecdsaPubFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ECDSA_PUB_KEY);
		FileUtils.writeToFile(publicKey, ecdsaPubFile.getAbsolutePath());
		log.info("[createAdmin] the admin create successfully.");
		return "0x" + Keys.getAddress(new BigInteger(publicKey));
	}

	public String checkAdmin() {
		File ecdsaFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ECDSA_KEY);
		File ecdsaPubFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ECDSA_PUB_KEY);
		if (!ecdsaFile.exists() || !ecdsaPubFile.exists()) {
			return StringUtils.EMPTY;
		}
		String publicKey = FileUtils.readFile(ecdsaPubFile.getAbsolutePath());
		return "0x" + Keys.getAddress(new BigInteger(publicKey));
	}

}
