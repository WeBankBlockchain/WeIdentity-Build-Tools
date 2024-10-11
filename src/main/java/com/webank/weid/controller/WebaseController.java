package com.webank.weid.controller;

import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.dto.ContractSolInfo;
import com.webank.weid.dto.PageDto;
import com.webank.weid.dto.webase.request.RegisterInfo;
import com.webank.weid.dto.webase.response.BaseListResponse;
import com.webank.weid.dto.webase.response.BaseResponse;
import com.webank.weid.dto.webase.response.NodeInfo;
import com.webank.weid.dto.webase.response.UserInfo;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.ContractService;
import com.webank.weid.service.WeBaseService;
import com.webank.weid.util.HttpClient;
import com.webank.weid.util.WeIdSdkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * webase 相关api接口
 */

@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
@Slf4j
public class WebaseController {

	private static boolean callWebaseApi = false;

	private static final String WEBASE_URI = "static/weid/weid-build-tools/webase-browser/index.html";

	@Value("${proxy.target.url}")
	private String proxyTargetUrl;
	
	@Autowired
	private WeBaseService weBaseService;
	
	@Autowired
    private ConfigService configService;
	
	@Autowired
	private ContractService contractService;
	
	@Description("检查webase是否安装启动")
	@GetMapping("/checkWebase")
	@ResponseBody
	public ResponseData<Boolean> checkWebase(HttpServletRequest request) {
		//1. 检查weabse服务是否启动
		String[] split = proxyTargetUrl.split(":");
		boolean result = WeIdSdkUtils.isHostConnectable(split[0], Integer.parseInt(split[1]));
		log.info("[checkWebase] result: {}", result);
		if (result && !callWebaseApi) {
			boolean setRes = setGroupForWebase(request);
			if (setRes) {
				setRes = setNodeForWebase();
				if (setRes) {
					callWebaseApi = true;
				}
			}
		}
		//如果webase服务已启动，则再检查webase页面是否安装（有可能服务是独立启动的，而页面没有安装）
		if (result) {
			URL webaseUri = Thread.currentThread().getContextClassLoader().getResource(WEBASE_URI);
			return new ResponseData<>(webaseUri != null, ErrorCode.SUCCESS);
		}
		return new ResponseData<>(Boolean.FALSE, ErrorCode.SUCCESS);
	}

	private boolean setGroupForWebase(HttpServletRequest request) {
		try {
			log.info("[setGroupForWebase] begin set group for webase.");
			String url = "http://" + proxyTargetUrl +"/fisco-bcos-browser/group/add";
			Map<String, String> data = new HashMap<>();
			data.put("groupId", WeIdSdkUtils.loadNewFiscoConfig().getGroupId());
			data.put("groupName", "weid-build-tools");
			data.put("groupDesc", StringUtils.EMPTY);
			String doPost = HttpClient.doPost(url, data, false);
			log.info("[setGroupForWebase]set group for webase result: {}.", doPost);
			return true;
		} catch (Exception e) {
			log.error("[setGroupForWebase] set group for webase has error.", e);
			return false;
		}
	}

	private boolean setNodeForWebase() {
		try {
			log.info("[setNodeForWebase] begin set node for webase.");
			String url = "http://" + proxyTargetUrl +"/fisco-bcos-browser/node/add";
			Map<String, Object> data = new HashMap<String, Object>();
			FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
			data.put("groupId", fiscoConfig.getGroupId());
			String[] splitNodes = fiscoConfig.getNodes().split(",");
			List<Map<String, String>> nodes = new ArrayList<>();
			int p2pPort = 30300;
			int rpcPort = 8845;
			for (String string : splitNodes) {
				String[] split = string.split(":");
				Map<String, String> node = new HashMap<>();
				node.put("ip", split[0]);
				node.put("p2pPort", String.valueOf(p2pPort));
				node.put("rpcPort", String.valueOf(rpcPort));
				p2pPort++;
				rpcPort++;
				nodes.add(node);
			}
			data.put("data", nodes);
			String doPost = HttpClient.doPost(url, data, false);
			log.info("[setNodeForWebase]set node for webase result: {}.", doPost);
			return true;
		} catch (Exception e) {
			log.error("[setNodeForWebase] set node for webase has error.", e);
			return false;
		}
	}
	
