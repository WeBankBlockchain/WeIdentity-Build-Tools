

package com.webank.weid.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.blockchain.protocol.response.ResponseData;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.util.FileUtils;

/**
 * 主页控制器.
 */
@Slf4j
@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
public class CommonController {

    @Autowired
    private WeIdSdkService weIdSdkService;

    @Description("获取版本号")
    @GetMapping("/getVersion")
    @ResponseBody
    public ResponseData<String> getVersion() {
        return new ResponseData<>(FileUtils.readFile("VERSION"), ErrorCode.SUCCESS);
    }

    @Description("获取群组列表")
    @GetMapping("/getAllGroup/{filterMaster}")
    public ResponseData<List<Map<String,String>>> getAllGroup(
            @PathVariable(value = "filterMaster") boolean filterMaster
    ) {
        List<String> allGroup = weIdSdkService.getAllGroup(filterMaster);
        List<Map<String,String>> result = new ArrayList<>();
        if (allGroup != null) {
            for (String string : allGroup) {
                Map<String,String> data = new HashMap<>();
                data.put("value", string);
                result.add(data);
            }
        }
        return new ResponseData<>(result, ErrorCode.SUCCESS);
    }

    @Description("查询群组列表")
    @GetMapping("/getGroupMapping")
    public ResponseData<List<Map<String, String>>> getGroupMapping() {
        log.info("start get group mapping.");
        return weIdSdkService.getGroupMapping();
    }
}
