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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdTools;

/**
 * 主页控制器.
 */
@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
public class CommonController {
	
    @Value("${proxy.target.url}")
    private String proxyTargetUrl;

    @Description("获取版本号")
    @GetMapping("/getVersion")
    @ResponseBody
    public String getVersion() {
        return FileUtils.readFile("VERSION");
    }

    @Description("检查webase是否安装启动")
    @GetMapping("/checkWebase")
    @ResponseBody
    public boolean checkWebase() {
        //1. 检查weabse服务是否启动
        String[] split = proxyTargetUrl.split(":");
        return WeIdTools.isHostConnectable(split[0], Integer.parseInt(split[1]));
    }
    

}
