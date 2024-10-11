package com.webank.weid.service;

import com.webank.weid.blockchain.service.fisco.CryptoFisco;
import com.webank.weid.util.DataToolUtils;
import java.io.File;
import java.math.BigInteger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.utils.Numeric;
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
		CryptoKeyPair credentials;
		if (StringUtils.isNotBlank(inputPrivateKey)) {
			log.info("[createAdmin] create by private key.");
			credentials = CryptoFisco.cryptoSuite.getKeyPairFactory().createKeyPair(new BigInteger(inputPrivateKey));
		} else {
			log.info("[createAdmin] create by default.");
			credentials = CryptoFisco.cryptoSuite.getKeyPairFactory().generateKeyPair();
		}
		String privateKey = DataToolUtils.hexStr2DecStr(credentials.getHexPrivateKey());
		String publicKey = DataToolUtils.hexStr2DecStr(credentials.getHexPublicKey());
		File ecdsaFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ADMIN_KEY);
		FileUtils.writeToFile(privateKey, ecdsaFile.getAbsolutePath());
		File ecdsaPubFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ADMIN_PUB_KEY);
		FileUtils.writeToFile(publicKey, ecdsaPubFile.getAbsolutePath());
		log.info("[createAdmin] the admin create successfully.");
		return DataToolUtils.addressFromPublic(new BigInteger(publicKey));
	}

	public String checkAdmin() {
		File ecdsaFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ADMIN_KEY);
		File ecdsaPubFile = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ADMIN_PUB_KEY);
		if (!ecdsaFile.exists() || !ecdsaPubFile.exists()) {
			return StringUtils.EMPTY;
		}
		String publicKey = FileUtils.readFile(ecdsaPubFile.getAbsolutePath());
		return DataToolUtils.addressFromPublic(new BigInteger(publicKey));
	}

}
