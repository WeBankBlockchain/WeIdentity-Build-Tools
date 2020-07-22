package com.webank.weid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 主页控制器.
 */
@Controller
@RequestMapping(value = "/")
public class IndexController {
    
    @RequestMapping(value = "/")
    public String index(){
        return "redirect:index.html";
    }
}
