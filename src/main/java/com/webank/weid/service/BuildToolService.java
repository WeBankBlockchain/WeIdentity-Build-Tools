/*
 *       Copyright© (2018-2019) WeBank Co., Ltd.
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

package com.webank.weid.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.sun.codemodel.JCodeModel;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.dto.CptFile;
import com.webank.weid.dto.CptInfo;
import com.webank.weid.dto.Issuer;
import com.webank.weid.dto.IssuerType;
import com.webank.weid.dto.PojoInfo;
import com.webank.weid.dto.WeIdInfo;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.ClaimPolicy;
import com.webank.weid.protocol.base.Cpt;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.HashContract;
import com.webank.weid.protocol.base.PresentationPolicyE;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.base.WeIdPublicKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.protocol.request.RegisterAuthorityIssuerArgs;
import com.webank.weid.protocol.request.RemoveAuthorityIssuerArgs;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.AuthorityIssuerService;
import com.webank.weid.rpc.CptService;
import com.webank.weid.rpc.WeIdService;
import com.webank.weid.service.impl.AuthorityIssuerServiceImpl;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.impl.engine.DataBucketServiceEngine;
import com.webank.weid.service.impl.engine.EngineFactory;
import com.webank.weid.util.CompilerAndJarTools;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

/**
 * @author tonychen 2019/4/11
 */
@Service
public class BuildToolService {

    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(BuildToolService.class);
    public static final String WEID_FILE = "weid";
    private static final String ECDSA_KEY = "ecdsa_key";
    private static final String ECDSA_PUB_KEY = "ecdsa_key.pub";
    private static final String WEID_PATH = "output/create_weid";
    private static final String ISSUER_PATH = "output/issuer";
    private static final String ISSUER_TYPE_PATH = "output/issuer_type";
    private static final String CPT_PATH = "output/cpt";
    private static final String CPT_RESULT_PATH = "output/regist_cpt";
    private static final String POJO_PATH = "output/pojo";
    private static final String PROPERTIES_KEY = "properties";
    private static final String ITEMS_KEY = "items";
    private static final String CLAIM_KEY = "claim";
    
    private WeIdService weIdService;
    private AuthorityIssuerService authorityIssuerService;
    private CptService cptService;
    
    private WeIdService getWeIdService() {
        if (weIdService == null) {
            weIdService = new WeIdServiceImpl();
        }
        return weIdService;
    }
    
    private AuthorityIssuerService getAuthorityIssuerService() {
        if (authorityIssuerService == null) {
            authorityIssuerService = new AuthorityIssuerServiceImpl();
        }
        return authorityIssuerService;
    }
    
    private CptService getCptService() {
        if (cptService == null) {
            cptService = new CptServiceImpl();
        }
        return cptService;
    }
    
    public DataBucketServiceEngine getDataBucket(CnsType cnsType) {
        return EngineFactory.createDataBucketServiceEngine(cnsType);
    }
    
