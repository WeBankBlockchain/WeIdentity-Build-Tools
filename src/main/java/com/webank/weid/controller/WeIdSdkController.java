package com.webank.weid.controller;

import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.dto.*;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.IssuerType;
import com.webank.weid.blockchain.protocol.response.ResponseData;
import com.webank.weid.service.ContractService;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
 * WeId sdk功能管理
 */

@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
@Slf4j
public class WeIdSdkController {

	@Autowired
	private WeIdSdkService weIdSdkService;

	@Autowired
	private ContractService contractService;

	@Description("查询数据概览")
	@GetMapping("/getDataPanel")
	public ResponseData<DataPanel> getDataPanel() {
		log.info("start getDataPanel.");
		return weIdSdkService.getDataPanel();
	}

	@GetMapping("/getWeIdList")
	public ResponseData<PageDto<WeIdInfo>> getWeIdList(
			@RequestParam(value = "pageSize") int pageSize,
			@RequestParam(value = "indexFirst") int indexFirst,
			@RequestParam(value = "iDisplayStart") int iDisplayStart,
			@RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		PageDto<WeIdInfo> pageDto = new PageDto<WeIdInfo>(iDisplayStart, iDisplayLength);
		return weIdSdkService.getWeIdList(pageDto, pageSize, indexFirst);
	}

	@Description("查询issuer type列表")
	@GetMapping("/getIssuerTypeList")
	public ResponseData<PageDto<IssuerType>> getIssuerTypeList(
	    @RequestParam(value = "iDisplayStart") int iDisplayStart,
	    @RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		log.info("begin get issuer type list, iDisplayStart:{}, iDisplayLength:{}", iDisplayStart, iDisplayLength);
        PageDto<IssuerType> pageDto = new PageDto<IssuerType>(iDisplayStart, iDisplayLength);
		return weIdSdkService.getIssuerTypeList(pageDto);
	}

    @Description("删除issuer type列表")
    @PostMapping("/removeIssuerType")
    public ResponseData<Boolean> removeIssuerType(@RequestParam("issuerType") String type) {
        log.info("begin remove issuer type: {}", type);
        return weIdSdkService.removeIssuerType(type);
    }

	@Description("系统自动生成公私钥生成weId")
	@PostMapping("/createWeId")
	public ResponseData<String> createWeId() {
		log.info("[createWeId] begin create weid...");
		return weIdSdkService.createWeId(DataFrom.WEB_BY_DEFAULT);
	}

	@Description("根据传入的私钥创建weId")
	@PostMapping("/createWeIdByPrivateKey")
	public ResponseData<String> createWeIdByPrivateKey(HttpServletRequest request) {
		log.info("[createWeIdByPrivateKey] begin create weid...");
		return weIdSdkService.createWeIdByPrivateKey(request, DataFrom.WEB_BY_PRIVATE_KEY);
	}

	@Description("根据传入的公钥代理创建weId")
	@PostMapping("/createWeIdByPublicKey")
	public ResponseData<String> createWeIdByPublicKey(HttpServletRequest request) {
		log.info("[createWeIdByPublicKey] begin create weid...");
		return weIdSdkService.createWeIdByPublicKey(request, DataFrom.WEB_BY_PUBLIC_KEY);
	}

	@Description("注册issuer")
	@PostMapping("/registerIssuer")
	public ResponseData<Boolean> registerIssuer(@RequestParam("weId") String weId,
												@RequestParam("name") String name,
												@RequestParam("description") String description) {
		log.info("begin register issuer, weId:{}, name:{}, description:{}", weId, name, description);
		description = StringEscapeUtils.unescapeHtml(description);
		return weIdSdkService.registerIssuer(weId, name, description);
	}

	@Description("认证issuer")
	@PostMapping("/recognizeAuthorityIssuer")
	public ResponseData<Boolean> recognizeAuthorityIssuer(@RequestParam("weId") String weId) {
		log.info("begin recognize authority issuer, weId:{}", weId);
		return weIdSdkService.recognizeAuthorityIssuer(weId);
	}

