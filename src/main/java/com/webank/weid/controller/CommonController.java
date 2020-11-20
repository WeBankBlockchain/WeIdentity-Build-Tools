/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
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

package com.webank.weid.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.service.ConfigService;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.HttpClient;
import com.webank.weid.util.WeIdTools;

/**
 * 主页控制器.
 */
@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
public class CommonController {
    
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(BuildToolController.class);
    
    private static boolean callWebaseApi = false;
    
    private static final String WEBASE_URI = "static/weid/weid-build-tools/webase-browser/index.html";

    @Value("${proxy.target.url}")
    private String proxyTargetUrl;
    
    @Autowired
    private ConfigService configService;

    @Description("获取版本号")
    @GetMapping("/getVersion")
    @ResponseBody
    public String getVersion() {
        return FileUtils.readFile("VERSION");
    }

    @Description("检查webase是否安装启动")
    @GetMapping("/checkWebase")
    @ResponseBody
    public boolean checkWebase(HttpServletRequest request) {
        //1. 检查weabse服务是否启动
        String[] split = proxyTargetUrl.split(":");
        boolean result = WeIdTools.isHostConnectable(split[0], Integer.parseInt(split[1]));
        logger.info("[checkWebase] result: {}", result);
        if (result && !callWebaseApi) {
            boolean setRes = setGroupForWebase(request);
            if (setRes) {
                setRes = setNodeForWebase(request);
                if (setRes) {
                    callWebaseApi = true;
                }
            }
        }
        //如果webase服务已启动，则再检查webase页面是否安装（有可能服务是独立启动的，而页面没有安装）
        if (result) {
            URL webaseUri = Thread.currentThread().getContextClassLoader().getResource(WEBASE_URI);
            return webaseUri != null;
        }
        return result;
    }
    
    private boolean setGroupForWebase(HttpServletRequest request) {
        try {
            logger.info("[setGroupForWebase] begin set group for webase.");
            String url = "http://" + proxyTargetUrl +"/fisco-bcos-browser/group/add";
            Map<String, String> data = new HashMap<String, String>();
            FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
            data.put("groupId", fiscoConfig.getGroupId());
            data.put("groupName", "weid-build-tools");
            data.put("groupDesc", StringUtils.EMPTY);
            String doPost = HttpClient.doPost(url, data, false);
            logger.info("[setGroupForWebase]set group for webase result: {}.", doPost);
            return true;
        } catch (Exception e) {
            logger.error("[setGroupForWebase] set group for webase has error.", e);
            return false;
        }
    }
    
    private boolean setNodeForWebase(HttpServletRequest request) {
        try {
            logger.info("[setNodeForWebase] begin set node for webase.");
            String url = "http://" + proxyTargetUrl +"/fisco-bcos-browser/node/add";
            Map<String, Object> data = new HashMap<String, Object>();
            FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
            data.put("groupId", fiscoConfig.getGroupId());
            String[] splitNodes = fiscoConfig.getNodes().split(",");
            List<Map<String, String>> nodes = new ArrayList<>();
            int p2pPort = 30300;
            int rpcPort = 8845;
            for (String string : splitNodes) {
                String[] split = string.split(":");
                Map<String, String> node = new HashMap<String, String>();
                node.put("ip", split[0]);
                node.put("p2pPort", String.valueOf(p2pPort));
                node.put("rpcPort", String.valueOf(rpcPort));
                p2pPort++;
                rpcPort++;
                nodes.add(node);
            }
            data.put("data", nodes);
            String doPost = HttpClient.doPost(url, data, false);
            logger.info("[setNodeForWebase]set node for webase result: {}.", doPost);
            return true;
        } catch (Exception e) {
            logger.error("[setNodeForWebase] set node for webase has error.", e);
            return false;
        }
    }
}
