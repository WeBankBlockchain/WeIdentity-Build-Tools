package com.webank.weid.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webank.weid.service.DeployService;

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
}
