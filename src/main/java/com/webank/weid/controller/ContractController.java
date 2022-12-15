package com.webank.weid.controller;

import com.webank.weid.blockchain.config.ContractConfig;
import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.blockchain.constant.CnsType;
import com.webank.weid.blockchain.deploy.v2.DeployContractV2;
import com.webank.weid.blockchain.deploy.v3.DeployContractV3;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.dto.CnsInfo;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.ShareInfo;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.ContractService;
import com.webank.weid.util.WeIdSdkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

import static com.webank.weid.constant.ChainVersion.FISCO_V2;

/**
 * 智能合约管理
 */

@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
@Slf4j
public class ContractController {

	@Value("${weid.build.tools.down:false}")
	private String isDownFile;

	@Autowired
	private ContractService contractService;

	@Autowired
	private ConfigService configService;

	@PostMapping("/deploy")
	public ResponseData<String> deploy(
			@RequestParam("chainId")  String chainId,
			@RequestParam(BuildToolsConstant.APPLY_NAME) String applyName
	) {
		log.info("[deploy] begin load fiscoConfig...");
		return contractService.deploy(chainId, applyName);
	}

	@Description("是否启用主hash")
	@GetMapping("/isEnableMasterCns")
	public ResponseData<Boolean> isEnableMasterCns() {
		log.info("start isEnableMasterCns.");
		return new ResponseData<>(StringUtils.isBlank(WeIdSdkUtils.getMainHash()), ErrorCode.SUCCESS);
	}

	@Description("是否启用Evidence hash")
	@GetMapping("/isEnableEvidenceCns/{groupId}")
	public ResponseData<Boolean> isEnableEvidenceCns(@PathVariable("groupId") String groupId) {
		log.info("start isEnableEvidenceCns, groupId:{}", groupId);
		return new ResponseData<>(StringUtils.isBlank(contractService.getEvidenceHash(groupId)), ErrorCode.SUCCESS);
	}

	@GetMapping("/getDeployInfo/{hash}")
	public ResponseData<DeployInfo> getDeployInfo(@PathVariable("hash") String hash) {
		log.info("start getDeployInfo, hash:{}", hash);
		return new ResponseData<>(contractService.getDeployInfoByHashFromChain(hash), ErrorCode.SUCCESS);
	}