	@Description("撤销认证issuer")
	@PostMapping("/deRecognizeAuthorityIssuer")
	public ResponseData<Boolean> deRecognizeAuthorityIssuer(@RequestParam("weId") String weId) {
		log.info("begin deRecognize authority issuer, weId:{}", weId);
		return weIdSdkService.deRecognizeAuthorityIssuer(weId);
	}

	@GetMapping("/getIssuerList")
	public ResponseData<PageDto<Issuer>> getIssuerList(
			@RequestParam(value = "iDisplayStart") int iDisplayStart,
			@RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		log.info("begin get issuer list, iDisplayStart:{}, iDisplayLength:{}", iDisplayStart, iDisplayLength);
		PageDto<Issuer> pageDto = new PageDto<Issuer>(iDisplayStart, iDisplayLength);
		return weIdSdkService.getIssuerList(pageDto);
	}

	@Description("移除issuer")
	@PostMapping("/removeIssuer")
	public ResponseData<Boolean> removeIssuer(@RequestParam("weId") String weId) {
		log.info("begin remove issuer, weId:{}", weId);
		return weIdSdkService.removeIssuer(weId);
	}

	@Description("注册issuer type")
	@PostMapping("/registerIssuerType")
	public ResponseData<Boolean> registerIssuerType(@RequestParam("issuerType") String type) {
		log.info("begin register issuer type, issuerType:{}", type);
		return weIdSdkService.registerIssuerType(type, DataFrom.WEB);
	}

	@Description("查询issuer成员列表")
	@PostMapping("/getIssuerListInType")
	public ResponseData<PageDto<AuthorityIssuer>> getIssuerListInType(
	    @RequestParam("issuerType") String type,
	    @RequestParam(value = "iDisplayStart") int iDisplayStart,
	    @RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		log.info("begin get issuer list in type, iDisplayStart:{}, iDisplayLength:{}", iDisplayStart, iDisplayLength);
        PageDto<AuthorityIssuer> pageDto = new PageDto<AuthorityIssuer>(iDisplayStart, iDisplayLength);
		return weIdSdkService.getSpecificTypeIssuerList(pageDto, type);
	}

    @Description("查询type中issuer成员总数")
    @GetMapping("/getSpecificTypeIssuerSize")
    public ResponseData<Integer> getSpecificTypeIssuerSize(
        @RequestParam("issuerType") String type
    ) {
        return weIdSdkService.getSpecificTypeIssuerSize(type);
    }

	@Description("向IssuerType中添加成员")
	@PostMapping("/addIssuerIntoIssuerType")
	public ResponseData<Boolean> addIssuerIntoIssuerType(
			@RequestParam("issuerType") String type,
			@RequestParam("weId") String weId
	) {
		log.info("begin addIssuerIntoIssuerType, weId:{}, issuerType:{}", weId, type);
		return weIdSdkService.addIssuerIntoIssuerType(type, weId);
	}

	@Description("移除IssuerType中的成员")
	@PostMapping("/removeIssuerFromIssuerType")
	public ResponseData<Boolean> removeIssuerFromIssuerType(
			@RequestParam("issuerType") String type,
			@RequestParam("weId") String weId) {
		log.info("begin remove issuer from issuer type, weId:{}, issuerType:{}", weId, type);
		return weIdSdkService.removeIssuerFromIssuerType(type, weId);
	}

	@Description("CPT注册")
	@PostMapping("/registerCpt")
	public ResponseData<Boolean> registerCpt(HttpServletRequest request) {
		log.info("[registerCpt] begin save the cpt json file...");
		String cptJson = request.getParameter("cptJson");
		cptJson = StringEscapeUtils.unescapeHtml(cptJson);
		String fileName = DataToolUtils.getUuId32();
		File targetFIle = new File("output/", fileName + ".json");
		FileUtils.writeToFile(cptJson, targetFIle.getAbsolutePath(), FileOperator.OVERWRITE);
		log.info("[registerCpt] begin register cpt...");
		String cptId = request.getParameter("cptId");
		try {
			//判断当前账户是否注册成weid，如果没有则创建weid
			contractService.createWeIdForCurrentUser(DataFrom.WEB);
			return weIdSdkService.registerCpt(targetFIle, cptId, DataFrom.WEB);
		} finally {
			FileUtils.delete(targetFIle);
		}
	}

//    @Description("获取cpt列表")
//    @GetMapping("/getCptInfoList")
//    public List<CptInfo> getCptInfoList() {
//        return weIdSdkService.getCptInfoList();
//    }

