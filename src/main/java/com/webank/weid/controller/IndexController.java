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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webank.weid.service.DeployService;
import com.webank.weid.util.FileUtils;

/**
 * 主页控制器.
 */
@Controller
@RequestMapping(value = "/")
public class IndexController {
    
    @Autowired
    DeployService deployService;
    
    @RequestMapping(value = "/")
    public String index(){
       String value = deployService.getGuideStatus(); 
        if (StringUtils.isBlank(value)) {
            return "redirect:guide.html";
        } else {
            return "redirect:index.html";
        }
    }
    
    @Description("获取版本号")
    @GetMapping("/getVersion")
    @ResponseBody
    public String getVersion() {
        return FileUtils.readFile("VERSION");
    }
}
