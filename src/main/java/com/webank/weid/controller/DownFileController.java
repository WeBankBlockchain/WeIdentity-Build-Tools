

package com.webank.weid.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.WeIdInfo;
import com.webank.weid.service.PolicyFactory;
import com.webank.weid.service.WeIdSdkService;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdSdkUtils;

@Slf4j
@RestController
@RequestMapping(value = "/weid/weid-build-tools/")
public class DownFileController {
    
    @Autowired
    private WeIdSdkService weIdSdkService;
    
    @Description("配置文件下载")
    @GetMapping("/downConfig")
    public void downConfig(HttpServletResponse response) {
        log.info("[downConfig] begin to down config...");
        String fileName = "resources.zip";
        boolean result = WeIdSdkUtils.toZip("resources", fileName);
        //说明打包成功，进行下载操作
        if (result) {
            log.info("[downConfig] the file toZip success, begin downloading.");
            File file = new File(fileName);
            response.setContentType("application/force-download");// 设置强制下载不打开         
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file.getAbsoluteFile());
                bis = new BufferedInputStream(fis);
                OutputStream outputStream = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    outputStream.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                outputStream.flush();
                log.info("[downConfig] the file downloading successfully.");
            } catch (Exception e) {
                log.error("[downConfig] the file downloading has error.", e);
            } finally {
                FileUtils.close(bis);
                FileUtils.close(fis);
            }
        } else {
            log.error("[downConfig] the file to zip fail.");
        }
    }
    
    @Description("配置文件下载")
    @GetMapping("/downEcdsaKey/{hash}")
    public void downEcdsaKey(HttpServletResponse response, @PathVariable("hash") String hash) {
        log.info("[downEcdsaKey] begin to down the EcdsaKey...");
        String fileName = BuildToolsConstant.ADMIN_KEY;
        DeployInfo deployInfo = WeIdSdkUtils.getDepolyInfoByHash(hash);
        if (deployInfo != null) {
            down(response, deployInfo.getPrivateKey().getBytes(), fileName);
        } else {
            log.error("[downEcdsaKey] no found the file.");
        }
    }
    
    private void down(HttpServletResponse response, byte[] buffer, String fileName) {
        log.info("[down] begin downloading, fileName is {}.", fileName);
        response.setContentType("application/force-download");// 设置强制下载不打开
        fileName = FileUtils.removeSpecial(fileName);//移除特殊符号
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        try {
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(buffer);
            outputStream.flush();
            log.info("[down] the file downloading successfully.");
        } catch (Exception e) {
            log.error("[down] the file downloading has error.", e);
        }
    }

    @Description("weid私钥下载")
    @GetMapping("/downWeIdEcdsaKey/{address}")
    public void downWeIdEcdsaKey(HttpServletResponse response, @PathVariable("address") String address) {
        log.info("[downWeIdEcdsaKey] begin to down the EcdsaKey...");
        String fileName = address + "_ecdsa_key";
        WeIdInfo weIdInfo = weIdSdkService.getWeIdInfo(address);
        if (weIdInfo != null) {
            down(response, weIdInfo.getEcdsaKey().getBytes(), fileName);
        } else {
            log.error("[downWeIdEcdsaKey] no found the file.");
        }
    }
    
    @Description("weid公钥钥下载")
    @GetMapping("/downWeIdEcdsaPubKey/{address}")
    public void downWeIdEcdsaPubKey(HttpServletResponse response, @PathVariable("address") String address) {
        log.info("[downWeIdEcdsaPubKey] begin to down the EcdsaKey...");
        String fileName = address + "_ecdsa_key.pub";
        WeIdInfo weIdInfo = weIdSdkService.getWeIdInfo(address);
        if (weIdInfo != null) {
            down(response, weIdInfo.getEcdsaPubKey().getBytes(), fileName);
        } else {
            log.error("[downWeIdEcdsaPubKey] no found the file.");
        }
    }
    
    @Description("下载CPT")
    @GetMapping("/downCpt/{cptId}")
    public void downCpt(HttpServletResponse response, @PathVariable("cptId") String cptId) {
        log.info("[downCpt] begin to down the CPT...");
        String fileName = "Cpt" + cptId + ".json";
        String cptJson = weIdSdkService.queryCptSchema(Integer.parseInt(cptId)).getResult();
        down(response, cptJson.getBytes(), fileName);
    }
    
    @Description("下载CPT Pojo Jar")
    @GetMapping("/downPojoJar/{pojoId}")
    public void downPojoJar(HttpServletResponse response, @PathVariable("pojoId") String pojoId) {
        log.info("[downPojoJar] begin to down the CPT JAR...");
        byte[] bytes = WeIdSdkUtils.getJarBytes(pojoId);
        down(response, bytes, BuildToolsConstant.CPT_JAR_NAME);
    }
    
    @Description("下载policy")
    @GetMapping("/downPolicy")
    public void downPolicy(
        HttpServletResponse response, 
        @RequestParam(value = "pojoId") String pojoId,
        @RequestParam(value = "cptIds") String cptStr,
        @RequestParam(value = "policyId") String policyId,
        @RequestParam(value = "policyType") String policyType,
        @RequestParam(value = "fromType") String fromType) {

        try {
            String jarPath = WeIdSdkUtils.getJarFile(pojoId).getAbsolutePath();
            String policy = PolicyFactory.loadJar(jarPath).generate(cptStr, policyType, policyId, fromType);
            down(response, policy.getBytes(), "presentation_policy.json");
        } catch (Exception e) {
            log.error("[downPolicy] down policy has error.", e);
        }
    }
}
