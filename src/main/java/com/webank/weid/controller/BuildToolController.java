package com.webank.weid.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.dto.CnsInfo;
import com.webank.weid.dto.CptFile;
import com.webank.weid.dto.CptInfo;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.Issuer;
import com.webank.weid.dto.IssuerType;
import com.webank.weid.dto.PojoInfo;
import com.webank.weid.dto.WeIdInfo;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.CheckNodeFace;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.BuildToolService;
import com.webank.weid.service.DeployService;
import com.webank.weid.service.v2.CheckNodeServiceV2;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

@RestController
public class BuildToolController {
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(BuildToolController.class);
    
    private static boolean nodeCheck = false;
    
    private static boolean dbCheck = false;
    
    @Autowired
    BuildToolService buildToolService;
    
    @Autowired
    DeployService deployService;
    
    @Autowired
    ConfigService configService;
    
    @Value("${weid.build.tools.down:false}")
    private String isDownFile;
    
    @GetMapping("/nodeCheckState")
    public boolean nodeCheckState() {
        return nodeCheck;
    }
    
    @GetMapping("/dbCheckState")
    public boolean dbCheckState() {
        return dbCheck;
    }
    
    @GetMapping("/isDownFile")
    public boolean isDownFile() {
        return isDownFile.equals("true");
    }
    
    @GetMapping("/isReady")
    public boolean isReady() {
        return nodeCheck && dbCheck;
    }
    
    @PostMapping("/createAdmin")
    public String createAdmin(HttpServletRequest request) {
        try {
            MultipartFile file = ((MultipartHttpServletRequest) request).getFile("ecdsa");
            String inputPrivateKey = null;
            if (file != null) { //说明是传入的私钥文件
                inputPrivateKey = new String(file.getBytes(),StandardCharsets.UTF_8);
            } else {
                inputPrivateKey = request.getParameter("privateKey");
            }
            return deployService.createAdmin(inputPrivateKey);
        } catch (Exception e) {
            logger.error("[createAdmin] create admin hash error.");
            return BuildToolsConstant.FAIL;
        }
    }
    
    @GetMapping("/checkAdmin")
    public String checkAdmin() {
        return deployService.checkAdmin();
    }
    