	@Description("weID向WeBase注册服务")
    @PostMapping("/webase/registerApp")
    @ResponseBody
    public ResponseData<Boolean> registerApp(@RequestBody RegisterInfo registerInfo) {
        // 1. 向WeBase注册服务
        BaseResponse<Object> result = weBaseService.registerService(registerInfo);
        // 2. 如果服务注册成功，则从Webase拉取证书，如果失败则直接返回
        if (result.getCode() != 0) {
            return new ResponseData<>(Boolean.FALSE, result.getCode(),  result.getMessage());
        }
        log.info("[registerApp] the app register result is {}", result);
        // 保存WeBae信息
        weBaseService.saveRegisterInfo(registerInfo);
        // 同步证书
        boolean syncResult = weBaseService.syncCertificate();
        log.info("[registerApp] the result of sync certificate is {}", syncResult);
        if (syncResult) {
            Integer encryptType = weBaseService.queryNodeType().getData();
            // 导入合约文件到WeBase
            List<ContractSolInfo> contractList = WeIdSdkUtils.getContractList(encryptType);
            String contractVersion =  contractService.getContractVersion();
            log.info("[registerApp] contract szie = {}, contract version = {}", contractList.size(), contractVersion);
            syncResult = weBaseService.syncContractToWeBase(contractList, contractVersion);
            log.info("[registerApp] the result of sync contract is {}", syncResult);
        }
        return new ResponseData<>(syncResult, ErrorCode.SUCCESS);
    }

    @Description("更新为非WeBase集成模式")
    @PostMapping("/webase/setNoWeBaseMode")
    @ResponseBody
    public ResponseData<Boolean> setNoWeBaseMode() {
        weBaseService.setNoWeBaseMode();
        return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
    }

    @Description("判断是否集成WeBase")
    @GetMapping("/webase/isIntegrateWebase")
    @ResponseBody
    public ResponseData<Boolean> isIntegrateWebase() {
        return new ResponseData<>(weBaseService.isIntegrateWebase(), ErrorCode.SUCCESS);
    }

    @Description("获取节点类型")
    @GetMapping("/webase/queryNodeType")
    @ResponseBody
    public ResponseData<Integer> queryNodeType() {
        try {
            BaseResponse<Integer> result = weBaseService.queryNodeType();
            if (result.getCode() != 0) {
                return new ResponseData<Integer>(result.getData(), result.getCode(),  result.getMessage());
            }
            return new ResponseData<Integer>(result.getData(), ErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("[queryNodeType] query node type has error.", e);
            return new ResponseData<>(0, ErrorCode.BASE_ERROR);
        }
    }

    @Description("获取节点列表")
    @GetMapping("/webase/queryNodeList")
    @ResponseBody
    public ResponseData<List<NodeInfo>> queryNodeList() {
        try {
            BaseResponse<List<NodeInfo>> result = weBaseService.queryNodeList();
            if (result.getCode() != 0) {
                return new ResponseData<List<NodeInfo>>(result.getData(), result.getCode(),  result.getMessage());
            }
            return new ResponseData<List<NodeInfo>>(result.getData(), ErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("[queryNodeType] query node type has error.", e);
            return new ResponseData<>(null, ErrorCode.BASE_ERROR);
        }
    }

    @Description("获取账户列表")
    @GetMapping("/webase/queryUserList")
    @ResponseBody
    public ResponseData<PageDto<UserInfo>> queryUserList(
        @RequestParam(value = "pageIndex") int pageIndex,
        @RequestParam(value = "pageSize") int pageSize
    ) {
        try {
            PageDto<UserInfo> pageDto = new PageDto<UserInfo>(pageIndex, pageSize);
            UserInfo userInfo = new UserInfo();
            userInfo.setHasPk(1);
            userInfo.setGroupId(configService.getMasterGroupId());
            pageDto.setQuery(userInfo);
            BaseListResponse<List<UserInfo>> result = weBaseService.queryUserList(pageDto);
            if (result.getCode() != 0) {
                return new ResponseData<PageDto<UserInfo>>(pageDto, result.getCode(),  result.getMessage());
            }
            pageDto.setDataList(result.getData());
            pageDto.setAllCount(result.getTotalCount());
            return new ResponseData<PageDto<UserInfo>>(pageDto, ErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("[queryNodeType] query node type has error.", e);
            return new ResponseData<>(null, ErrorCode.BASE_ERROR);
        }
    }

    @Description("根据用户Id创建账户")
    @PostMapping("/webase/createAdmin")
    @ResponseBody
    public ResponseData<String> createAdmin(@RequestParam(value = "userId") int userId) {
        try {
            String result = weBaseService.createAdmin(userId);
            if (StringUtils.isBlank(result)) {
                return new ResponseData<>(StringUtils.EMPTY, ErrorCode.UNKNOW_ERROR);
            }
            return new ResponseData<String>(result, ErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("[createAdmin] create Admin by userId = {}.", userId, e);
            return new ResponseData<>(StringUtils.EMPTY, ErrorCode.BASE_ERROR);
        }
    }
}