    public String createWeId(DataFrom from) {
        ResponseData<CreateWeIdDataResult> response = getWeIdService().createWeId();
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[CreateWeId] create WeID faild. error code : {}, error msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            return response.getErrorCode() + "-" + response.getErrorMessage();
        }
        CreateWeIdDataResult result = response.getResult();
        String weId = result.getWeId();
        //System.out.println("weid is ------> " + weId);
        String publicKey = result.getUserWeIdPublicKey().getPublicKey();
        String privateKey = result.getUserWeIdPrivateKey().getPrivateKey();
        saveWeId(weId, publicKey, privateKey, from, false);
        return weId;
    }
    
    // 根据私钥创建weId
    public String createWeIdByPrivateKey(String privateKey, DataFrom from) {
        CreateWeIdArgs arg = new CreateWeIdArgs();
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey);
        arg.setWeIdPrivateKey(weIdPrivateKey);
        arg.setPublicKey(DataToolUtils.publicKeyFromPrivate(new BigInteger(privateKey)).toString());
        return this.createWeId(arg, from, false);
    }
    
    // 根据公钥代理创建weId(只有主群组管理员才可以调用)
    public String createWeIdByPublicKey(String publicKey, DataFrom from) {
        // 判断当前使用的cns是否为当前admin部署的
        // 获取当前配置的hash值
        String hash = getMainHash();
        // 获取所有的主合约cns值，从而获取当前cns的部署者
        List<HashContract> result = getDataBucket(CnsType.DEFAULT).getAllHash().getResult();
        // 当前hash的所有者
        String currentHashOwner =  null;
        for (HashContract hashContract : result) {
            if (hashContract.getHash().equals(hash)) {
                currentHashOwner = hashContract.getOwner();
                break;
            }
        }
        // 转换成weid
        String owner = WeIdUtils.convertAddressToWeId(currentHashOwner);
        WeIdAuthentication currentWeIdAuth = getCurrentWeIdAuth();
        // 如果当前weId地址跟 owner地址一致说明是主群组管理员
        if (currentWeIdAuth.getWeId().equals(owner)) {
            WeIdPublicKey weidPublicKey = new WeIdPublicKey();
            weidPublicKey.setPublicKey(publicKey);
            ResponseData<String> response = getWeIdService().delegateCreateWeId(weidPublicKey, currentWeIdAuth);
            if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
                logger.error(
                    "[CreateWeId] create WeID faild. error code : {}, error msg :{}",
                    response.getErrorCode(),
                    response.getErrorMessage());
                return response.getErrorCode() + "-" + response.getErrorMessage();
            }
            String weId = response.getResult();
            saveWeId(weId, publicKey, null, from, false);
            return weId;
        }
        return "create fail: no permission.";
    }
    
    /**
     * 部署admin的weid
     * @param arg 创建weId的参数
     * @param from 数据来源
     * @param isAdmin 是否为管理员
     * @return 返回创建weId结果
     */
    public String createWeId(CreateWeIdArgs arg, DataFrom from, boolean isAdmin) {
        ResponseData<String> response = getWeIdService().createWeId(arg);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[CreateWeId] create WeID faild. error code : {}, error msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            return response.getErrorCode() + "-" + response.getErrorMessage();
        }
        String weId = response.getResult();
        saveWeId(weId, arg.getPublicKey(), arg.getWeIdPrivateKey().getPrivateKey(), from, isAdmin);
        return weId;
    }
    
    /**
     * 检查weid是否存在
     * @param weId 需要被检查的WeId
     * @return 返回weId是否存在
     */
    public boolean checkWeId(String weId) {
        ResponseData<Boolean> response = getWeIdService().isWeIdExist(weId);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[checkWeId] check the WeID faild. error code : {}, error msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            return false;
        }
        return response.getResult();
    }
    
    private void saveWeId(
        String weId, 
        String publicKey, 
        String privateKey, 
        DataFrom from, 
        boolean isAdmin
    ) {
        //write weId, publicKey and privateKey to output dir
        File targetDir = getWeidDir(getWeIdAddress(weId));
        if (from == DataFrom.COMMAND) {
            FileUtils.writeToFile(weId, WEID_FILE); //适配命令输出
        }
        FileUtils.writeToFile(weId, new File(targetDir, WEID_FILE).getAbsolutePath());
        FileUtils.writeToFile(publicKey, new File(targetDir, ECDSA_PUB_KEY).getAbsolutePath());
        if (StringUtils.isNotBlank(privateKey)) {
            FileUtils.writeToFile(privateKey, new File(targetDir, ECDSA_KEY).getAbsolutePath());
        }
        saveWeIdInfo(weId, publicKey, privateKey, from, isAdmin);
    }

    public File getWeidDir(String address) {
        address = FileUtils.getSecurityFileName(address);
        File targetDir = new File(WEID_PATH + "/" + getMainHash() + "/" + address);
        return targetDir;
    }
    
    public String getWeidDir() {
        return new File(WEID_PATH + "/" + getMainHash()).getAbsolutePath();
    }
    
    private String getWeIdAddress(String weId) {
        return WeIdUtils.convertWeIdToAddress(weId);
    }
    
    private void saveWeIdInfo(
        String weId, 
        String publicKey, 
        String privateKey, 
        DataFrom from, 
        boolean isAdmin
    ) {
        logger.info("[saveWeIdInfo] begin to save weid info...");
        //创建部署目录
        File targetDir = getWeidDir(getWeIdAddress(weId));
        File saveFile = new File(targetDir.getAbsoluteFile(), "info");
        FileUtils.writeToFile(
            buildInfo(weId, publicKey, privateKey, from, isAdmin), 
            saveFile.getAbsolutePath(), 
            FileOperator.OVERWRITE);
        logger.info("[saveWeIdInfo] save the weid info successfully.");
    }
    
    private String buildInfo(
        String weId, 
        String publicKey, 
        String privateKey, 
        DataFrom from, 
        boolean isAdmin
    ) {
        WeIdInfo info = new WeIdInfo();
        info.setId(getWeIdAddress(weId));
        long time = System.currentTimeMillis();
        info.setTime(time);
        info.setEcdsaKey(privateKey);
        info.setEcdsaPubKey(publicKey);
        info.setWeId(weId);
        info.setHash(getMainHash());
        info.setFrom(from.name());
        info.setAdmin(isAdmin);
        return DataToolUtils.serialize(info);
    }
    
    public List<WeIdInfo> getWeIdList() {
        List<WeIdInfo> list = new ArrayList<WeIdInfo>();
        String currentHash = getMainHash();
        if (StringUtils.isBlank(currentHash)) {
            return list;
        }
        File targetDir = new File(WEID_PATH + "/" + currentHash);
        if (!targetDir.exists()) {
            return list;
        }
        for (File file : targetDir.listFiles()) {
            File weidFile = new File(file.getAbsoluteFile(),"info");
            String jsonData = FileUtils.readFile(weidFile.getAbsolutePath());
            WeIdInfo info = DataToolUtils.deserialize(jsonData, WeIdInfo.class);
            File issuerFile = new File(ISSUER_PATH + "/" + currentHash, info.getId());
            info.setIssuer(issuerFile.exists());
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }
    
    public WeIdInfo getWeIdInfo(String address) {
        File targetDir = getWeidDir(address);
        File weIdFile = new File(targetDir.getAbsoluteFile(), "info");
        String jsonData = FileUtils.readFile(weIdFile.getAbsolutePath());
        return DataToolUtils.deserialize(jsonData, WeIdInfo.class);
    }
    
    /**
     * 注册issuer.
     * @param weId 被注册成issuer的WeId
     * @param name 注册issuer名
     * @param from 数据来源
     * @return 返回注册结果，true表示成功，false表示失败
     */
    public String registerIssuer(String weId, String name, DataFrom from) {
        logger.info("[registerIssuer] begin register authority issuer..., weId={}, name={}", weId, name);
        RegisterAuthorityIssuerArgs registerAuthorityIssuerArgs = new RegisterAuthorityIssuerArgs();
        AuthorityIssuer authorityIssuer = new AuthorityIssuer();
        authorityIssuer.setName(name);
        authorityIssuer.setWeId(weId);
        authorityIssuer.setAccValue("1");
        authorityIssuer.setCreated(System.currentTimeMillis());
        registerAuthorityIssuerArgs.setAuthorityIssuer(authorityIssuer);
        String hash = getMainHash();
        registerAuthorityIssuerArgs.setWeIdPrivateKey(DeployService.getWeIdPrivateKey(hash));
        ResponseData<Boolean> response = getAuthorityIssuerService().
            registerAuthorityIssuer(registerAuthorityIssuerArgs);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[registerIssuer] register authority issuer {} failed. error code : {}, error msg :{}",
                weId,
                response.getErrorCode(),
                response.getErrorMessage()
            );
            return response.getErrorCode() + "-" + response.getErrorMessage();
        } else {
            logger.info(
                "[registerIssuer] register authority issuer {} success.",
                weId
            );
            //保存issuer
            File weIdFile = new File(ISSUER_PATH + "/" + hash, getWeIdAddress(weId));
            Issuer issuer = buildIssuer(weId, name, hash, from);
            String data = DataToolUtils.serialize(issuer);
            FileUtils.writeToFile(data, weIdFile.getAbsolutePath(), FileOperator.OVERWRITE);
            return BuildToolsConstant.SUCCESS;
        }
    }
    
    private Issuer buildIssuer(String weId, String name, String hash, DataFrom from) {
        Issuer issuer = new Issuer();
        issuer.setHash(hash);
        issuer.setId(getWeIdAddress(weId));
        issuer.setWeId(weId);
        issuer.setName(name);
        long time = System.currentTimeMillis();
        issuer.setTime(time);
        issuer.setFrom(from.name());
        return issuer;
    }
    
    public List<Issuer> getIssuerList() {
        String currentHash = getMainHash();
        List<Issuer> list = new ArrayList<Issuer>();
        if (StringUtils.isBlank(currentHash)) {
            return list;
        }
        File targetDir = new File(ISSUER_PATH + "/" + currentHash);
        if (!targetDir.exists()) {
            return list;
        }
        for (File file : targetDir.listFiles()) {
            //根据weid判断本地是否存在
            String jsonData = FileUtils.readFile(file.getAbsolutePath());
            Issuer info = DataToolUtils.deserialize(jsonData, Issuer.class);
            info.setCanDo(true);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }
    
    public String removeIssuer(String weId) {
        logger.info("[removeIssuer] begin remove authority issuer={} ...", weId);
        RemoveAuthorityIssuerArgs removeAuthorityIssuerArgs = new RemoveAuthorityIssuerArgs();
        removeAuthorityIssuerArgs.setWeId(weId);
        String hash = getMainHash();
        removeAuthorityIssuerArgs.setWeIdPrivateKey(DeployService.getWeIdPrivateKey(hash));

        ResponseData<Boolean> response = getAuthorityIssuerService()
            .removeAuthorityIssuer(removeAuthorityIssuerArgs);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[removeIssuer] remove authority issuer {} faild. error code : {}, error msg :{}",
                weId,
                response.getErrorCode(),
                response.getErrorMessage()
            );
            return response.getErrorCode() + "-" + response.getErrorMessage(); 
        } else {
            logger.info(
                "[removeIssuer] remove authority issuer {} success.",
                weId
            );
            File issuerFile = new File(ISSUER_PATH + "/" + hash, getWeIdAddress(weId));
            FileUtils.delete(issuerFile);
            return BuildToolsConstant.SUCCESS;
        }
    }
    
    public String registerIssuerType(String type, DataFrom from) {
        logger.info("[registerIssuerType] Registering issuer type with best effort: " + type);
        ResponseData<Boolean> response = getAuthorityIssuerService()
            .registerIssuerType(getCurrentWeIdAuth(), type);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[registerIssuerType] register issuer type {} faild. error code : {}, error msg :{}",
                type,
                response.getErrorCode(),
                response.getErrorMessage()
            );
            return response.getErrorCode() + "-" + response.getErrorMessage(); 
        } else {
            logger.info(
                "[registerIssuerType] register issuer type {} success.",
                type
            );
            String id = DataToolUtils.getUuId32();
            //文件落地处理,每注册一个issuerType 记录一个文件
            File issuerTypeFile = new File(ISSUER_TYPE_PATH + "/" + getMainHash(), id);
            IssuerType info = buildIssuerType(type, from);
            String data = DataToolUtils.serialize(info);
            FileUtils.writeToFile(data, issuerTypeFile.getAbsolutePath(), FileOperator.OVERWRITE);
            return BuildToolsConstant.SUCCESS;
        }
    }
    
    private WeIdAuthentication getCurrentWeIdAuth() {
        String hash = getMainHash();
        WeIdPrivateKey weIdPrivateKey = DeployService.getWeIdPrivateKey(hash);
        WeIdAuthentication callerAuth = new WeIdAuthentication();
        callerAuth.setWeIdPrivateKey(weIdPrivateKey);
        callerAuth.setWeId(WeIdUtils.convertPublicKeyToWeId(
            DataToolUtils.publicKeyFromPrivate(new BigInteger(weIdPrivateKey.getPrivateKey())).toString()));
        return callerAuth;
    }
    
    private IssuerType buildIssuerType(String type, DataFrom from) {
        IssuerType info = new IssuerType();
        info.setHash(getMainHash());
        long time = System.currentTimeMillis();
        info.setTime(time);
        info.setType(type);
        info.setFrom(from.name());
        return info;
    }
    
    public List<IssuerType> getIssuerTypeList() {
        String currentHash = getMainHash();
        List<IssuerType> list = new ArrayList<IssuerType>();
        if (StringUtils.isBlank(currentHash)) {
            return list;
        }
        File targetDir = new File(ISSUER_TYPE_PATH + "/" + currentHash);
        if (!targetDir.exists()) {
            return list;
        }
        for (File file : targetDir.listFiles()) {
            //根据weid判断本地是否存在
            String jsonData = FileUtils.readFile(file.getAbsolutePath());
            IssuerType info = DataToolUtils.deserialize(jsonData, IssuerType.class);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }
    
    public List<String> getAllSpecificTypeIssuerList(String issuerType) {
        List<String> list = new ArrayList<String>();
        int num = 10;
        int startIndex = 0;
        while(true) {
            ResponseData<List<String>>  result = getAuthorityIssuerService()
                .getAllSpecificTypeIssuerList(issuerType, startIndex, num);
            if (result.getResult() != null && result.getResult().size() > 0 ) {
                list.addAll(convertToWeId(result.getResult()));
                if (result.getResult().size() < num) {
                    break;
                }
                startIndex = startIndex + num;
            } else {
                break;
            }
        }
        return list;
    }
    
    private List<String> convertToWeId(List<String> data) {
        List<String> list = new ArrayList<String>();
        for (String string : data) {
            list.add(WeIdUtils.convertAddressToWeId(string));
        }
        return list;
    }
    
    public String addIssuerIntoIssuerType(String type, String weId) {
        logger.info("[addIssuerIntoIssuerType] Adding WeIdentity DID {} in {}", weId, type);
        ResponseData<Boolean> response = getAuthorityIssuerService()
            .addIssuerIntoIssuerType(getCurrentWeIdAuth(), type, weId);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[addIssuerIntoIssuerType] add {} into {} fail. error code : {}, error msg :{}",
                weId,
                type,
                response.getErrorCode(),
                response.getErrorMessage()
            );
            return response.getErrorCode() + "-" + response.getErrorMessage(); 
        } else {
            logger.info(
                "[addIssuerIntoIssuerType] add {} into {} success.",
                weId,
                type
            );
            return BuildToolsConstant.SUCCESS;
        }
    }
    
    public String removeIssuerFromIssuerType(String type, String weId) {
        logger.info("[removeIssuerFromIssuerType] Removing WeIdentity DID {} from {}", weId, type);
        ResponseData<Boolean> response = getAuthorityIssuerService()
            .removeIssuerFromIssuerType(getCurrentWeIdAuth(), type, weId);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error(
                "[removeIssuerFromIssuerType] remove {} from {} fail. error code : {}, error msg :{}",
                weId,
                type,
                response.getErrorCode(),
                response.getErrorMessage()
            );
            return response.getErrorCode() + "-" + response.getErrorMessage(); 
        } else {
            logger.info(
                "[removeIssuerFromIssuerType] remove {} from {} success.",
                weId,
                type
            );
            return BuildToolsConstant.SUCCESS;
        }
    }
    
    public String registerCpt(File cptFile, String cptId, DataFrom from) throws IOException {
        return registerCpt(cptFile, getCurrentWeIdAuth(), cptId, from).getMessage();
    }
    
    public CptFile registerCpt(
        File cptFile,
        WeIdAuthentication callerAuth, 
        String cptId,
        DataFrom from) throws IOException {
        
        String fileName = cptFile.getName();
        logger.info("[registerCpt] begin register CPT file: {}", fileName);
        CptFile result = new CptFile();
        result.setCptFileName(fileName);
        if (!fileName.endsWith(".json")) {
            logger.error("the file type error. fileName={}", fileName);
            result.setMessage("file type error.");
            return result;
        }
        JsonNode jsonNode = JsonLoader.fromFile(cptFile);
        String cptJsonSchema = jsonNode.toString();
        CptStringArgs cptStringArgs = new CptStringArgs();
        cptStringArgs.setCptJsonSchema(cptJsonSchema);
        cptStringArgs.setWeIdAuthentication(callerAuth);

        ResponseData<CptBaseInfo> response;
        if (StringUtils.isEmpty(cptId)) {
            response = getCptService().registerCpt(cptStringArgs);
        } else {
            Integer cptId1 = Integer.valueOf(cptId);
            response = getCptService().registerCpt(cptStringArgs, cptId1);
        }
        //System.out.println("[RegisterCpt] result:" + DataToolUtils.serialize(response));
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error("[registerCpt] load config faild. ErrorCode is:{}, msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            result.setMessage(response.getErrorCode() + "-" + response.getErrorMessage());
            return result;
        } else {
            logger.info(
                "[registerCpt] register CPT file:{} result is success. cpt id = {}", 
                fileName, 
                response.getResult().getCptId());
        }
        Integer resultCptId = response.getResult().getCptId();
        String content = new StringBuffer()
            .append(fileName)
            .append("=")
            .append(resultCptId)
            .append("\r\n")
            .toString();
        FileUtils.writeToFile(content, CPT_RESULT_PATH + "/regist_cpt.out", FileOperator.APPEND);
        
        logger.info("[registerCpt] begin save register info.");
        //开始保存CPT数据
        File cptDir = new File(CPT_PATH + "/" + getMainHash() + "/" + resultCptId);
        cptDir.mkdirs();
        //复制CPT文件
        FileUtils.copy(cptFile, new File(cptDir.getAbsolutePath(), cptFile.getName()));       
        //构建cpt数据文件
        String data = DataToolUtils.serialize(
            buildCptInfo(callerAuth.getWeId(), resultCptId, fileName, from));
        File cptInfoFile = new File(cptDir.getAbsoluteFile(), "info");
        FileUtils.writeToFile(data, cptInfoFile.getAbsolutePath(), FileOperator.OVERWRITE);
        //链上查询cpt写链上cpt文件
        String cptSchema = queryCptSchema(resultCptId);
        File file = new File(cptDir.getAbsoluteFile(), getCptFileName(resultCptId));
        FileUtils.writeToFile(cptSchema, file.getAbsolutePath(), FileOperator.OVERWRITE);
        result.setMessage(BuildToolsConstant.SUCCESS);
        result.setCptId(resultCptId);
        return result;
    }
    
    private CptInfo buildCptInfo(String weId, Integer cptId, String cptJsonName, DataFrom from) {
        CptInfo info = new CptInfo();
        info.setHash(getMainHash());
        long time = System.currentTimeMillis();
        info.setTime(time);
        info.setCptId(cptId);
        info.setWeId(weId);
        info.setCptJsonName(cptJsonName);
        info.setFrom(from.name());
        return info;
    }
    
    public List<CptInfo> getCptInfoList() {
        List<CptInfo> list = new ArrayList<CptInfo>();
        String currentHash = getMainHash();
        if (StringUtils.isBlank(currentHash)) {
            return list;
        }
        File targetDir = new File(CPT_PATH + "/" + currentHash);
        if (!targetDir.exists()) {
            return list;
        }
        for (File file : targetDir.listFiles()) {
            File cptInfoFile = new File(file.getAbsoluteFile(),"info");
            String jsonData = FileUtils.readFile(cptInfoFile.getAbsolutePath());
            CptInfo info = DataToolUtils.deserialize(jsonData, CptInfo.class);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }
    
    public String queryCptSchema(Integer cptId) {
        ResponseData<Cpt> response = getCptService().queryCpt(cptId);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.error("[queryCptInfo] query cpt fail. ErrorCode is:{}, msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            return response.getErrorCode() + "-" + response.getErrorMessage();
        } else {
            logger.info(
                "[queryCptInfo] query CPT is success. cpt id = {}", 
                response.getResult().getCptId());
            return ConfigUtils.serializeWithPrinter(response.getResult().getCptJsonSchema());
        }
    }
    
    /**
     * 根据CPT文件生成java源文件
     * @param cptFile cpt文件
     * @param cptId cptId
     * @param pojoId 转换成pojo的Id
     * @param fromType 转换来源
     * @throws Exception 异常抛出
     */
    public void generateJavaCodeByCpt(File cptFile, Integer cptId, String pojoId, String fromType) throws Exception {
        String fileName = cptFile.getName();
        logger.info("[generateJavaCodeByCpt] begin generate for cptFile = {}", fileName);
        JCodeModel codeModel = new JCodeModel();
        try {
            URL source = cptFile.toURI().toURL();
            GenerationConfig config = new DefaultGenerationConfig() {
                @Override
                public boolean isGenerateBuilders() {
                    return true;
                }
                @Override
                public boolean isIncludeAdditionalProperties() {
                    return false;
                }
            };
            SchemaMapper mapper = new SchemaMapper(
                new RuleFactory(config, new NoopAnnotator(), new SchemaStore()), 
                new SchemaGenerator());
            String packageName = "com.weidentity.weid.cpt" + cptId;
            if ("policy".equals(fromType)) {
                packageName = packageName + ".policy";
            }
            
            mapper.generate(codeModel, "Cpt" + cptId, packageName, source);
            File targetFile = getSourceFile(pojoId);
            targetFile.mkdirs();
            codeModel.build(targetFile);
            logger.info("[generateJavaCodeByCpt] generate successfully cptFile = {}", fileName);
        } catch (Exception e) {
            logger.error("[generateJavaCodeByCpt] generate has error. cptFile = {}", fileName, e);
            throw e;
        }
    }
    
    public void deletePojoInfo(String pojoId) {
        FileUtils.deleteAll(new File(getPojoDir().getAbsoluteFile() + "/" + pojoId));
    }
    
    public File getSourceFile(String pojoId) {
        return new File(getPojoDir().getAbsoluteFile() + "/" + pojoId + "/java");
    }
    
    /**
     * 根据java文件生成jar包
     * @param sourceFile 源文件路径
     * @param cptIds jar包含的cpt集合
     * @param fromType  转换来源
     * @param from  数据来源
     * @return 返回是否创建成功
     */
    public boolean createJar(File sourceFile, Integer[] cptIds, String fromType, DataFrom from) {
        logger.info("[createJar] begin create jar.");
        String javaClassPath = sourceFile.getParentFile().getAbsolutePath() + "/classes";  
        String targetPath = sourceFile.getParentFile().getAbsolutePath() + "/" + BuildToolsConstant.CPT_JAR_NAME;  
        try {
            CompilerAndJarTools.instance(sourceFile.getAbsolutePath(), javaClassPath, targetPath).complier().generateJar();
            logger.info("[createJar] create jar successfully.");
            logger.info("[createJar] begin save the pojo info.");
            //保存pojo信息
            File pojoFile = new File(sourceFile.getParentFile().getAbsolutePath(), "info");
            String data = DataToolUtils.serialize(
                buildPojoInfo(sourceFile.getParentFile().getName(), cptIds, fromType, from)
                );
            FileUtils.writeToFile(data, pojoFile.getAbsolutePath(), FileOperator.OVERWRITE);
            return true;
        } catch (Exception e) {
            logger.error("[createJar] create jar has error.", e);
            return false;
        }
    }
    
    public File getCptFile(Integer cptId) {
        return new File(CPT_PATH + "/" + getMainHash() + "/" + cptId,
            getCptFileName(cptId));
    }
    
    private String getCptFileName(Integer cptId) {
        return "Cpt" + String.valueOf(cptId) + ".json";
    }
    
    private PojoInfo buildPojoInfo(String id, Integer[] cptIds, String fromType, DataFrom from) {
        PojoInfo info = new PojoInfo();
        info.setCptIds(cptIds);
        info.setHash(getMainHash());
        info.setId(id);
        info.setTime(System.currentTimeMillis());
        info.setFrom(from.name());
        info.setFromType(fromType);
        return info;
    }
    
    public List<PojoInfo> getPojoList() {
        List<PojoInfo> list = new ArrayList<PojoInfo>();
        String currentHash = getMainHash();
        if (StringUtils.isBlank(currentHash)) {
            return list;
        }
        File targetDir = new File(POJO_PATH + "/" + currentHash);
        if (!targetDir.exists()) {
            return list;
        }
        for (File file : targetDir.listFiles()) {
            File cptInfoFile = new File(file.getAbsoluteFile(),"info");
            String jsonData = FileUtils.readFile(cptInfoFile.getAbsolutePath());
            PojoInfo info = DataToolUtils.deserialize(jsonData, PojoInfo.class);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }
    
    public byte[] getJarBytes(String id) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {
            File jarFile = getJarFile(id);
            in = new BufferedInputStream(new FileInputStream(jarFile));  
            byte[] buffer = new byte[1024];  
            while (true) {  
                int count = in.read(buffer);  
                if (count == -1)  
                    break;  
                bos.write(buffer, 0, count);  
            }
            bos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            logger.error("[getJarBytes] read byte error for down cpt jar.", e);
            return null;
        } finally {
            FileUtils.close(in);
            FileUtils.close(bos);
        }
    }
    
    public File getJarFile(String pojoId) {
        pojoId =  FileUtils.getSecurityFileName(pojoId);
        return new File(getPojoDir().getAbsoluteFile() + "/" + pojoId, 
            BuildToolsConstant.CPT_JAR_NAME);
    }
    
    private File getPojoDir() {
        return new File(POJO_PATH + "/" + getMainHash());
    }

    /**
     * convert policy to cpt pojo.
     *
     * @param policyJson the policy JSON
     * @return 返回披露策略文件中cpt集合
     */
    public ResponseData<List<CptFile>> generateCptFileListByPolicy(String policyJson) {
        List<CptFile> cptFileList = new ArrayList<CptFile>();
        try {
            PresentationPolicyE policyE = PresentationPolicyE.fromJson(policyJson);
            if (policyE == null) {
                logger.error(
                    "[generateCptFileByPolicy]Presentation policy from json = {} is null!", 
                    policyJson);
                return new ResponseData<List<CptFile>>(null, ErrorCode.ILLEGAL_INPUT);
            }
            String policyType = policyE.getPolicyType();
            Map<Integer, ClaimPolicy> policy = policyE.getPolicy();
            for (Map.Entry<Integer, ClaimPolicy> entry : policy.entrySet()) {
                Integer cptId = entry.getKey();
                ResponseData<Cpt> resp = getCptService().queryCpt(cptId);
                Cpt cpt = resp.getResult();
                if (cpt == null || !resp.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
                    logger.error(
                        "[generateCptFileByPolicy] CPT --->{} in presentation policy "
                        + "does not exist!",
                        cptId);
                    return new ResponseData<List<CptFile>>(null,ErrorCode.CPT_NOT_EXISTS);
                }
                Map<String, Object> cptMap = DataToolUtils.clone((HashMap<String, Object>)cpt.getCptJsonSchema()) ;
                Map<String, Object> cptFieldMap = (Map<String, Object>) cptMap.get(PROPERTIES_KEY);
                ClaimPolicy claimPolicy = entry.getValue();
                String fieldsToBeDisclosed = claimPolicy.getFieldsToBeDisclosed();
                HashMap<String, Object> disclosedMap = DataToolUtils
                    .deserialize(fieldsToBeDisclosed, HashMap.class);
                if ("zkp".equals(policyType)) {
                    disclosedMap = (HashMap) disclosedMap.get(CLAIM_KEY);
                }
                generateCptMap(disclosedMap, cptFieldMap);
                if (cptFieldMap.isEmpty()) {//说明全部为不披露
                    return new ResponseData<List<CptFile>>(null, -1, "all attributes are not disclosed");
                }
                String cptJson = DataToolUtils.serialize(cptMap);
                String fileName =
                    "Cpt" + String.valueOf(cpt.getCptId()) + ".json";
                FileUtils.writeToFile(cptJson, fileName, FileOperator.OVERWRITE);
                CptFile cptFile = new CptFile();
                cptFile.setCptFileName(fileName);
                cptFile.setCptId(cptId);
                cptFile.setMessage(ErrorCode.SUCCESS.getCodeDesc());
                cptFileList.add(cptFile);
            }
            return new ResponseData<List<CptFile>>(cptFileList, ErrorCode.SUCCESS);
        } catch (Exception e) {
            logger.error("[CptToPojo] Generate CPT file by policyJson - {} failed.", policyJson, e);
            return new ResponseData<List<CptFile>>(null, ErrorCode.UNKNOW_ERROR);
        }
    }

    private static void generateCptMap(
        Map<String, Object> disclosedMap,
        Map<String, Object> cptMap) {

        for (Map.Entry<String, Object> en : disclosedMap.entrySet()) {
            String k = en.getKey();
            Object v = en.getValue();
            Object cptObj = cptMap.get(k);
            if (v instanceof Map) {
                HashMap cptValue = (HashMap) cptObj;
                processKey(k, (HashMap)v, (HashMap)cptMap, cptValue);
            } else if (v instanceof List) {
                ArrayList discloseList = ((ArrayList) v);
                HashMap dMap = (HashMap) discloseList.get(0);
                HashMap cptValue = (HashMap) cptObj;
                HashMap itemValue = (HashMap) cptValue.get(ITEMS_KEY);
                processKey(k, dMap, (HashMap)cptMap, itemValue);
            } else {
                if (String.valueOf(v).equals("0")) {
                    cptMap.remove(k);
                }
            }
        }
    }

    private static void processKey(String k, HashMap dMap, HashMap cptMap, HashMap cptMapNext) {
        if(cptMapNext.containsKey(PROPERTIES_KEY)) {
            HashMap cptProoperties = (HashMap) cptMapNext.get(PROPERTIES_KEY);
            generateCptMap((HashMap) dMap, cptProoperties);
            if (cptProoperties.isEmpty()) {
                cptMap.remove(k);
            }
        } else {
            generateCptMap((HashMap) dMap, cptMapNext);
            if (cptMapNext.isEmpty()) {
                cptMap.remove(k);
            }
        }
    }
    
    public String getIssuerByWeId(String weIdAddress) {
        String mainHash = getMainHash();
        if (StringUtils.isBlank(mainHash)) {
            return StringUtils.EMPTY;
        }
        AuthorityIssuerService service = this.getAuthorityIssuerService();
        String weId = WeIdUtils.convertAddressToWeId(weIdAddress);
        
        logger.info("[getIssuerByWeId] begin query issuer. weid = {}", weId);
        ResponseData<AuthorityIssuer> response = service.queryAuthorityIssuerInfo(weId);
        if (!response.getErrorCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.warn("[getIssuerByWeId] query issuer fail. ErrorCode is:{}, msg :{}",
                response.getErrorCode(),
                response.getErrorMessage());
            return StringUtils.EMPTY;
        } else {
            return response.getResult().getName();
        }
    }
    
    /**
     * 根据orgId获取org_config里面的hash数据.
     * @param orgId 机构编码
     * @return 返回hash对象信息
     */
    public HashContract getHashFromOrgCns(String orgId) {
        CnsInfo cnsInfo = BaseService.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return null;
        }
        List<HashContract> allHash = getDataBucket(CnsType.ORG_CONFING).getAllHash().getResult();
        for (HashContract hashContract : allHash) {
            if (hashContract.getHash().equals(orgId)) {
                return hashContract;
            }
        }
        return null;
    }
    
    // 判断当前机构配置跟当前私钥是否匹配
    public boolean isMatchThePrivateKey() {
        HashContract hashFromOrgIdCns = getHashFromOrgCns(ConfigUtils.getCurrentOrgId());
        // 如果不存在机构配置，则可以匹配
        if (hashFromOrgIdCns == null) {
            logger.info("[isMatchThePrivateKey] the orgId does not exist in orgConfig cns, default match.");
            return true;//不存在
        }
        WeIdPrivateKey currentPrivateKey = DeployService.getCurrentPrivateKey();
        String publicKey = DataToolUtils.publicKeyFromPrivate(
            new BigInteger(currentPrivateKey.getPrivateKey())).toString();
        String address = "0x" + Keys.getAddress(new BigInteger(publicKey));
        if (address.equals(hashFromOrgIdCns.getOwner())) {
            logger.info("[isMatchThePrivateKey] the orgId is exist in orgConfig cns, match the private key.");
            return true;//存在orgId并且为当前机构所有
        }
        logger.info("[isMatchThePrivateKey] the orgId is exist, but misMatch the private key.");
        return false; //存在机构id不为当前机构所有，私钥不匹配
    }
    
    
    public String getMainHash() {
        CnsInfo cnsInfo = BaseService.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return StringUtils.EMPTY;
        }
        return getDataBucket(CnsType.ORG_CONFING).get(
            WeIdConstant.CNS_GLOBAL_KEY, WeIdConstant.CNS_MAIN_HASH).getResult();
    }
    
    public String getEvidenceHash(String groupId) {
        CnsInfo cnsInfo = BaseService.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return StringUtils.EMPTY;
        }
        String orgId = ConfigUtils.getCurrentOrgId();
        return getDataBucket(CnsType.ORG_CONFING).get(
            orgId, WeIdConstant.CNS_EVIDENCE_HASH + groupId).getResult();
    }
}
