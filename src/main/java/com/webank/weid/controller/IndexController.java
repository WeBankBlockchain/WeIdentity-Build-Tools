

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
        return "redirect:weid/weid-build-tools/index.html";
    }
    
    @RequestMapping(value = "/weid/weid-build-tools/")
    public String index1(){
        return "redirect:index.html";
    }
    
    @RequestMapping(value = "/weid/weid-build-tools")
    public String index2(){
        return "redirect:weid-build-tools/index.html";
    }
}
