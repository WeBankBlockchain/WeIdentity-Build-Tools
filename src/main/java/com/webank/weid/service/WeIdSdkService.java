package com.webank.weid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JCodeModel;
import com.webank.weid.blockchain.service.fisco.BaseServiceFisco;
import com.webank.weid.constant.*;
import com.webank.weid.dto.*;
import com.webank.weid.protocol.base.*;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.protocol.request.RegisterAuthorityIssuerArgs;
import com.webank.weid.protocol.request.RemoveAuthorityIssuerArgs;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.rpc.AuthorityIssuerService;
import com.webank.weid.service.rpc.CptService;
import com.webank.weid.service.rpc.PolicyService;
import com.webank.weid.service.rpc.WeIdService;
import com.webank.weid.blockchain.service.fisco.server.WeServerUtils;
import com.webank.weid.blockchain.constant.CnsType;
import com.webank.weid.blockchain.protocol.base.HashContract;
import com.webank.weid.service.impl.AuthorityIssuerServiceImpl;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.PolicyServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeIdSdkService {

	private WeIdService weIdService;
	private AuthorityIssuerService authorityIssuerService;
	private CptService cptService;
	private PolicyService policyService;

	private PolicyService getPolicyService() {
		if (policyService == null) {
			policyService = new PolicyServiceImpl();
		}
		return policyService;
	}

	private WeIdService getWeIdService() {
		if (weIdService == null) {
			weIdService = new WeIdServiceImpl();
		}
		return weIdService;
	}

	private AuthorityIssuerService getAuthorityIssuerService() {
		if (authorityIssuerService == null) {
			authorityIssuerService = new AuthorityIssuerServiceImpl();
		}
		return authorityIssuerService;
	}

	private CptService getCptService() {
		if (cptService == null) {
			cptService = new CptServiceImpl();
		}
		return cptService;
	}

	private ConfigService configService = new ConfigService();
	
    private WeBaseService weBaseService = new WeBaseService();

	public ResponseData<DataPanel> getDataPanel() {
		DataPanel data = new DataPanel();
		try {
			data.setBlockNumber(BaseServiceFisco.getBlockNumber());
			data.setWeIdCount(getWeIdService().getWeIdCount().getResult());
			data.setCptCount(getCptService().getCptCount().getResult());
			data.setPolicyCount(getPolicyService().getPolicyCount().getResult());
			data.setIssuerCount(getAuthorityIssuerService().getIssuerCount().getResult());
			data.setCredentialCount(20);
		} catch (IOException e) {
			log.error("get data panel failed. ", e);
			return new ResponseData<>(null, ErrorCode.BASE_ERROR);
		}

		return new ResponseData<>(data, ErrorCode.SUCCESS);
	}

	public ResponseData<PageDto<WeIdInfo>> getWeIdList(
			PageDto<WeIdInfo> pageDto,
			Integer pageSize,
			Integer indexFirst
	) {
		Integer total = getWeIdService().getWeIdCount().getResult();
		Integer indexLast = indexFirst+pageSize-1;
		if(indexFirst+pageSize > total) {indexLast = total-1;}
		ResponseData<List<String>> response = getWeIdService().getWeIdList(indexFirst, indexLast);
		if (response.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
			log.error("[getWeIdList] get weIdList has error, {} - {}", response.getErrorCode(), response.getErrorMessage());
			return new ResponseData<>(pageDto, response.getErrorCode(), response.getErrorMessage());
		}
		List<String> list = response.getResult();
		pageDto.setAllCount(getWeIdService().getWeIdCount().getResult());
		List<WeIdInfo> rList = new ArrayList<>();
		if (list.size() > 0) {
			String mainHash = WeIdSdkUtils.getMainHash();
			for (String weId : list) {
				WeIdInfo weInfo = getWeIdInfo(this.getWeIdAddress(weId));
				if(weInfo == null) {
					weInfo = new WeIdInfo();
				}
				weInfo.setWeId(weId);
				AuthorityIssuer issuer = getAuthorityIssuerService().queryAuthorityIssuerInfo(weId).getResult();
				weInfo.setIssuer(issuer != null);
				weInfo.setHash(mainHash);
				rList.add(weInfo);
			}
		}
		pageDto.setDataList(rList);
		return new ResponseData<>(pageDto, ErrorCode.SUCCESS);
	}

	public WeIdInfo getWeIdInfo(String address) {
		File targetDir = getWeidDir(address);
		File weIdFile = new File(targetDir.getAbsoluteFile(), "info");
		String jsonData = FileUtils.readFile(weIdFile.getAbsolutePath());
		if (StringUtils.isBlank(jsonData)) {
			return null;
		}
		return DataToolUtils.deserialize(jsonData, WeIdInfo.class);
	}

	public ResponseData<String> createWeId(DataFrom from) {
		ResponseData<CreateWeIdDataResult> response = getWeIdService().createWeId();
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error(
					"[CreateWeId] create WeID faild. error code : {}, error msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return new ResponseData<>(StringUtils.EMPTY, response.getErrorCode(), response.getErrorMessage());
		}
		CreateWeIdDataResult result = response.getResult();
		String weId = result.getWeId();
		//System.out.println("weid is ------> " + weId);
		String publicKey = result.getUserWeIdPublicKey().getPublicKey();
		String privateKey = result.getUserWeIdPrivateKey().getPrivateKey();
		saveWeId(weId, publicKey, privateKey, from, false);
		// 向WeBase导入私钥
		importPrivateKeyToWeBase(weId, privateKey);
		return new ResponseData<>(weId, ErrorCode.SUCCESS);
	}

	/**
	 * 部署admin的weid
	 * @param arg 创建weId的参数
	 * @param from 数据来源
	 * @param isAdmin 是否为管理员
	 * @return 返回创建weId结果
	 */
	public ResponseData<String> createWeId(CreateWeIdArgs arg, DataFrom from, boolean isAdmin) {
		ResponseData<String> response = getWeIdService().createWeId(arg);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error(
					"[CreateWeId] create WeID faild. error code : {}, error msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return response;
		}

		String weId = response.getResult();
		saveWeId(weId, arg.getPublicKey(), arg.getWeIdPrivateKey().getPrivateKey(), from, isAdmin);
		return response;
	}

	// 根据私钥创建weId
	public ResponseData<String> createWeIdByPrivateKey(HttpServletRequest request, DataFrom from) {
		String privateKey;
		try {
			MultipartFile file = ((MultipartHttpServletRequest) request).getFile("privateKey");
			privateKey = new String(file.getBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("get private key failed.", e);
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.BASE_ERROR);
		}

		CreateWeIdArgs arg = new CreateWeIdArgs();
		WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
		weIdPrivateKey.setPrivateKey(privateKey);
		arg.setWeIdPrivateKey(weIdPrivateKey);
		arg.setPublicKey(DataToolUtils.publicKeyFromPrivate(new BigInteger(privateKey)).toString());
		ResponseData<String> response = this.createWeId(arg, from, false);
		if (response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
		    // 向WeBase导入私钥
	        importPrivateKeyToWeBase(response.getResult(), privateKey);
        }
		return response;
	}

	// 根据公钥代理创建weId(只有主群组管理员才可以调用)
	public ResponseData<String> createWeIdByPublicKey(HttpServletRequest request, DataFrom from) {
		String publicKey;
		try {
			MultipartFile file = ((MultipartHttpServletRequest) request).getFile("publicKey");
			publicKey = new String(file.getBytes(),StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("get public key failed.", e);
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.BASE_ERROR);
		}

		// 判断当前使用的cns是否为当前admin部署的
		// 获取当前配置的hash值
		String hash = WeIdSdkUtils.getMainHash();
		// 获取所有的主合约cns值，从而获取当前cns的部署者
		List<HashContract> result = WeIdSdkUtils.getDataBucket(CnsType.DEFAULT).getAllBucket().getResult();
		// 当前hash的所有者
		String currentHashOwner =  null;
		for (HashContract hashContract : result) {
			if (hashContract.getHash().equals(hash)) {
				currentHashOwner = hashContract.getOwner();
				break;
			}
		}
		// 转换成weid
		String owner = WeIdUtils.convertAddressToWeId(currentHashOwner);
		WeIdAuthentication currentWeIdAuth = getCurrentWeIdAuth();
		// 如果当前weId地址跟 owner地址一致说明是主群组管理员
		if (currentWeIdAuth.getWeId().equals(owner)) {
			WeIdPublicKey weidPublicKey = new WeIdPublicKey();
			weidPublicKey.setPublicKey(publicKey);
			ResponseData<String> response = getWeIdService().createWeIdByPublicKey(weidPublicKey, currentWeIdAuth.getWeIdPrivateKey());
			if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
				log.error(
						"[CreateWeId] create WeID faild. error code : {}, error msg :{}",
						response.getErrorCode(),
						response.getErrorMessage());
				return new ResponseData<>(StringUtils.EMPTY, response.getErrorCode(), response.getErrorMessage());
			}
			String weId = response.getResult();
			// 本地保存WeId
			saveWeId(weId, publicKey, null, from, false);
			// 导入公钥到WeBase
			importPublicKeyToWeBase(weId, publicKey);
			return new ResponseData<>(weId, ErrorCode.SUCCESS);
		}
		return new ResponseData<>(StringUtils.EMPTY, ErrorCode.BASE_ERROR.getCode(),
				"create fail: no permission.");
	}

	/**
	 * 检查weid是否存在
	 * @param weId 需要被检查的WeId
	 * @return 返回weId是否存在
	 */
	public boolean checkWeId(String weId) {
		ResponseData<Boolean> response = getWeIdService().isWeIdExist(weId);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error(
					"[checkWeId] check the WeID faild. error code : {}, error msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return false;
		}
		return response.getResult();
	}

	private void saveWeId(
			String weId,
			String publicKey,
			String privateKey,
			DataFrom from,
			boolean isAdmin
	) {
		//write weId, publicKey and privateKey to output dir
		File targetDir = getWeidDir(getWeIdAddress(weId));
		if (from == DataFrom.COMMAND) {
			FileUtils.writeToFile(weId, BuildToolsConstant.WEID_FILE); //适配命令输出
		}
		FileUtils.writeToFile(weId, new File(targetDir, BuildToolsConstant.WEID_FILE).getAbsolutePath());
		FileUtils.writeToFile(publicKey, new File(targetDir, BuildToolsConstant.ADMIN_PUB_KEY).getAbsolutePath());
		if (StringUtils.isNotBlank(privateKey)) {
			FileUtils.writeToFile(privateKey, new File(targetDir, BuildToolsConstant.ADMIN_KEY).getAbsolutePath());
		}
		saveWeIdInfo(weId, publicKey, privateKey, from, isAdmin);
	}

	public File getWeidDir(String address) {
		address = FileUtils.getSecurityFileName(address);
		return new File(BuildToolsConstant.WEID_PATH + "/" + WeIdSdkUtils.getMainHash() + "/" + address);
	}

	public ResponseData<String> getWeidDir() {
		String mainHash = WeIdSdkUtils.getMainHash();
		if (StringUtils.isBlank(mainHash)) {
			log.error("mainHash is empty");
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.BASE_ERROR);
		}

		return new ResponseData<>(new File(BuildToolsConstant.WEID_PATH + "/" + mainHash).getAbsolutePath(),
				ErrorCode.SUCCESS);
	}

	private void saveWeIdInfo(
			String weId,
			String publicKey,
			String privateKey,
			DataFrom from,
			boolean isAdmin
	) {
		log.info("[saveWeIdInfo] begin to save weid info...");
		//创建部署目录
		File targetDir = getWeidDir(getWeIdAddress(weId));
		File saveFile = new File(targetDir.getAbsoluteFile(), "info");
		FileUtils.writeToFile(
				buildInfo(weId, publicKey, privateKey, from, isAdmin),
				saveFile.getAbsolutePath(),
				FileOperator.OVERWRITE);
		log.info("[saveWeIdInfo] save the weid info successfully.");
	}

	private String buildInfo(
			String weId,
			String publicKey,
			String privateKey,
			DataFrom from,
			boolean isAdmin
	) {
		WeIdInfo info = new WeIdInfo();
		info.setId(getWeIdAddress(weId));
		long time = System.currentTimeMillis();
		info.setTime(time);
		info.setEcdsaKey(privateKey);
		info.setEcdsaPubKey(publicKey);
		info.setWeId(weId);
		info.setHash(WeIdSdkUtils.getMainHash());
		info.setFrom(from.name());
		info.setAdmin(isAdmin);
		return DataToolUtils.serialize(info);
	}

	/**
	 * 注册issuer.
	 * @param weId 被注册成issuer的WeId
	 * @param name 注册issuer名
	 * @param description 注册issuer备注
	 * @return 返回注册结果，true表示成功，false表示失败
	 */
	public ResponseData<Boolean> registerIssuer(String weId, String name, String description) {
		log.info("[registerIssuer] begin register authority issuer..., weId={}, name={}, description={}", weId, name, description);
		RegisterAuthorityIssuerArgs registerAuthorityIssuerArgs = new RegisterAuthorityIssuerArgs();
		AuthorityIssuer authorityIssuer = new AuthorityIssuer();
		authorityIssuer.setName(name);
		authorityIssuer.setWeId(weId);
		authorityIssuer.setAccValue("1");
		authorityIssuer.setCreated(System.currentTimeMillis());
		authorityIssuer.setDescription(description);
		registerAuthorityIssuerArgs.setAuthorityIssuer(authorityIssuer);
		String hash = WeIdSdkUtils.getMainHash();
		registerAuthorityIssuerArgs.setWeIdPrivateKey(WeIdSdkUtils.getWeIdPrivateKey(hash));
		return getAuthorityIssuerService().registerAuthorityIssuer(registerAuthorityIssuerArgs);
	}

	/**
	 * 认证issuer.
	 * @param weId 需要认证的weId
	 * @return 返回认证结果
	 */
	public ResponseData<Boolean> recognizeAuthorityIssuer(String weId) {
		WeIdPrivateKey weIdPrivateKey = WeIdSdkUtils.getWeIdPrivateKey(WeIdSdkUtils.getMainHash());
		return getAuthorityIssuerService().recognizeAuthorityIssuer(weId, weIdPrivateKey);
	}

	/**
	 * 撤销认证issuer.
	 * @param weId 需要认证的weId
	 * @return 返回认证结果
	 */
	public ResponseData<Boolean> deRecognizeAuthorityIssuer(String weId) {
		WeIdPrivateKey weIdPrivateKey = WeIdSdkUtils.getWeIdPrivateKey(WeIdSdkUtils.getMainHash());
		return getAuthorityIssuerService().deRecognizeAuthorityIssuer(weId, weIdPrivateKey);
	}

	public ResponseData<PageDto<Issuer>> getIssuerList(PageDto<Issuer> pageDto) {
		String hash = WeIdSdkUtils.getMainHash();
		ResponseData<List<AuthorityIssuer>> response =
				getAuthorityIssuerService().getAllAuthorityIssuerList(pageDto.getStartIndex(), pageDto.getPageSize());
		List<Issuer> list = new ArrayList<>();
		if (response.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
			if (CollectionUtils.isNotEmpty(response.getResult())) {
				response.getResult().forEach(authorityIssuer -> {
					Issuer issuer = new Issuer();
					issuer.setWeId(authorityIssuer.getWeId());
					issuer.setName(authorityIssuer.getName());
					issuer.setCreateTime(String.valueOf(authorityIssuer.getCreated()));
					issuer.setHash(hash);
					issuer.setDescription(authorityIssuer.getDescription());
					issuer.setRecognized(authorityIssuer.isRecognized());
					list.add(issuer);
				});
			}
		} else {
			log.warn("[getIssuerList] query issuerList from chain fail: {} - {}.", response.getErrorCode(), response.getErrorMessage());
			return new ResponseData<>(null, response.getErrorCode(),  response.getErrorMessage());
		}
		Integer allCount = getAuthorityIssuerService().getIssuerCount().getResult();
		pageDto.setAllCount(allCount);
		pageDto.setDataList(list);
		return new ResponseData<>(pageDto, ErrorCode.SUCCESS);
	}

	public ResponseData<Boolean> removeIssuer(String weId) {
		log.info("[removeIssuer] begin remove authority issuer={} ...", weId);
		RemoveAuthorityIssuerArgs removeAuthorityIssuerArgs = new RemoveAuthorityIssuerArgs();
		removeAuthorityIssuerArgs.setWeId(weId);
		String hash = WeIdSdkUtils.getMainHash();
		removeAuthorityIssuerArgs.setWeIdPrivateKey(WeIdSdkUtils.getWeIdPrivateKey(hash));

		return getAuthorityIssuerService().removeAuthorityIssuer(removeAuthorityIssuerArgs);
	}

	public ResponseData<Boolean> registerIssuerType(String type, DataFrom from) {
		log.info("[registerIssuerType] Registering issuer type with best effort: " + type);
		ResponseData<Boolean> response = getAuthorityIssuerService()
				.registerIssuerType(getCurrentWeIdAuth(), type);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error(
					"[registerIssuerType] register issuer type {} faild. error code : {}, error msg :{}",
					type,
					response.getErrorCode(),
					response.getErrorMessage()
			);
			return response;
		}

		log.info("[registerIssuerType] register issuer type {} success.", type);
		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	private WeIdAuthentication getCurrentWeIdAuth() {
		String hash = WeIdSdkUtils.getMainHash();
		WeIdPrivateKey weIdPrivateKey = WeIdSdkUtils.getWeIdPrivateKey(hash);
		WeIdAuthentication callerAuth = new WeIdAuthentication();
		callerAuth.setWeIdPrivateKey(weIdPrivateKey);
		String weid = WeIdUtils.convertPublicKeyToWeId(
				DataToolUtils.publicKeyFromPrivate(new BigInteger(weIdPrivateKey.getPrivateKey())).toString());
		callerAuth.setWeId(weid);
		WeIdDocument weIdDocument = getWeIdService().getWeIdDocument(weid).getResult();
		callerAuth.setAuthenticationMethodId(weIdDocument.getAuthentication().get(0).getId());
		return callerAuth;
	}

    public ResponseData<PageDto<IssuerType>> getIssuerTypeList(PageDto<IssuerType> pageDto) {
        ResponseData<List<IssuerType>> response =
            getAuthorityIssuerService().getIssuerTypeList(pageDto.getStartIndex(), pageDto.getPageSize());
        if (response.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
            log.error("getIssuerTypeList error, msg:{}", response.getErrorMessage());
            return new ResponseData<>(null, response.getErrorCode(), response.getErrorMessage());
        }
        pageDto.setAllCount(getAuthorityIssuerService().getIssuerTypeCount().getResult());
        pageDto.setDataList(response.getResult());
        return new ResponseData<>(pageDto, ErrorCode.SUCCESS);
    }

    public ResponseData<Boolean> removeIssuerType(String type) {
        log.info("[removeIssuerType] removeing issuer type with best effort: " + type);
        ResponseData<Boolean> response = getAuthorityIssuerService()
                .removeIssuerType(getCurrentWeIdAuth(), type);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            log.error(
                    "[removeIssuerType] remove issuer type {} faild. error code : {}, error msg :{}",
                    type,
                    response.getErrorCode(),
                    response.getErrorMessage()
            );
            return response;
        }
        log.info("[removeIssuerType] remove issuer type {} success.", type);
        return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
    }

    public ResponseData<PageDto<AuthorityIssuer>> getSpecificTypeIssuerList(PageDto<AuthorityIssuer> pageDto, String type) {
        ResponseData<List<String>> response =
            getAuthorityIssuerService().getAllSpecificTypeIssuerList(type, pageDto.getStartIndex(), pageDto.getPageSize());
        List<AuthorityIssuer> rList = new ArrayList<>();
        if (response.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            if (CollectionUtils.isNotEmpty(response.getResult())) {
                response.getResult().forEach(value -> {
                    AuthorityIssuer issuer = this.getIssuerByWeId(value);
                    if (issuer == null) {
                        issuer = new AuthorityIssuer();
                        issuer.setWeId(WeIdUtils.convertAddressToWeId(value));
                    }
                    rList.add(issuer);
                });
            }
        } else {
            log.warn("[getSpecificTypeIssuerList] query issuerList in type from chain fail: {} - {}.", response.getErrorCode(), response.getErrorMessage());
            return new ResponseData<>(null, response.getErrorCode(),  response.getErrorMessage());
        }
        pageDto.setAllCount(getAuthorityIssuerService().getSpecificTypeIssuerSize(type).getResult());
        pageDto.setDataList(rList);
        return new ResponseData<>(pageDto, ErrorCode.SUCCESS);
    }

    public ResponseData<Integer> getSpecificTypeIssuerSize(String type) {
        return getAuthorityIssuerService().getSpecificTypeIssuerSize(type);
    }

	public ResponseData<Boolean> addIssuerIntoIssuerType(String type, String weId) {
		log.info("[addIssuerIntoIssuerType] Adding WeIdentity DID {} in {}", weId, type);
		ResponseData<Boolean> response = getAuthorityIssuerService()
				.addIssuerIntoIssuerType(getCurrentWeIdAuth(), type, weId);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error(
					"[addIssuerIntoIssuerType] add {} into {} fail. error code : {}, error msg :{}",
					weId,
					type,
					response.getErrorCode(),
					response.getErrorMessage()
			);
			return response;
		}

		log.info("[addIssuerIntoIssuerType] add {} into {} success.", weId, type);
		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	public ResponseData<Boolean> removeIssuerFromIssuerType(String type, String weId) {
		log.info("[removeIssuerFromIssuerType] Removing WeIdentity DID {} from {}", weId, type);
		ResponseData<Boolean> response = getAuthorityIssuerService()
				.removeIssuerFromIssuerType(getCurrentWeIdAuth(), type, weId);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error(
					"[removeIssuerFromIssuerType] remove {} from {} fail. error code : {}, error msg :{}",
					weId,
					type,
					response.getErrorCode(),
					response.getErrorMessage()
			);
			return response;
		}

		log.info("[removeIssuerFromIssuerType] remove {} from {} success.", weId, type);
		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	public ResponseData<Boolean> registerCpt(File cptFile, String cptId, DataFrom from) {
		try {
			ResponseData<CptFile> responseData = registerCpt(cptFile, getCurrentWeIdAuth(), cptId, from);
			if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
				return new ResponseData<>(Boolean.FALSE, responseData.getErrorCode(), responseData.getErrorMessage());
			}
		} catch (IOException e) {
			return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR.getCode(), e.getMessage());
		}

		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	public ResponseData<CptFile> registerCpt(
			File cptFile,
			WeIdAuthentication callerAuth,
			String cptId,
			DataFrom from) throws IOException {

		String fileName = cptFile.getName();
		log.info("[registerCpt] begin register CPT file: {}", fileName);
		CptFile result = new CptFile();
		result.setCptFileName(fileName);
		if (!fileName.endsWith(".json")) {
			log.error("the file type error. fileName={}", fileName);
			return new ResponseData<>(result, ErrorCode.UNKNOW_ERROR.getCode(), "the file type error");
		}
		JsonNode jsonNode = DataToolUtils.loadJsonObjectFromFile(cptFile);
		String cptJsonSchema = jsonNode.toString();
		CptStringArgs cptStringArgs = new CptStringArgs();
		cptStringArgs.setCptJsonSchema(cptJsonSchema);
		cptStringArgs.setWeIdAuthentication(callerAuth);

		ResponseData<CptBaseInfo> response;
		if (StringUtils.isEmpty(cptId)) {
			response = getCptService().registerCpt(cptStringArgs);
		} else {
			Integer cptId1 = Integer.valueOf(cptId);
			response = getCptService().registerCpt(cptStringArgs, cptId1);
		}
		//System.out.println("[RegisterCpt] result:" + DataToolUtils.serialize(response));
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error("[registerCpt] load config faild. ErrorCode is:{}, msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return new ResponseData<>(result, response.getErrorCode(), response.getErrorMessage());
		}

		log.info("[registerCpt] register CPT file:{} result is success. cpt id = {}", fileName,
				response.getResult().getCptId());

		Integer resultCptId = response.getResult().getCptId();
		String content = new StringBuffer()
				.append(fileName)
				.append("=")
				.append(resultCptId)
				.append("\r\n")
				.toString();
		FileUtils.writeToFile(content, BuildToolsConstant.CPT_RESULT_PATH + "/regist_cpt.out", FileOperator.APPEND);

		log.info("[registerCpt] begin save register info.");
		//开始保存CPT数据
		File cptDir = new File(BuildToolsConstant.CPT_PATH + "/" + WeIdSdkUtils.getMainHash() + "/" + resultCptId);
		cptDir.mkdirs();
		//复制CPT文件
		FileUtils.copy(cptFile, new File(cptDir.getAbsolutePath(), cptFile.getName()));
		//构建cpt数据文件
		String data = DataToolUtils.serialize(
				buildCptInfo(callerAuth.getWeId(), resultCptId, fileName, from, jsonNode));
		File cptInfoFile = new File(cptDir.getAbsoluteFile(), "info");
		FileUtils.writeToFile(data, cptInfoFile.getAbsolutePath(), FileOperator.OVERWRITE);
		//链上查询cpt写链上cpt文件
		ResponseData<String> responseData = queryCptSchema(resultCptId);
		if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
			return new ResponseData<>(result, responseData.getErrorCode(), responseData.getErrorMessage());
		}
		String cptSchema = responseData.getResult();
		File file = new File(cptDir.getAbsoluteFile(), getCptFileName(resultCptId));
		FileUtils.writeToFile(cptSchema, file.getAbsolutePath(), FileOperator.OVERWRITE);
