package com.webank.weid.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.util.HttpClient;
import com.webank.weid.util.WeIdSdkUtils;

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
}