	@PostMapping("/removeHash/{hash}/{type}")
	public ResponseData<Boolean> removeHash(@PathVariable("hash") String hash, @PathVariable("type") Integer type){
		log.info("start removeHash, hash:{}, type:{}", hash, type);
		if (type != 1 && type != 2) {
			log.error("[removeHash] the type error, type = {}.", type);
			return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), "the type error");
		}

		CnsType cnsType = (type == 1) ? CnsType.DEFAULT : CnsType.SHARE;
		return contractService.removeHash(cnsType, hash);
	}

	@GetMapping("/getDeployList")
	public ResponseData<LinkedList<CnsInfo>> getDeployList() {
		log.info("start getDeployList.");
		return contractService.getDeployList();
	}

	@Description("此接口用于给命令版本部署后，重载合约地址")
	@GetMapping("/reloadAddress")
	public void reloadAddress() {
		log.info("start reload address.");
		configService.reloadAddress();
	}

	@GetMapping("/isDownFile")
	public ResponseData<Boolean> isDownFile() {
		log.info("start isDownFile");
		return new ResponseData<>(isDownFile.equals("true"), ErrorCode.SUCCESS);
	}

	@GetMapping("/deploySystemCpt/{hash}")
	public ResponseData<Boolean> deploySystemCpt(@PathVariable("hash") String hash) {
		log.info("[deploySystemCpt] begin deploy System Cpt...");
		return contractService.deploySystemCpt(hash, DataFrom.WEB);
	}

	@Description("从share的cns中获取所有的hash")
	@GetMapping("/getShareList")
	public ResponseData<List<ShareInfo>> getShareList() {
		log.info("start getShareList.");
		return contractService.getShareList();
	}

	@Description("根据群组Id部署Evidence合约")
	@PostMapping("/deployEvidence")
	public ResponseData<String> deployEvidence(
			@RequestParam(value = "groupId") String groupId,
			@RequestParam(value = "evidenceName") String evidenceName
	) {
		log.info("start deploy evidence, groupId:{}, evidenceName:{}", groupId, evidenceName);
		String hash = contractService.deployEvidence(WeIdSdkUtils.loadNewFiscoConfig(), groupId, DataFrom.WEB);
		if (StringUtils.isBlank(hash)) {
			return new ResponseData<>(StringUtils.EMPTY, ErrorCode.BASE_ERROR);
		}
		//将应用名写入配置中
		evidenceName = StringEscapeUtils.unescapeHtml(evidenceName);
		WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getWeIdPrivateKey(hash);
		com.webank.weid.blockchain.protocol.response.ResponseData<Boolean> response = WeIdSdkUtils.getDataBucket(CnsType.SHARE)
				.put(hash, BuildToolsConstant.EVIDENCE_NAME, evidenceName, currentPrivateKey.getPrivateKey());
		log.info("[deployEvidence] put evidenceName: {}", response);
		return new ResponseData<>(hash, ErrorCode.SUCCESS);
	}

	@Description("启用新的shareHash,禁用老的shareHash")
	@PostMapping("/enableShareCns")
	public ResponseData<Boolean> enableShareCns(@RequestParam(value = "hash") String hash) {
		log.info("start enableShareCns. hash:{}", hash);
		return contractService.enableShareCns(hash);
	}

	@GetMapping("/getShareInfo/{hash}")
	public ResponseData<ShareInfo> getShareInfo(@PathVariable("hash") String hash) {
		log.info("start getShareInfo. hash:{}", hash);
		return contractService.getShareInfo(hash);
	}

	@Description("根据hash查询所有的启用成员")
	@GetMapping("/getUserListByHash/{hash}")
	public ResponseData<List<AuthorityIssuer>> getUserListByHash(@PathVariable("hash") String hash) {
		log.info("[getUserListByHash] get user list by hash = {}.", hash);
		return contractService.getUserListByHash(hash);
	}

	@GetMapping("/enableHash/{hash}")
	public ResponseData<Boolean> enableHash(@PathVariable("hash") String hash) {
		log.info("[enableHash] begin load fiscoConfig...");
		try {
			//  获取老Hash
			String  oldHash = WeIdSdkUtils.getMainHash();
			// 获取原配置
			FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
			WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getCurrentPrivateKey();

			// 获取部署数据
			DeployInfo deployInfo = contractService.getDeployInfoByHashFromChain(hash);
			ContractConfig contract = new ContractConfig();
			contract.setWeIdAddress(deployInfo.getWeIdAddress());
			contract.setIssuerAddress(deployInfo.getAuthorityAddress());
			contract.setSpecificIssuerAddress(deployInfo.getSpecificAddress());
			contract.setEvidenceAddress(deployInfo.getEvidenceAddress());
			contract.setCptAddress(deployInfo.getCptAddress());
			if (StringUtils.isNotBlank(deployInfo.getChainId())) {
				fiscoConfig.setChainId(deployInfo.getChainId());
			} else {
				//兼容历史数据
				fiscoConfig.setChainId(configService.loadConfig().get("chain_id"));
			}
			// 写入全局配置中
			if (FISCO_V2.getVersion() == Integer.parseInt(fiscoConfig.getVersion())) {
				DeployContractV2.putGlobalValue(fiscoConfig, contract, currentPrivateKey.getPrivateKey());
			} else {
				DeployContractV3.putGlobalValue(fiscoConfig, contract, currentPrivateKey.getPrivateKey());
			}
			// 节点启用新hash并停用原hash
			contractService.enableHash(CnsType.DEFAULT, hash, oldHash);
			// 初始化机构cns 目的是当admin首次部署合约未启用evidenceHash之前，用此私钥占用其配置空间，并且vpc2可以检测出已vpc1已配置
			// 此方法为存写入方法，每次覆盖
			WeIdSdkUtils.getDataBucket(CnsType.ORG_CONFING).put(
					fiscoConfig.getCurrentOrgId(),
					WeIdConstant.CNS_EVIDENCE_ADDRESS + 0, "0x0",
					currentPrivateKey.getPrivateKey()
			);
			//重新加载合约地址
			reloadAddress();
			log.info("[enableHash] enable the hash {} successFully.", hash);
			contractService.createWeIdForCurrentUser(DataFrom.WEB);
			return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
		} catch (WeIdBaseException e) {
			log.error("[enableHash] enable the hash error.", e);
			return new ResponseData<>(Boolean.FALSE, e.getErrorCode());
		} catch (Exception e) {
			log.error("[enableHash] enable the hash error.", e);
			return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR.getCode(), e.getMessage());
		}
	}
}