//        result.setMessage(BuildToolsConstant.SUCCESS);
		result.setCptId(resultCptId);
		return new ResponseData<>(result, ErrorCode.SUCCESS);
	}

	private CptInfo buildCptInfo(String weId, Integer cptId, String cptJsonName, DataFrom from, JsonNode jsonNode) {
		CptInfo info = new CptInfo();
		info.setHash(WeIdSdkUtils.getMainHash());
		long time = System.currentTimeMillis();
		info.setTime(time);
		info.setCptId(cptId);
		info.setWeId(weId);
		info.setCptJsonName(cptJsonName);
		info.setFrom(from.name());
		info.setCptTitle(jsonNode.get("title").asText());
		info.setCptDesc(jsonNode.get("description").asText());
		return info;
	}

	public List<CptInfo> getCptInfoList() {
		List<CptInfo> list = new ArrayList<>();
		String currentHash = WeIdSdkUtils.getMainHash();
		if (StringUtils.isBlank(currentHash)) {
			return list;
		}
		File targetDir = new File(BuildToolsConstant.CPT_PATH + "/" + currentHash);
		if (!targetDir.exists()) {
			return list;
		}
		for (File file : targetDir.listFiles()) {
			File cptInfoFile = new File(file.getAbsoluteFile(),"info");
			String jsonData = FileUtils.readFile(cptInfoFile.getAbsolutePath());
			CptInfo info = DataToolUtils.deserialize(jsonData, CptInfo.class);
			list.add(info);
		}
		Collections.sort(list);
		return list;
	}

	public ResponseData<PageDto<CptInfo>> getCptList(PageDto<CptInfo> pageDto) {
		if ("user".equals(pageDto.getQuery().getCptType())) {
			pageDto.setStartIndex(pageDto.getStartIndex() + BuildToolsConstant.CPTID_LIST.size());
		}

		ResponseData<List<Integer>> response =
				getCptService().getCptIdList(pageDto.getStartIndex(), pageDto.getPageSize());
		List<CptInfo> list = new ArrayList<>();
		if (response.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
			for (Integer cptId : response.getResult()) {
				Cpt cpt = getCptService().queryCpt(cptId).getResult();
				CptInfo cptInfo = new CptInfo();
				cptInfo.setCptId(cptId);
				cptInfo.setCptTitle((String)cpt.getCptJsonSchema().get("title"));
				cptInfo.setWeId(cpt.getCptPublisher());
				cptInfo.setCptDesc((String)cpt.getCptJsonSchema().get("description"));
				cptInfo.setTime(cpt.getCreated());
				if (BuildToolsConstant.CPTID_LIST.contains(cptId)) {
					cptInfo.setCptType("sys");
				} else {
					cptInfo.setCptType("user");
				}
				if (StringUtils.isBlank(pageDto.getQuery().getCptType())) {
					list.add(cptInfo);
				} else if (cptInfo.getCptType().equals(pageDto.getQuery().getCptType())) {
					list.add(cptInfo);
				}
			}
		} else {
			log.warn("[getCptList] query getCptList from chain fail: {} - {}.", response.getErrorCode(), response.getErrorMessage());
			return new ResponseData<>(null, response.getErrorCode(), response.getErrorMessage());
		}

		if ("sys".equals(pageDto.getQuery().getCptType())) {
			pageDto.setAllCount(BuildToolsConstant.CPTID_LIST.size());
		} else if ("user".equals(pageDto.getQuery().getCptType())) {
			pageDto.setAllCount(getCptService().getCptCount().getResult() - BuildToolsConstant.CPTID_LIST.size());
		} else {
			pageDto.setAllCount(getCptService().getCptCount().getResult());
		}
		pageDto.setDataList(list);
		return new ResponseData<>(pageDto, ErrorCode.SUCCESS);
	}

	public ResponseData<String> queryCptSchema(Integer cptId) {
		ResponseData<Cpt> response = getCptService().queryCpt(cptId);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error("[queryCptInfo] query cpt fail. ErrorCode is:{}, msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return new ResponseData<>(StringUtils.EMPTY, response.getErrorCode(), response.getErrorMessage());
		}

		log.info("[queryCptInfo] query CPT is success. cpt id = {}", response.getResult().getCptId());
		return new ResponseData<>(ConfigUtils.serializeWithPrinter(response.getResult().getCptJsonSchema()),
				ErrorCode.SUCCESS);
	}

	/**
	 * 根据CPT文件生成java源文件
	 * @param cptFile cpt文件
	 * @param cptId cptId
	 * @param pojoId 转换成pojo的Id
	 * @param fromType 转换来源
	 * @throws Exception 异常抛出
	 */
	public void generateJavaCodeByCpt(File cptFile, Integer cptId, String pojoId, String fromType) throws Exception {
		String fileName = cptFile.getName();
		log.info("[generateJavaCodeByCpt] begin generate for cptFile = {}", fileName);
		JCodeModel codeModel = new JCodeModel();
		try {
			URL source = cptFile.toURI().toURL();
			GenerationConfig config = new DefaultGenerationConfig() {
				@Override
				public boolean isGenerateBuilders() {
					return true;
				}
				@Override
				public boolean isIncludeAdditionalProperties() {
					return false;
				}
			};
			SchemaMapper mapper = new SchemaMapper(
					new RuleFactory(config, new NoopAnnotator(), new SchemaStore()),
					new SchemaGenerator());
			String packageName = "com.weidentity.weid.cpt" + cptId;
			if ("policy".equals(fromType)) {
				packageName = packageName + ".policy";
			}

			mapper.generate(codeModel, "Cpt" + cptId, packageName, source);
			File targetFile = WeIdSdkUtils.getSourceFile(pojoId);
			targetFile.mkdirs();
			codeModel.build(targetFile);
			log.info("[generateJavaCodeByCpt] generate successfully cptFile = {}", fileName);
		} catch (Exception e) {
			log.error("[generateJavaCodeByCpt] generate has error. cptFile = {}", fileName, e);
			throw e;
		}
	}

	/**
	 * convert policy to cpt pojo.
	 *
	 * @param policyJson the policy JSON
	 * @return 返回披露策略文件中cpt集合
	 */
	public ResponseData<List<CptFile>> generateCptFileListByPolicy(String policyJson) {
		List<CptFile> cptFileList = new ArrayList<>();
		try {
			PresentationPolicyE policyE = PresentationPolicyE.fromJson(policyJson);
			if (policyE == null) {
				log.error(
						"[generateCptFileByPolicy]Presentation policy from json = {} is null!",
						policyJson);
				return new ResponseData<>(null, ErrorCode.ILLEGAL_INPUT);
			}
			String policyType = policyE.getPolicyType();
			Map<Integer, ClaimPolicy> policy = policyE.getPolicy();
			for (Map.Entry<Integer, ClaimPolicy> entry : policy.entrySet()) {
				Integer cptId = entry.getKey();
				ResponseData<Cpt> resp = getCptService().queryCpt(cptId);
				Cpt cpt = resp.getResult();
				if (cpt == null || !resp.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
					log.error(
							"[generateCptFileByPolicy] CPT --->{} in presentation policy "
									+ "does not exist!",
							cptId);
					return new ResponseData<>(null, ErrorCode.CPT_NOT_EXISTS);
				}
				Map<String, Object> cptMap = DataToolUtils.clone((HashMap<String, Object>)cpt.getCptJsonSchema()) ;
				Map<String, Object> cptFieldMap = (Map<String, Object>) cptMap.get(BuildToolsConstant.PROPERTIES_KEY);
				ClaimPolicy claimPolicy = entry.getValue();
				String fieldsToBeDisclosed = claimPolicy.getFieldsToBeDisclosed();
				HashMap<String, Object> disclosedMap = DataToolUtils
						.deserialize(fieldsToBeDisclosed, HashMap.class);
				if ("zkp".equals(policyType)) {
					disclosedMap = (HashMap) disclosedMap.get(BuildToolsConstant.CLAIM_KEY);
				}
				generateCptMap(disclosedMap, cptFieldMap);
				if (cptFieldMap.isEmpty()) {//说明全部为不披露
					return new ResponseData<>(null, -1, "all attributes are not disclosed");
				}
				String cptJson = DataToolUtils.serialize(cptMap);
				String fileName = "Cpt" + cpt.getCptId() + ".json";
				FileUtils.writeToFile(cptJson, fileName, FileOperator.OVERWRITE);
				CptFile cptFile = new CptFile();
				cptFile.setCptFileName(fileName);
				cptFile.setCptId(cptId);
				cptFile.setMessage(ErrorCode.SUCCESS.getCodeDesc());
				cptFileList.add(cptFile);
			}
			return new ResponseData<>(cptFileList, ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[CptToPojo] Generate CPT file by policyJson - {} failed.", policyJson, e);
			return new ResponseData<>(null, ErrorCode.UNKNOW_ERROR);
		}
	}

	private static void generateCptMap(
			Map<String, Object> disclosedMap,
			Map<String, Object> cptMap) {

		for (Map.Entry<String, Object> en : disclosedMap.entrySet()) {
			String k = en.getKey();
			Object v = en.getValue();
			Object cptObj = cptMap.get(k);
			if (v instanceof Map) {
				HashMap cptValue = (HashMap) cptObj;
				processKey(k, (HashMap)v, (HashMap)cptMap, cptValue);
			} else if (v instanceof List) {
				ArrayList discloseList = ((ArrayList) v);
				HashMap dMap = (HashMap) discloseList.get(0);
				HashMap cptValue = (HashMap) cptObj;
				HashMap itemValue = (HashMap) cptValue.get(BuildToolsConstant.ITEMS_KEY);
				processKey(k, dMap, (HashMap)cptMap, itemValue);
			} else {
				if (String.valueOf(v).equals("0")) {
					cptMap.remove(k);
				}
			}
		}
	}

	private static void processKey(String k, HashMap dMap, HashMap cptMap, HashMap cptMapNext) {
		if(cptMapNext.containsKey(BuildToolsConstant.PROPERTIES_KEY)) {
			HashMap cptProoperties = (HashMap) cptMapNext.get(BuildToolsConstant.PROPERTIES_KEY);
			generateCptMap(dMap, cptProoperties);
			if (cptProoperties.isEmpty()) {
				cptMap.remove(k);
			}
		} else {
			generateCptMap(dMap, cptMapNext);
			if (cptMapNext.isEmpty()) {
				cptMap.remove(k);
			}
		}
	}

	public AuthorityIssuer getIssuerByWeId(String weIdAddress) {
		String mainHash = WeIdSdkUtils.getMainHash();
		if (StringUtils.isBlank(mainHash)) {
			return null;
		}
		String weId = WeIdUtils.convertAddressToWeId(weIdAddress);

		log.info("[getIssuerByWeId] begin query issuer. weid = {}", weId);
		ResponseData<AuthorityIssuer> response = getAuthorityIssuerService().queryAuthorityIssuerInfo(weId);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.warn("[getIssuerByWeId] query issuer fail. ErrorCode is:{}, msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return null;
		} else {
			return response.getResult();
		}
	}

	public ResponseData<Boolean> registerClaimPolicy(Integer cptId, String policyJson) {
		WeIdAuthentication currentWeIdAuth = getCurrentWeIdAuth();
		ResponseData<Integer> response = getPolicyService().registerClaimPolicy(cptId, policyJson, currentWeIdAuth);
		if (response.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
			log.error("[registerClaimPolicy] register ClaimPolicy fail. ErrorCode is:{}, msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return new ResponseData<>(Boolean.FALSE, response.getErrorCode(), response.getErrorMessage());
		}

		log.info(
				"[registerClaimPolicy] register ClaimPolicys success. cpt id = {}, policyId = {}", cptId, response.getResult());
		return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
	}

	public ResponseData<PageDto<PolicyInfo>> getPolicyList(PageDto<PolicyInfo> pageDto) {
		ResponseData<List<Integer>> response =
				getPolicyService().getAllClaimPolicies(pageDto.getStartIndex(), pageDto.getPageSize());
		if (response.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
			log.error("getPolicyList error, msg:{}", response.getErrorMessage());
			return new ResponseData<>(null, response.getErrorCode(), response.getErrorMessage());
		}

		List<PolicyInfo> list = new ArrayList<>();
		if (response.getResult() != null) {
			for (Integer value : response.getResult()) {
				PolicyInfo policyInfo = new PolicyInfo();
				policyInfo.setId(value.toString());
				list.add(policyInfo);
			}
		}
		pageDto.setAllCount(getPolicyService().getPolicyCount().getResult());
		pageDto.setDataList(list);
		return new ResponseData<>(pageDto, ErrorCode.SUCCESS);
	}

	public ResponseData<String> queryPolicy(Integer policyId) {
		ResponseData<ClaimPolicy> response = getPolicyService().getClaimPolicy(policyId);
		if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
			log.error("[queryPolicy] query policy fail. ErrorCode is:{}, msg :{}",
					response.getErrorCode(),
					response.getErrorMessage());
			return new ResponseData<>(StringUtils.EMPTY, response.getErrorCode(), response.getErrorMessage());
		}

		log.info("[queryPolicy] query policy is success. policy id = {}", policyId);
		HashMap claimMap = DataToolUtils.deserialize(response.getResult().getFieldsToBeDisclosed(), HashMap.class);
		return new ResponseData<>(ConfigUtils.serializeWithPrinter(claimMap), ErrorCode.SUCCESS);
	}

	public File getCptFile(Integer cptId) {
		return new File(BuildToolsConstant.CPT_PATH + "/" + WeIdSdkUtils.getMainHash() + "/" + cptId,
				getCptFileName(cptId));
	}

	private String getCptFileName(Integer cptId) {
		return "Cpt" + cptId + ".json";
	}


	public ResponseData<Boolean> cptToPojo(Integer[] cptIds) {
		String pojoId = DataToolUtils.getUuId32();
		try {
			log.info("[cptToPojo] begin cpt to pojo.");
			for (int i = 0; i < cptIds.length; i++) {
				File cptFile = getCptFile(cptIds[i]);
				generateJavaCodeByCpt(cptFile, cptIds[i], pojoId, "cpt");
			}
			File sourceFile = WeIdSdkUtils.getSourceFile(pojoId);
			return new ResponseData<>(
					WeIdSdkUtils.createJar(sourceFile, cptIds, "cpt", DataFrom.WEB),
					ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[cptToPojo] cpt to pojo has error.", e);
			WeIdSdkUtils.deletePojoInfo(pojoId);
			return new ResponseData<>(Boolean.FALSE,
					ErrorCode.BASE_ERROR.getCode(),
					"cpt to pojo has error");
		}
	}

	public ResponseData<Boolean> policyToPojo(String policy) {
		String pojoId = DataToolUtils.getUuId32();
		try {
			policy = StringEscapeUtils.unescapeHtml(policy);
			log.info("[policyToPojo] begin policy to pojo, policy:{}", policy);

			ResponseData<List<CptFile>> result = generateCptFileListByPolicy(policy);
			if (result.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
				return new ResponseData<>(Boolean.FALSE, result.getErrorCode(), result.getErrorMessage());
			}

			List<Integer> cptIdList = new ArrayList<>();
			for (CptFile cptFile : result.getResult()) {
				File file = new File(cptFile.getCptFileName());
				generateJavaCodeByCpt(file, cptFile.getCptId(), pojoId, "policy");
				cptIdList.add(cptFile.getCptId());
				//cpt生成代码文件后进行删除policy生成的中间cpt文件
				FileUtils.delete(file);
			}
			File sourceFile = WeIdSdkUtils.getSourceFile(pojoId);
			Integer[] cptIds = cptIdList.toArray(new Integer[cptIdList.size()]);
			return new ResponseData<>(
					WeIdSdkUtils.createJar(sourceFile, cptIds, "policy", DataFrom.WEB),
					ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[cptToPojo] policy to pojo has error.", e);
			WeIdSdkUtils.deletePojoInfo(pojoId);
			return new ResponseData<>(Boolean.FALSE,
					ErrorCode.UNKNOW_ERROR.getCode(),
					"policy to pojo has error");
		}
	}

	public ResponseData<String> cptToPolicy(Integer cptId) {
		// 先根据cpt转pojo，然后根据pojo转policy
		// 生成pojoId
		String pojoId = String.valueOf(cptId); //DataToolUtils.getUuId32();
		try {
			String jarPath = WeIdSdkUtils.getJarFile(pojoId).getAbsolutePath();
			File jarFile = new File(jarPath);
			if (!jarFile.exists()) {
				log.info("[cptToPolicy] begin cpt to pojo. cptId = {}.", cptId);
				// 获取cpt文件
				File cptFile = getCptFile(cptId);
				if (!cptFile.exists()) {
					//如果不存在cpt文件则 创建cpt文件
					ResponseData<String> responseData = queryCptSchema(cptId);
					if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
						return responseData;
					}
					FileUtils.writeToFile(responseData.getResult(), cptFile.getAbsolutePath());
				}
				// 生成源码文件
				generateJavaCodeByCpt(cptFile, cptId, pojoId, "cpt");
				File sourceFile = WeIdSdkUtils.getSourceFile(pojoId);
				// 转成jar文件
				Integer[] cptIds = {cptId};
				WeIdSdkUtils.createJar(sourceFile, cptIds, "cpt", DataFrom.WEB);
				log.info("[cptToPolicy] begin pojo to policy, cptId = {}.", cptId);
			}
			// 根据pojo转换policy
			String policy = PolicyFactory.loadJar(jarPath).generate(cptId);
			return new ResponseData<>(policy, ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[cptToPolicy] cpt to policy has error.", e);
			WeIdSdkUtils.deletePojoInfo(pojoId);
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.UNKNOW_ERROR.getCode(), "cpt to policy has error");
		}
	}

	public ResponseData<List<PojoInfo>> getPojoList() {
		List<PojoInfo> list = new ArrayList<>();
		String currentHash = WeIdSdkUtils.getMainHash();
		if (StringUtils.isBlank(currentHash)) {
			return new ResponseData<>(list, ErrorCode.UNKNOW_ERROR);
		}
		File targetDir = new File(BuildToolsConstant.POJO_PATH + "/" + currentHash);
		if (!targetDir.exists()) {
			return new ResponseData<>(list, ErrorCode.UNKNOW_ERROR);
		}
		for (File file : targetDir.listFiles()) {
			File cptInfoFile = new File(file.getAbsoluteFile(),"info");
			String jsonData = FileUtils.readFile(cptInfoFile.getAbsolutePath());
			PojoInfo info = DataToolUtils.deserialize(jsonData, PojoInfo.class);
			list.add(info);
		}
		Collections.sort(list);
		return new ResponseData<>(list, ErrorCode.SUCCESS);
	}

	public ResponseData<List<Map<String, String>>> getGroupMapping() {
		Map<String, List<String>> groupMapping = WeServerUtils.getGroupMapping();
		Set<Map.Entry<String, List<String>>> entrySet = groupMapping.entrySet();
		List<Map<String, String>> list = new ArrayList<>();
		String masterGroupId = configService.loadConfig().get("group_id");
		for (Map.Entry<String, List<String>> entry : entrySet) {
			Map<String, String> info = new HashMap<>();
			info.put("groupId", entry.getKey());
			info.put("nodes", entry.getValue().toString());
			info.put("type", "子群组");
			if (masterGroupId.equals(entry.getKey())) {
				info.put("type", "主群组");
				list.add(0, info);
			} else {
				list.add(info);
			}
		}
		return new ResponseData<>(list, ErrorCode.SUCCESS);
	}

	/**
	 * 获取群组列表
	 * @param filterMaster 是否过滤主群组
	 * @return 返回群组列表
	 */
	public List<String> getAllGroup(boolean filterMaster) {
		List<String> list = WeServerUtils.getGroupList();
		if (filterMaster) {
			return list.stream()
					.filter(s -> !s.equals(BaseServiceFisco.masterGroupId.toString()))
					.collect(Collectors.toList());
		}
		return list;
	}

	private String getWeIdAddress(String weId) {
		return WeIdUtils.convertWeIdToAddress(weId);
	}
	
    private void importPrivateKeyToWeBase(String weId, String privateKey) {
        String accountName = this.getWeIdAddress(weId);
        // 获取群组
		String groupId = configService.getMasterGroupId();
        Boolean result = weBaseService.importPrivateKeyToWeBase(groupId, accountName, privateKey);
        log.info("[createWeId] import privateKey to weBase result = {}", result);
	}

    private void importPublicKeyToWeBase(String weId, String publicKey) {
        String accountName = this.getWeIdAddress(weId);
        // 获取群组
        String groupId = configService.getMasterGroupId();
        Boolean result = weBaseService.importPublicKeyToWeBase(groupId, accountName, publicKey);
        log.info("[createWeId] import publicKey to weBase result = {}", result);
    }
}
