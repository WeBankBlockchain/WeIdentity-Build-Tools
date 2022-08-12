package com.webank.weid.controller;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.service.v3.CheckNodeServiceV3;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.CheckNodeFace;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.GuideService;
import com.webank.weid.service.WeBaseService;
import com.webank.weid.service.v2.CheckNodeServiceV2;
import com.webank.weid.util.WeIdSdkUtils;

@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
@Slf4j
public class GuideController {

    @Autowired
    private GuideService guideService;

    @Autowired
    private WeBaseService weBaseService;

    @Autowired
    private ConfigService configService;
	
	@Description("设置引导完成状态")
	@PostMapping("/setGuideStatus")
	public ResponseData<Boolean> setGuideStatus(@RequestParam(value = "step") String step) {
		log.info("start setGuideStatus. step:{}", step);
		return new ResponseData<>(guideService.setGuideStatus(step), ErrorCode.SUCCESS);
	}

	@Description("获取引导状态")
	@GetMapping("/getGuideStatus")
	public ResponseData<String> getGuideStatus() {
		log.info("start getGuideStatus.");
		return guideService.getGuideStatus();
	}

	@Description("设置用户角色")
	@PostMapping("/setRole")
	public ResponseData<Boolean> setRole(@RequestParam(value = "roleType") String roleType) {
		log.info("start setRole. roleType:{}", roleType);
		return new ResponseData<>(guideService.setRoleType(roleType), ErrorCode.SUCCESS);
	}

	@PostMapping("/createAdmin")
	public ResponseData<String> createAdmin(HttpServletRequest request) {
		try {
			MultipartFile file = ((MultipartHttpServletRequest) request).getFile("private_key");
			String inputPrivateKey;
			if (file != null) { //说明是传入的私钥文件
				inputPrivateKey = new String(file.getBytes(), StandardCharsets.UTF_8);
			} else {
				inputPrivateKey = request.getParameter("privateKey");
			}
			// 创建账户
			String account = guideService.createAdmin(inputPrivateKey);
			// 获取群组
			String groupId = configService.getMasterGroupId();
			// 获取私钥
			String privateKey = WeIdSdkUtils.getCurrentPrivateKey().getPrivateKey();
			Boolean result = weBaseService.importPrivateKeyToWeBase(groupId, account, privateKey);
			log.info("[createAdmin] import privateKey to weBase result = {}", result);
			return new ResponseData<>(account, ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[createAdmin] create admin hash error.", e);
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.UNKNOW_ERROR);
		}
	}

	@GetMapping("/checkAdmin")
	public ResponseData<String> checkAdmin() {
		log.info("start checkAdmin.");
		return new ResponseData<>(guideService.checkAdmin(), ErrorCode.SUCCESS);
	}

	@Description("判断当前机构是否存在机构配置，如果存在则不需要配置机构私钥，系统默认配置机构私钥。"
			+ "返回： 1：存在，0：不存在，2：异常。")
	@PostMapping("/checkOrgId")
	public ResponseData<Integer> checkOrgId() {
		try {
			log.info("[checkOrgId] begin check the orgId.");
			// 判断是否存在机构配置
			CheckNodeFace checkNode = null;
			FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
			if (fiscoConfig.getVersion().startsWith(WeIdConstant.FISCO_BCOS_2_X_VERSION_PREFIX)) {
				log.info("[checkNode] the node version is 2.x in your configuration.");
				checkNode = new CheckNodeServiceV2();
			} else {
				log.info("[checkNode] the node version is 3.x in your configuration.");
				checkNode = new CheckNodeServiceV3();
			}
			boolean isExist = checkNode.checkOrgId(WeIdSdkUtils.loadNewFiscoConfig());
			// 如果存在
			if (isExist) {
				String address = guideService.checkAdmin();
				if (StringUtils.isBlank(address)) {
					String account = guideService.createAdmin(null);
					// 获取群组
		            String groupId = configService.getMasterGroupId();
					// 获取私钥
		            String privateKey = WeIdSdkUtils.getCurrentPrivateKey().getPrivateKey();
		            Boolean result = weBaseService.importPrivateKeyToWeBase(groupId, account, privateKey);
		            log.info("[checkOrgId] import privateKey to weBase result = {}", result);
				}
			}
			return new ResponseData<>(isExist ? 1 : 0, ErrorCode.SUCCESS);
		} catch (Exception e) {
			log.error("[checkOrgId] check orgId is exist has error.", e);
			return new ResponseData<>(2, ErrorCode.UNKNOW_ERROR);
		}
	}
}
