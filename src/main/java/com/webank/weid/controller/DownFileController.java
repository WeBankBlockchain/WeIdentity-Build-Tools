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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.WeIdInfo;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.BuildToolService;
import com.webank.weid.service.DeployService;
import com.webank.weid.service.PolicyFactory;
import com.webank.weid.util.FileUtils;

@RestController
public class DownFileController {
    
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(DownFileController.class);
    
    @Autowired
    ConfigService configService;
    
    @Autowired
    DeployService deployService;
    
    @Autowired
    BuildToolService createWeIdService;
    
    @Autowired
    BuildToolService buildToolService;
    
    @Description("配置文件下载")
    @GetMapping("/downConfig")
    public void downConfig(HttpServletResponse response) {
        logger.info("[downConfig] begin to down config...");
        String fileName = "resources.zip";
        boolean result = configService.toZip("resources", fileName);
        //说明打包成功，进行下载操作
        if (result) {
            logger.info("[downConfig] the file toZip success, begin downloading.");
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
               logger.info("[downConfig] the file downloading successfully.");
               return;
            } catch (Exception e) {
                logger.error("[downConfig] the file downloading has error.", e);
            } finally {
               FileUtils.close(bis);
               FileUtils.close(fis);
            }
        } else {
            logger.error("[downConfig] the file to zip fail.");
        }
    }
    
    @Description("配置文件下载")
    @GetMapping("/downEcdsaKey/{hash}")
    public void downEcdsaKey(HttpServletResponse response, @PathVariable("hash") String hash) {
        logger.info("[downEcdsaKey] begin to down the EcdsaKey...");
        String fileName = "ecdsa_key";
        DeployInfo deployInfo = DeployService.getDepolyInfoByHash(hash);
        if (deployInfo != null) {
            down(response, deployInfo.getEcdsaKey().getBytes(), fileName);
        } else {
            logger.error("[downEcdsaKey] no found the file.");
        }
    }
    
    private void down(HttpServletResponse response, byte[] buffer, String fileName) {
        logger.info("[down] begin downloading, fileName is {}.", fileName);
        response.setContentType("application/force-download");// 设置强制下载不打开
        fileName = FileUtils.removeSpecial(fileName);//移除特殊符号
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        try {
           OutputStream outputStream = response.getOutputStream();
           outputStream.write(buffer);
           outputStream.flush();
           logger.info("[down] the file downloading successfully.");
           return;
        } catch (Exception e) {
            logger.error("[down] the file downloading has error.", e);
        }
    }

    @Description("weid私钥下载")
    @GetMapping("/downWeIdEcdsaKey/{address}")
    public void downWeIdEcdsaKey(HttpServletResponse response, @PathVariable("address") String address) {
        logger.info("[downWeIdEcdsaKey] begin to down the EcdsaKey...");
        String fileName = address + "_ecdsa_key";
        WeIdInfo weIdInfo = createWeIdService.getWeIdInfo(address);
        if (weIdInfo != null) {
            down(response, weIdInfo.getEcdsaKey().getBytes(), fileName);
        } else {
            logger.error("[downWeIdEcdsaKey] no found the file.");
        }
    }
    
    @Description("weid公钥钥下载")
    @GetMapping("/downWeIdEcdsaPubKey/{address}")
    public void downWeIdEcdsaPubKey(HttpServletResponse response, @PathVariable("address") String address) {
        logger.info("[downWeIdEcdsaPubKey] begin to down the EcdsaKey...");
        String fileName = address + "_ecdsa_key.pub";
        WeIdInfo weIdInfo = createWeIdService.getWeIdInfo(address);
        if (weIdInfo != null) {
            down(response, weIdInfo.getEcdsaPubKey().getBytes(), fileName);
        } else {
            logger.error("[downWeIdEcdsaPubKey] no found the file.");
        }
    }
    
    @Description("下载CPT")
    @GetMapping("/downCpt/{cptId}")
    public void downCpt(HttpServletResponse response, @PathVariable("cptId") String cptId) {
        logger.info("[downCpt] begin to down the CPT...");
        String fileName = "Cpt" + cptId + ".json";
        String cptJson = buildToolService.queryCptSchema(Integer.parseInt(cptId));
        down(response, cptJson.getBytes(), fileName);
    }
    
    @Description("下载CPT Pojo Jar")
    @GetMapping("/downPojoJar/{pojoId}")
    public void downPojoJar(HttpServletResponse response, @PathVariable("pojoId") String pojoId) {
        logger.info("[downPojoJar] begin to down the CPT JAR...");
        byte[] bytes = buildToolService.getJarBytes(pojoId);
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
            String jarPath = buildToolService.getJarFile(pojoId).getAbsolutePath();
            String policy = PolicyFactory.loadJar(jarPath).generate(cptStr, policyType, policyId, fromType);
            down(response, policy.getBytes(), "presentation_policy.json");
        } catch (Exception e) {
            logger.error("[downPolicy] down policy has error.", e);
        }
    }
}