    @GetMapping("/deploy")
    public String deploy() {
        logger.info("[deploy] begin load fiscoConfig...");
        try {
            FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
            String hash = deployService.deploy(fiscoConfig, DataFrom.WEB);
            logger.info("[deploy] the hash: {}", hash);
            return hash;
        } catch (Exception e) {
            logger.error("[deploy] the contract depoly error.", e);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @GetMapping("/enableHash/{hash}")
    public boolean enableHash(@PathVariable("hash") String hash) {
        logger.info("[enableHash] begin load fiscoConfig...");
        try {
            //获取原配置
            FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
            //配置启用新hash
            configService.enableHash(hash);
            //节点启用新hash并停用原hash
            deployService.enableHash(hash, fiscoConfig);
            //重新加载合约地址
            configService.reloadAddress();
            logger.info("[enableHash] enable the hash {} successFully.", hash);
            return true;
        } catch (Exception e) {
            logger.error("[enableHash] the contract depoly error.", e);
            return false;
        }
    }
    
    @GetMapping("/deploySystemCpt/{hash}")
    public boolean deploySystemCpt(@PathVariable("hash") String hash) {
        logger.info("[deploySystemCpt] begin deploy System Cpt...");
        try {
            deployService.deploySystemCpt(hash, DataFrom.WEB);
            deployService.clearDeployFile();
            return true;
        } catch (Exception e) {
            logger.error("[deploySystemCpt] the System Cpt depoly error.", e);
            return false;
        }
    }
    
    @GetMapping("/getDeployList")
    public LinkedList<CnsInfo> getDeployList() {
        return deployService.getDeployList();
    }
    
    @GetMapping("/getDeployInfo/{hash}")
    public DeployInfo getDeployInfo(@PathVariable("hash") String hash) {
        return deployService.getDeployInfoByHashFromChain(hash);
    }
    
    @GetMapping("/removeHash/{hash}")
    public String removeHash(@PathVariable("hash") String hash){
        try {
            return deployService.removeHash(hash);
        } catch (Exception e) {
            logger.error("[removeHash] remove the hash error.", e);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @GetMapping("/createWeId")
    public boolean createWeId() {
        try {
            logger.info("[createWeId] begin create weid...");
            buildToolService.createWeId(DataFrom.WEB);
            buildToolService.deleteWeId();
            logger.info("[createWeId] the weid create successfully.");
            return true;
        } catch (Exception e) {
            logger.error("[createWeId] create weId has error.", e);
            return false;
        }
    }
    
    @GetMapping("/getWeIdList")
    public List<WeIdInfo> getWeIdList() {
        return buildToolService.getWeIdList();
    }

    @Description("加载run.config配置")
    @GetMapping("/loadConfig")
    public Map<String, String> loadConfig() {
        return configService.loadConfig();
    }
    
    @Description("节点配置提交")
    @PostMapping("/nodeConfigUpload")
    public String nodeConfigUpload(HttpServletRequest request) {
        logger.info("[nodeConfigUpload] begin upload file...");
        nodeCheck = false;
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        File targetFIle = new File("resources/");
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            File dest = new File(targetFIle.getAbsoluteFile() + "/" + fileName);
            try {
                file.transferTo(dest);
                logger.info("[nodeConfigUpload] the {} upload success", fileName);
            } catch (IOException e) {
                logger.error("[nodeConfigUpload] the {} upload fail", fileName, e);
                return BuildToolsConstant.FAIL;
            }
        }
        logger.info("[nodeConfigUpload] begin update run.config...");
        //更新run.config
        String orgId = request.getParameter("orgId");
        String version = request.getParameter("version");
        String ipPort = request.getParameter("ipPort");
        String chainId = request.getParameter("chainId");
        String profileActive = request.getParameter("cnsProFileActive");
        String privName = request.getParameter("privName");
        if (profileActive.equals("priv")) {
            profileActive = privName;
        }
        //根据模板生成配置文件
        if(configService.processNodeConfig(ipPort, version, orgId, chainId, profileActive)) {
            return BuildToolsConstant.SUCCESS;
        }
        return BuildToolsConstant.FAIL;
    }
    
    @Description("节点检查")
    @GetMapping("/checkNode")
    public boolean checkNode() {
        try {
            nodeCheck = false;
            logger.info("[checkNode] begin check the node...");
            if (!configService.isExistsForProperties()) {
                return false;
            }
            logger.info("[checkNode] begin load the fiscoConfig...");
            FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
            logger.info("[checkNode] the fiscoConfig load successfully.");
            CheckNodeFace checkNode = null;
            if (fiscoConfig.getVersion().startsWith(WeIdConstant.FISCO_BCOS_1_X_VERSION_PREFIX)) {
                
            } else {
                logger.info("[checkNode] the node version is 2.x in your configuration.");
                checkNode = new CheckNodeServiceV2();
            }
            if (checkNode != null && checkNode.check(fiscoConfig)) {
                logger.info("[checkNode] the node check successfull.");
                nodeCheck = true;
            }
        } catch (Exception e) {
            logger.error("[checkNode] checkVersion with exception.", e);
        }
        return nodeCheck;
    }
    
    @Description("数据库配置提交")
    @PostMapping("/submitDbConfig")
    public String submitDbConfig(HttpServletRequest request) {
        logger.info("[submitDbConfig] begin submit dbconfig...");
        dbCheck = false;
        String address = request.getParameter("mysql_address");
        String database = request.getParameter("mysql_database");
        String username = request.getParameter("mysql_username");
        String password = request.getParameter("mysql_password");
        //根据模板生成配置文件
        if(configService.processDbConfig(address, database, username, password)) {
            return BuildToolsConstant.SUCCESS;
        }
        return BuildToolsConstant.FAIL;
    }
    
    @Description("数据库检查")
    @GetMapping("/checkDb")
    public boolean checkDb() {
        dbCheck = false;
        if (!configService.isExistsForProperties()) {
            return false;
        }
        logger.info("[checkDb] begin check the db...");
        dbCheck = configService.checkDb();
        return dbCheck;
    }
    
    @Description("注册issuer")
    @GetMapping("/registerIssuer/{weId}/{name}")
    public String registerIssuer(
        @PathVariable("weId") String weId, 
        @PathVariable("name") String name) {

        try {
            return buildToolService.registerIssuer(weId, name, DataFrom.WEB);
        } catch (Exception e) {
            logger.error("[registerIssuer] register issuer has error.", e);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @GetMapping("/getIssuerList")
    public List<Issuer> getIssuerList() {
        return buildToolService.getIssuerList();
    }
    
    @Description("移除issuer")
    @GetMapping("/removeIssuer/{weId}")
    public String removeIssuer(
        @PathVariable("weId") String weId) {

        try {
            return buildToolService.removeIssuer(weId);
        } catch (Exception e) {
            logger.error("[removeIssuer] remove issuer has error.", e);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @Description("注册issuer type")
    @GetMapping("/registerIssuerType/{type}")
    public String registerIssuerType(@PathVariable("type") String type) {

        try {
            return buildToolService.registerIssuerType(type, DataFrom.WEB);
        } catch (Exception e) {
            logger.error("[registerIssuerType] register issuer type has error.", e);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @Description("查询issuer type列表")
    @GetMapping("/getIssuerTypeList")
    public List<IssuerType> getIssuerTypeList() {
        return buildToolService.getIssuerTypeList();
    }
    
    @Description("查询issuer成员列表")
    @GetMapping("/getAllIssuerInType/{type}")
    public List<String> getAllIssuerInType(@PathVariable("type") String type) {
        return buildToolService.getAllSpecificTypeIssuerList(type);
    }
    
    @Description("向IssuerType中添加成员")
    @GetMapping("/addIssuerIntoIssuerType/{type}/{weId}")
    public String addIssuerIntoIssuerType(
        @PathVariable("type") String type,
        @PathVariable("weId") String weId) {
        return buildToolService.addIssuerIntoIssuerType(type, weId);
    }
    
    @Description("移除IssuerType中的成员")
    @GetMapping("/removeIssuerFromIssuerType/{type}/{weId}")
    public String removeIssuerFromIssuerType(
        @PathVariable("type") String type,
        @PathVariable("weId") String weId) {
        return buildToolService.removeIssuerFromIssuerType(type, weId);
    }
    
    @Description("CPT注册")
    @PostMapping("/registerCpt")
    public String registerCpt(HttpServletRequest request) {
        logger.info("[registerCpt] begin save the cpt json file...");
        String cptJson = request.getParameter("cptJson");
        String fileName = request.getParameter("fileName");
        File targetFIle = new File("output/", fileName);
        FileUtils.writeToFile(cptJson, targetFIle.getAbsolutePath(), FileOperator.OVERWRITE);
        logger.info("[registerCpt] begin register cpt...");
        
        DeployInfo deployInfo = DeployService.getDepolyInfoByHash(ConfigUtils.getCurrentHash());
        String weId = WeIdUtils.convertPublicKeyToWeId(deployInfo.getEcdsaPublicKey());
        String cptId = request.getParameter("cptId");
        try {
            return buildToolService.registerCpt(targetFIle, weId, cptId, DataFrom.WEB);
        } catch (IOException e) {
            logger.error("[registerCpt] register cpt has error.", e);
        } finally {
            FileUtils.delete(targetFIle);
        }
        return BuildToolsConstant.FAIL;
    }
    
    @Description("获取cpt列表")
    @GetMapping("/getCptInfoList")
    public List<CptInfo> getCptInfoList() {
        return buildToolService.getCptInfoList();
    }
    
    @Description("获取cpt Schema信息")
    @GetMapping("/queryCptSchema/{cptId}")
    public String queryCptSchema(@PathVariable("cptId") Integer cptId) {
        return buildToolService.queryCptSchema(cptId);
    }
    
    @Description("将指定cptId转pojo")
    @GetMapping("/cptToPojo")
    public String cptToPojo(
        @RequestParam(value = "cptIds[]") Integer[] cptIds) {
        String pojoId = DataToolUtils.getUuId32();
        try {
            logger.info("[cptToPojo] begin cpt to pojo.");
            for (int i = 0; i < cptIds.length; i++) {
                File cptFile = buildToolService.getCptFile(cptIds[i]);
                buildToolService.generateJavaCodeByCpt(cptFile, cptIds[i], pojoId, "cpt");
            }
            File sourceFile = buildToolService.getSourceFile(pojoId);
            buildToolService.createJar(sourceFile, cptIds, "cpt", DataFrom.WEB);
            return BuildToolsConstant.SUCCESS;
        } catch (Exception e) {
            logger.error("[cptToPojo] cpt to pojo has error.", e);
            buildToolService.deletePojoInfo(pojoId);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @Description("根据policy转pojo")
    @PostMapping("/policyToPojo")
    public String policyToPojo(
        @RequestParam(value = "policy") String policy) {
        String pojoId = DataToolUtils.getUuId32();
        try {
            logger.info("[policyToPojo] begin policy to pojo.");
            logger.info("[policyToPojo] policy = {}.", policy);
            
            ResponseData<List<CptFile>> result = 
                buildToolService.generateCptFileListByPolicy(policy);
            if (result.getErrorCode().intValue() != ErrorCode.SUCCESS.getCode()) {
                return result.getErrorMessage();
            }
            List<Integer> cptIdList = new ArrayList<>();
            for (CptFile cptFile : result.getResult()) {
                File file = new File(cptFile.getCptFileName());
                buildToolService.generateJavaCodeByCpt(file, cptFile.getCptId(), pojoId, "policy");
                cptIdList.add(cptFile.getCptId());
                //cpt生成代码文件后进行删除policy生成的中间cpt文件
                FileUtils.delete(file);
            }
            File sourceFile = buildToolService.getSourceFile(pojoId);
            Integer[] cptIds = cptIdList.toArray(new Integer[cptIdList.size()]);
            buildToolService.createJar(sourceFile, cptIds, "policy", DataFrom.WEB);
            return BuildToolsConstant.SUCCESS;
        } catch (Exception e) {
            logger.error("[cptToPojo] policy to pojo has error.", e);
            buildToolService.deletePojoInfo(pojoId);
            return BuildToolsConstant.FAIL;
        }
    }
    
    @Description("获取POJO列表")
    @GetMapping("/getPojoList")
    public List<PojoInfo> getPojoList() {
        return buildToolService.getPojoList();
    }
    
}