	@GetMapping("/getCptInfoList")
	public ResponseData<PageDto<CptInfo>> getCptInfoList(
			@RequestParam(value = "iDisplayStart") int iDisplayStart,
			@RequestParam(value = "iDisplayLength") int iDisplayLength,
			@RequestParam(value = "cptType") String cptType
	) {
		log.info("begin getCptInfoList, iDisplayStart:{}, iDisplayLength:{}, cptType:{}",
				iDisplayStart, iDisplayLength, cptType);
		PageDto<CptInfo> pageDto = new PageDto<CptInfo>(iDisplayStart, iDisplayLength);
		pageDto.setQuery(new CptInfo());
		pageDto.getQuery().setCptType(cptType);
		return weIdSdkService.getCptList(pageDto);
	}

	@Description("获取cpt Schema信息")
	@GetMapping("/queryCptSchema/{cptId}")
	public ResponseData<String> queryCptSchema(@PathVariable("cptId") Integer cptId) {
		log.info("begin queryCptSchema, cptId:{}", cptId);
		return weIdSdkService.queryCptSchema(cptId);
	}

	@Description("将指定cptId转pojo")
	@GetMapping("/cptToPojo")
	public ResponseData<Boolean> cptToPojo(@RequestParam(value = "cptIds[]") Integer[] cptIds) {
		log.info("start cptToPojo.");
		return weIdSdkService.cptToPojo(cptIds);
	}

	@Description("根据policy转pojo")
	@PostMapping("/policyToPojo")
	public ResponseData<Boolean> policyToPojo(@RequestParam(value = "policy") String policy) {
		log.info("start policyToPojo, policy:{}", policy);
		return weIdSdkService.policyToPojo(policy);
	}

	@Description("获取POJO列表")
	@GetMapping("/getPojoList")
	public ResponseData<List<PojoInfo>> getPojoList() {
		log.info("begin getPojoList.");
		return weIdSdkService.getPojoList();
	}

	@Description("获取Policy列表")
	@GetMapping("/getPolicyList")
	public ResponseData<PageDto<PolicyInfo>> getPolicyList(
			@RequestParam(value = "iDisplayStart") int iDisplayStart,
			@RequestParam(value = "iDisplayLength") int iDisplayLength
	) {
		log.info("begin getPolicyList, iDisplayStart:{}, iDisplayLength:{}", iDisplayStart, iDisplayLength);
		PageDto<PolicyInfo> pageDto = new PageDto<PolicyInfo>(iDisplayStart, iDisplayLength);
		return weIdSdkService.getPolicyList(pageDto);
	}

	@Description("获取cpt Schema信息")
	@GetMapping("/queryPolicy/{policyId}")
	public ResponseData<String> queryPolicy(@PathVariable("policyId") Integer policyId) {
		log.info("start queryPolicy, policyId:{}", policyId);
		return weIdSdkService.queryPolicy(policyId);
	}

	@Description("将指定cptId转policy")
	@GetMapping("/cptToPolicy")
	public ResponseData<String> cptToPolicy(@RequestParam(value = "cptId") Integer cptId) {
		log.info("start cptToPolicy, cptId:{}", cptId);
		return weIdSdkService.cptToPolicy(cptId);
	}

	@Description("注册policy到链上")
	@PostMapping("/registerClaimPolicy")
	public ResponseData<Boolean> registerClaimPolicy(HttpServletRequest request) {
		log.info("start registerClaimPolicy.");
		String policyJson = request.getParameter("policy");
		policyJson = StringEscapeUtils.unescapeHtml(policyJson);
		Integer cptId = Integer.parseInt(request.getParameter("cptId"));
		return weIdSdkService.registerClaimPolicy(cptId, policyJson);
	}

	@GetMapping("/getWeIdPath")
	public ResponseData<String> getWeIdPath() {
		log.info("start getWeIdPath.");
		return weIdSdkService.getWeidDir();
	}
}
