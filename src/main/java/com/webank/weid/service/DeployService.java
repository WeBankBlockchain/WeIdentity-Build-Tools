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

package com.webank.weid.service;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.deploy.DeployEvidence;
import com.webank.weid.contract.deploy.v1.DeployContractV1;
import com.webank.weid.contract.deploy.v2.DeployContractV2;
import com.webank.weid.contract.v2.WeIdContract;
import com.webank.weid.dto.CnsInfo;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.ShareInfo;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.HashContract;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.fisco.WeServerUtils;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.impl.engine.DataBucketServiceEngine;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

@Service
public class DeployService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeployService.class);
    private static final String HASH = "hash";
    private static final String ECDSA_KEY = "ecdsa_key";
    private static final String ECDSA_PUB_KEY = "ecdsa_key.pub";
    private static final String ROLE_FILE = "role";
    private static final String GUIDE_FILE = "guide";
    private static final String ADMIN_PATH = "output/admin";
    public static final String DEPLOY_PATH = "output/deploy";
    public static final String SHARE_PATH = "output/share";
    private static final String OTHER_PATH = "output/other";
    
    private static final String AUTH_ADDRESS_FILE_NAME = "authorityIssuer.address";
    private static final String CPT_ADDRESS_FILE_NAME = "cptController.address";
    private static final String WEID_ADDRESS_FILE_NAME = "weIdContract.address";
    private static final String EVID_ADDRESS_FILE_NAME = "evidenceController.address";
    private static final String SPECIFIC_ADDRESS_FILE_NAME = "specificIssuer.address";
    
    static {
        FileUtils.mkdirs(ADMIN_PATH);
        FileUtils.mkdirs(DEPLOY_PATH);
    }
    
    private BuildToolService buildToolService = new BuildToolService();
  
    public String deploy(FiscoConfig fiscoConfig, DataFrom from) {
        logger.info("[deploy] begin depoly contract...");
        File targetDir = new File(ADMIN_PATH, ECDSA_KEY);
        String privateKey = null;
        if (targetDir.exists()) {
            privateKey = FileUtils.readFile(targetDir.getAbsolutePath());
        }
        if (fiscoConfig.getVersion().startsWith(WeIdConstant.FISCO_BCOS_1_X_VERSION_PREFIX)) {
            DeployContractV1.deployContract(privateKey);
        } else {
            // false: 表示不即时注册hash到org_config CNS中
            DeployContractV2.deployContract(privateKey, fiscoConfig, false);
        }
        logger.info("[deploy] the contract depoly finish.");
        //开始保存文件
        //将私钥移动到/output/admin中
        copyEcdsa();
        return saveDeployInfo(fiscoConfig, from);
    }
    
    private void copyEcdsa() {
        logger.info("[copyEcdsa] begin copy the ecdsa to admin...");
        File ecdsaFile = new File(ECDSA_KEY);
        File targetDir = new File(ADMIN_PATH);
        FileUtils.copy(ecdsaFile, new File(targetDir.getAbsoluteFile(), ECDSA_KEY));
        
        File ecdsaPubFile = new File(ECDSA_PUB_KEY);
        FileUtils.copy(ecdsaPubFile, new File(targetDir.getAbsoluteFile(), ECDSA_PUB_KEY));
        logger.info("[copyEcdsa] the ecdsa copy successfully.");
    }
    
    private String saveDeployInfo(FiscoConfig fiscoConfig, DataFrom from) {
        logger.info("[saveDeployInfo] begin to save deploy info...");
        //创建部署目录
        String hash = FileUtils.readFile(HASH);
        saveDeployInfo(buildInfo(fiscoConfig, hash, from));
        logger.info("[saveDeployInfo] save the deploy info successfully.");
        return hash;
    }
    
    public void clearDeployFile() {
        //清理合約地址文件
        FileUtils.delete(AUTH_ADDRESS_FILE_NAME);
        FileUtils.delete(WEID_ADDRESS_FILE_NAME);
        FileUtils.delete(CPT_ADDRESS_FILE_NAME);
        FileUtils.delete(EVID_ADDRESS_FILE_NAME);
        FileUtils.delete(SPECIFIC_ADDRESS_FILE_NAME);
        FileUtils.delete(ECDSA_KEY); 
        FileUtils.delete(ECDSA_PUB_KEY); 
        FileUtils.delete(HASH); 
        FileUtils.delete(BuildToolService.WEID_FILE); 
    }
    
    private  void saveDeployInfo(DeployInfo info) {
        File deployDir = new File(DEPLOY_PATH);
        File deployFile = new File(deployDir.getAbsoluteFile(), info.getHash());
        String jsonData = DataToolUtils.serialize(info);
        FileUtils.writeToFile(jsonData, deployFile.getAbsolutePath(), FileOperator.OVERWRITE);
    }
    
    private DeployInfo buildInfo(FiscoConfig fiscoConfig, String hash, DataFrom from) {
        DeployInfo info = new DeployInfo();
        info.setHash(hash);
        long time = System.currentTimeMillis();
        info.setTime(time);
        info.setEcdsaKey(FileUtils.readFile(ECDSA_KEY));
        BigInteger privateKey = new BigInteger(info.getEcdsaKey());
        info.setEcdsaPublicKey(DataToolUtils.publicKeyFromPrivate(privateKey).toString());
        try {
            info.setNodeVerion(BaseService.getVersion());
        } catch (Exception e) {
            info.setNodeVerion(fiscoConfig.getVersion()); 
        }
        info.setNodeAddress(fiscoConfig.getNodes());
        info.setAuthorityAddress(FileUtils.readFile(AUTH_ADDRESS_FILE_NAME));
        info.setCptAddress(FileUtils.readFile(CPT_ADDRESS_FILE_NAME));
        info.setWeIdAddress(FileUtils.readFile(WEID_ADDRESS_FILE_NAME));
        info.setEvidenceAddress(FileUtils.readFile(EVID_ADDRESS_FILE_NAME));
        info.setSpecificAddress(FileUtils.readFile(SPECIFIC_ADDRESS_FILE_NAME));
        info.setChainId(fiscoConfig.getChainId());
        info.setContractVersion(getVersionByClass(WeIdContract.class));
        info.setWeIdSdkVersion(getVersionByClass(WeIdServiceImpl.class));
        info.setFrom(from.name());
        return info;
    }
    
    private static String getVersionByClass(Class<?> clazz) {
        String jarFile = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarFile = jarFile.substring(jarFile.lastIndexOf("/")+1);
        return jarFile.substring(0, jarFile.lastIndexOf("."));
    }
    
    public LinkedList<CnsInfo> getDeployList() {
        LinkedList<CnsInfo> dataList = new LinkedList<CnsInfo>();
        //如果没有部署databuket则直接返回
        org.fisco.bcos.web3j.precompile.cns.CnsInfo cnsInfo = BaseService.getBucketByCns(CnsType.DEFAULT);
        if (cnsInfo == null) {
            return dataList;
        }
        //如果没有部署databuket则直接返回
        cnsInfo = BaseService.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return dataList;
        }
        String currentHash = buildToolService.getMainHash();
        List<HashContract> result = buildToolService.getDataBucket(CnsType.DEFAULT).getAllHash().getResult();
        String roleType = this.getRoleType();
        Map<String, String> cache = new HashMap<String, String>();
        for (HashContract hashContract : result) {
            CnsInfo cns = new CnsInfo();
            cns.setHash(hashContract.getHash());
            cns.setTime(hashContract.getTime());
            cns.setWeId(hashContract.getOwner());
            cns.setEnable(hashContract.getHash().equals(currentHash));
            cns.setRoleType(roleType);
            //如果当前角色为非管理员, 则只显示已启用数据，未启用数据直接跳过
            if ("2".equals(roleType) && !cns.isEnable()) {
                continue;
            }
            
            DeployInfo deployInfo = getDepolyInfoByHash(cns.getHash());
            if (deployInfo != null && !deployInfo.isDeploySystemCpt()) {
                cns.setNeedDeployCpt(true);
            }
            if (cns.isEnable() && cns.isNeedDeployCpt()) { //如果是启用状态
                cns.setShowDeployCptBtn(true);
            }
            // 查询此部署账户的权威机构名
            if(cache.containsKey(cns.getWeId())) {
                cns.setIssuer(cache.get(cns.getWeId()));
            } else {
                String issuer = buildToolService.getIssuerByWeId(cns.getWeId());
                cns.setIssuer(issuer);
                cache.put(cns.getWeId(), issuer);
            }
            dataList.add(cns);
        }
        return dataList;
    }
    
    /**
     * 根据hash从链上获取地址信息.
     * @param hash 获取部署数据的hash值
     * @return 返回当前hash的部署信息
     */
    public DeployInfo getDeployInfoByHashFromChain(String hash) {
        //判断本地是否有次hash记录
        DeployInfo deploy = getDepolyInfoByHash(hash);
        if (deploy != null) {
            deploy.setLocal(true);//本地有
        } else {
            deploy = new DeployInfo();
            String weIdAddr = getValueFromCns(CnsType.DEFAULT, hash, WeIdConstant.CNS_WEID_ADDRESS);
            String authAddr = getValueFromCns(CnsType.DEFAULT, hash, WeIdConstant.CNS_AUTH_ADDRESS);
            String cptAddr = getValueFromCns(CnsType.DEFAULT, hash, WeIdConstant.CNS_CPT_ADDRESS);
            String specificAddr = getValueFromCns(CnsType.DEFAULT, hash, WeIdConstant.CNS_SPECIFIC_ADDRESS);
            String evidenceAddr = getValueFromCns(CnsType.DEFAULT, hash, WeIdConstant.CNS_EVIDENCE_ADDRESS);
            String chainId = getValueFromCns(CnsType.DEFAULT, hash, WeIdConstant.CNS_CHAIN_ID);
            deploy.setChainId(chainId);
            deploy.setHash(hash);
            deploy.setWeIdAddress(weIdAddr);
            deploy.setAuthorityAddress(authAddr);
            deploy.setCptAddress(cptAddr);
            deploy.setSpecificAddress(specificAddr);
            deploy.setEvidenceAddress(evidenceAddr);
            deploy.setFrom("其他机构");
        }
        return deploy;
    }
    
    private String getValueFromCns(CnsType cnsType, String hash, String key) {
        return buildToolService.getDataBucket(cnsType).get(hash, key).getResult();
    }
    
    private static File getDeployFileByHash(String hash) {
        hash = FileUtils.getSecurityFileName(hash);
        return new File(DEPLOY_PATH, hash);
    }
    
    public static DeployInfo getDepolyInfoByHash(String hash) {
        File deployDir = getDeployFileByHash(hash);
        if (deployDir.exists()) {
            String jsonData = FileUtils.readFile(deployDir.getAbsolutePath());
            return DataToolUtils.deserialize(jsonData, DeployInfo.class);
        } else {
            return null;
        }
    }
    
    public boolean deploySystemCpt(String hash, DataFrom from) {
        DeployInfo deployInfo = getDepolyInfoByHash(hash);
        if (deployInfo == null) {
            logger.error("[deploySystemCpt] can not found the admin ECDSA.");
            return false;
        }
        //注册weid
        createWeId(deployInfo, from, true);
        logger.info("[deploySystemCpt] begin register systemCpt...");
        //部署系统CPT, 
        boolean result = registerSystemCpt(deployInfo);
        if (result) {
            logger.info("[deploySystemCpt] systemCpt is registed and to save.");
            //更新部署信息,表示已部署系统CPT 
            deployInfo.setDeploySystemCpt(true);
            saveDeployInfo(deployInfo);
            return true;
        } else {
            logger.error("[deploySystemCpt] systemCpt is register fail.");
            return false;
        }
    }
    
    private boolean registerSystemCpt(DeployInfo deployInfo) {
        CptStringArgs cptStringArgs = new CptStringArgs();
        WeIdAuthentication weIdAuthentication = new WeIdAuthentication();
        BigInteger privateKey = new BigInteger(deployInfo.getEcdsaKey());
        String weId = WeIdUtils.convertPublicKeyToWeId(deployInfo.getEcdsaPublicKey());
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        weIdPrivateKey.setPrivateKey(privateKey.toString());
        weIdAuthentication.setWeIdPrivateKey(weIdPrivateKey);
        weIdAuthentication.setWeId(weId);
        cptStringArgs.setWeIdAuthentication(weIdAuthentication);

        List<Integer> cptIdList = Arrays.asList(101, 102, 103, 106, 107, 110, 111);
        CptServiceImpl cptService = new CptServiceImpl();
        for (Integer cptId : cptIdList) {
            String cptJsonSchema = DataToolUtils.generateDefaultCptJsonSchema(cptId);
            if (cptJsonSchema.isEmpty()) {
                logger.info("[registerSystemCpt] Cannot generate CPT json schema with ID: " + cptId);
                return false;
            }
            cptStringArgs.setCptJsonSchema(cptJsonSchema);
            ResponseData<CptBaseInfo> responseData = cptService.registerCpt(cptStringArgs, cptId);
            if (responseData.getResult() == null) {
                logger.info("[registerSystemCpt] Register System CPT failed with ID: " + cptId);
                return false;
            }
        }
        return true;
    }
    
    private void createWeId(DeployInfo deployInfo, DataFrom from, boolean isAdmin) {
        logger.info("[createWeId] begin createWeid for admin");
        CreateWeIdArgs arg = new CreateWeIdArgs();
        arg.setPublicKey(deployInfo.getEcdsaPublicKey());
        WeIdPrivateKey pkey = new WeIdPrivateKey();
        pkey.setPrivateKey(deployInfo.getEcdsaKey());
        arg.setWeIdPrivateKey(pkey);
        String weId = WeIdUtils.convertPublicKeyToWeId(arg.getPublicKey());
        boolean checkWeId = buildToolService.checkWeId(weId);
        if (!checkWeId) {
            String result = buildToolService.createWeId(arg, from, isAdmin);
            logger.info("[createWeId]  createWeId for admin result = {}", result);
            System.out.println("createWeId for admin result = " + result);
        } else {
            logger.info("[createWeId] the weId is exist."); 
        }
    }
    
    /**
     * 给当前账户创建WeId.
     * @param from 创建来源
     */
    public void createWeIdForCurrentUser(DataFrom from) {
        //判断当前私钥账户对应的weid是否存在，如果不存在则创建weId
        CreateWeIdArgs arg = new CreateWeIdArgs();
        arg.setWeIdPrivateKey(getCurrentPrivateKey());
        arg.setPublicKey(DataToolUtils.publicKeyFromPrivate(new BigInteger(arg.getWeIdPrivateKey().getPrivateKey())).toString());
        String weId = WeIdUtils.convertPublicKeyToWeId(arg.getPublicKey());
        logger.info("[createWeIdForCurrentUser] the current weId is = {}", weId);
        boolean checkWeId = buildToolService.checkWeId(weId);
        if (!checkWeId) {
            logger.info("[createWeIdForCurrentUser] the current weId is not exist and begin create.");
            String result = buildToolService.createWeId(arg, from, true);
            logger.info("[createWeIdForCurrentUser] create weid for current account result = {}", result);
        } else {
            logger.info("[createWeIdForCurrentUser] the current weId is exist."); 
        }
    }
    
    public static WeIdPrivateKey getCurrentPrivateKey() {
        WeIdPrivateKey weIdPrivate = new WeIdPrivateKey();
        File targetDir = new File(ADMIN_PATH, ECDSA_KEY);
        weIdPrivate.setPrivateKey(FileUtils.readFile(targetDir.getAbsolutePath()));
        return weIdPrivate;
    }
    
    public static WeIdPrivateKey getWeIdPrivateKey(String hash) {
        //根据部署编码获取当次部署的私钥
        DeployInfo deployInfo = getDepolyInfoByHash(hash);
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        if (deployInfo != null) {
            weIdPrivateKey.setPrivateKey(deployInfo.getEcdsaKey());
            return weIdPrivateKey;
        }
        return getCurrentPrivateKey();
    }
    
    public void enableHash(CnsType cnsType, String hash, String oldHash) {
        logger.info("[enableHash] begin enable the hash: {}", hash);
        //启用新hash
        WeIdPrivateKey privateKey = getWeIdPrivateKey(hash);
        ResponseData<Boolean> enableHash = buildToolService.getDataBucket(cnsType).enableHash(hash, privateKey);
        logger.info("[enableHash] enable the hash {} --> result: {}", hash, enableHash);
        //如果原hash不为空，则停用原hash
        if (StringUtils.isNotBlank(oldHash)) {
            ResponseData<Boolean> disableHash = buildToolService.getDataBucket(cnsType).disableHash(oldHash, privateKey);
            logger.info("[enableHash] disable the old hash {} --> result: {}", oldHash, disableHash);
        } else {
            logger.info("[enableHash] no old hash to disable");
        }
    }
    
    public String removeHash(CnsType cnsType, String hash) {
        logger.info("[removeHash] begin remove the hash: {}", hash);
        WeIdPrivateKey privateKey = getWeIdPrivateKey(hash);
        ResponseData<Boolean> remove = buildToolService.getDataBucket(cnsType)
            .removeDataBucketItem(hash, false, privateKey);
        if (remove.getErrorCode().intValue() != ErrorCode.SUCCESS.getCode()) {
            logger.error("[removeHash] remove the hash {} --> result: {}", hash, remove);
            return remove.getErrorCode().intValue() + "-" + remove.getErrorMessage();
        }
        logger.info("[removeHash] remove the hash successfully, hash: {}", hash);
        return BuildToolsConstant.SUCCESS;
    }
    
    public String createAdmin(String inputPrivateKey) {
        logger.info("[createAdmin] begin create admin.");
        Credentials credentials = null;
        if (StringUtils.isNotBlank(inputPrivateKey)) {
            logger.info("[createAdmin] create by private key.");
            credentials = GenCredential.create(new BigInteger(inputPrivateKey).toString(16));
        } else {
            logger.info("[createAdmin] create by default.");
            credentials = GenCredential.create();
        }
        String privateKey = credentials.getEcKeyPair().getPrivateKey().toString();
        String publicKey = credentials.getEcKeyPair().getPublicKey().toString();
        File ecdsaFile = new File(ADMIN_PATH, ECDSA_KEY);
        FileUtils.writeToFile(privateKey, ecdsaFile.getAbsolutePath());
        File ecdsaPubFile = new File(ADMIN_PATH, ECDSA_PUB_KEY);
        FileUtils.writeToFile(publicKey, ecdsaPubFile.getAbsolutePath());
        logger.info("[createAdmin] the admin create successfully.");
        return "0x" + Keys.getAddress(new BigInteger(publicKey));
    }
    
    public String checkAdmin() {
        File ecdsaFile = new File(ADMIN_PATH, ECDSA_KEY);
        File ecdsaPubFile = new File(ADMIN_PATH, ECDSA_PUB_KEY);
        if (!ecdsaFile.exists() || !ecdsaPubFile.exists()) {
            return StringUtils.EMPTY;
        }
        String publicKey = FileUtils.readFile(ecdsaPubFile.getAbsolutePath());
        return "0x" + Keys.getAddress(new BigInteger(publicKey));
    }

    /**
     * 获取群组列表
     * @param filterMaster 是否过滤主群组
     * @return 返回群组列表
     */
    public List<String> getAllGroup(boolean filterMaster) {
        List<String> list = WeServerUtils.getGroupList();
        if (filterMaster) {
            return list.stream()
                    .filter(s -> !s.equals(BaseService.masterGroupId.toString()))
                    .collect(Collectors.toList());
        }
       return list;
    }
    
    /**
     * 获取所有的hash列表
     * @return 返回shareInfo列表数据
     */
    public List<ShareInfo> getShareList() {
        List<ShareInfo> result = new ArrayList<ShareInfo>();
        // 如果没有部署databuket则直接返回
        org.fisco.bcos.web3j.precompile.cns.CnsInfo cnsInfo = BaseService.getBucketByCns(CnsType.SHARE);
        if (cnsInfo == null) {
            logger.warn("[getShareList] the cnsType does not regist, please deploy the evidence.");
            return result;
        }
        String orgId = ConfigUtils.getCurrentOrgId();
        DataBucketServiceEngine dataBucket = buildToolService.getDataBucket(CnsType.SHARE);
        List<HashContract> list = dataBucket.getAllHash().getResult();
        // 判断机构配置私钥是否匹配所有者，如果不匹配页面可以不用显示按钮
        boolean isMatch =  buildToolService.isMatchThePrivateKey();
        Map<String, String> cache = new HashMap<String, String>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> allGroup = getAllGroup(true);
            for (HashContract hashContract : list) {
                ShareInfo share = new ShareInfo();
                share.setTime(hashContract.getTime());
                share.setOwner(hashContract.getOwner());
                share.setHash(hashContract.getHash());
                share.setShowBtn(isMatch);
                // 获取hash的群组
                String groupId = dataBucket.get(hashContract.getHash(), WeIdConstant.CNS_GROUP_ID).getResult();
                if(StringUtils.isNotBlank(groupId) && allGroup.contains(groupId)) {
                    share.setGroupId(Integer.parseInt(groupId));
                    //判断是否启用此hash
                    String enableHash = buildToolService.getDataBucket(CnsType.ORG_CONFING).get(orgId, WeIdConstant.CNS_EVIDENCE_HASH + groupId).getResult();
                    share.setEnable(hashContract.getHash().equals(enableHash));
                    //查询此部署账户的权威机构名
                    if(cache.containsKey(share.getOwner())) {
                        share.setIssuer(cache.get(share.getOwner()));
                    } else {
                        String issuer = buildToolService.getIssuerByWeId(share.getOwner());
                        share.setIssuer(issuer);
                        cache.put(share.getOwner(), issuer);
                    }
                    result.add(share);
                }
            }
        }
        return result;
    }
    
    /**
     * 根据群组部署Evidence合约.
     * @param fiscoConfig 当前配置信息
     * @param groupId 群组编号
     * @param from 部署来源
     * @return 返回是否部署成功
     */
    public String deployEvidence(FiscoConfig fiscoConfig, Integer groupId, DataFrom from) {
        logger.info("[deployEvidence] begin deploy the evidence, groupId = {}.", groupId);
        try {
            //  获取私钥
            WeIdPrivateKey currentPrivateKey = getCurrentPrivateKey();
            String hash = DeployEvidence.deployContract(
                currentPrivateKey.getPrivateKey(), 
                groupId, 
                false
            );
            if (StringUtils.isBlank(hash)) {
                logger.error("[deployEvidence] deploy the evidence fail, please check the log.");
                return StringUtils.EMPTY;
            }
            // 写部署文件
            ShareInfo share = buildShareInfo(fiscoConfig, hash, groupId, currentPrivateKey, from);
            saveShareInfo(share);
            logger.info("[deployEvidence] the evidence deploy successfully.");
            return hash;
        } catch (Exception e) {
            logger.error("[deployEvidence] deploy the evidence has error.", e);
            return StringUtils.EMPTY;
        }
    }
    
    private  void saveShareInfo(ShareInfo info) {
        File deployDir = new File(SHARE_PATH);
        File deployFile = new File(deployDir.getAbsoluteFile(), info.getHash());
        String jsonData = DataToolUtils.serialize(info);
        FileUtils.writeToFile(jsonData, deployFile.getAbsolutePath(), FileOperator.OVERWRITE);
    }
    
    private ShareInfo buildShareInfo(
        FiscoConfig fiscoConfig, 
        String hash,
        Integer groupId,
        WeIdPrivateKey currentPrivateKey,
        DataFrom from
    ) {
        ShareInfo info = new ShareInfo();
        info.setHash(hash);
        info.setTime(System.currentTimeMillis());
        info.setEcdsaKey(currentPrivateKey.getPrivateKey());
        info.setEcdsaPublicKey(
            DataToolUtils.publicKeyFromPrivate(new BigInteger(info.getEcdsaKey())).toString());
        try {
            info.setNodeVerion(BaseService.getVersion());
        } catch (Exception e) {
            info.setNodeVerion(fiscoConfig.getVersion()); 
        }
        info.setNodeAddress(fiscoConfig.getNodes());
        String evidenceAddress = buildToolService.getDataBucket(CnsType.SHARE).get(
            hash, WeIdConstant.CNS_EVIDENCE_ADDRESS).getResult();
        info.setEvidenceAddress(evidenceAddress);
        info.setContractVersion(getVersionByClass(WeIdContract.class));
        info.setWeIdSdkVersion(getVersionByClass(WeIdServiceImpl.class));
        info.setGroupId(groupId);
        info.setFrom(from.name());
        return info;
    }
    
    public ShareInfo getShareInfo(String hash) {
        ShareInfo shareInfo = getShareInfoByHash(hash);
        if (shareInfo != null) {
            shareInfo.setLocal(true);
        } else {
            shareInfo = new ShareInfo();
            shareInfo.setHash(hash);
            String groupId = buildToolService.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_GROUP_ID).getResult();
            if(StringUtils.isNotBlank(groupId)) {
                shareInfo.setGroupId(Integer.parseInt(groupId));
            }
            String evidenceAddress = buildToolService.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_EVIDENCE_ADDRESS).getResult();
            if(StringUtils.isNotBlank(evidenceAddress)) {
                shareInfo.setEvidenceAddress(evidenceAddress);
            }
        }
        return shareInfo;
    }
    
    private static File getShareFileByHash(String hash) {
        hash = FileUtils.getSecurityFileName(hash);
        return new File(SHARE_PATH, hash);
    }
    
    public static ShareInfo getShareInfoByHash(String hash) {
        File shareFile = getShareFileByHash(hash);
        if (shareFile.exists()) {
            String jsonData = FileUtils.readFile(shareFile.getAbsolutePath());
            return DataToolUtils.deserialize(jsonData, ShareInfo.class);
        } else {
            return null;
        }
    }
    
    public String enableShareCns(String hash) {
        logger.info("[enableShareCns] begin enable new hash...");
        try {
            List<String> allGroup = getAllGroup(true);
            // 查询hash对应的群组
            String groupId = buildToolService.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_GROUP_ID).getResult();
            String evidenceAddress = buildToolService.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_EVIDENCE_ADDRESS).getResult();
            if (!allGroup.contains(groupId)) {
                logger.error("[enableShareCns] the groupId of hash is not in your groupList. groupId = {}" , groupId);
                return "fail";
            }
            // 获取原hash
            String shareHashOld = buildToolService.getEvidenceHash(groupId);
            // 更新配置到链上机构配置中 evidenceAddress.<groupId> 
            String orgId = ConfigUtils.getCurrentOrgId();
            WeIdPrivateKey privateKey = getWeIdPrivateKey(hash);
            ResponseData<Boolean> result = buildToolService.getDataBucket(CnsType.ORG_CONFING).put(orgId, WeIdConstant.CNS_EVIDENCE_HASH + groupId, hash, privateKey);
            if (result.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                return result.getErrorCode() + "-" + result.getErrorMessage();
            }
            result = buildToolService.getDataBucket(CnsType.ORG_CONFING).put(orgId, WeIdConstant.CNS_EVIDENCE_ADDRESS + groupId, evidenceAddress, privateKey);
            if (result.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                return result.getErrorCode() + "-" + result.getErrorMessage();
            }
            // 启用新hash并停用原hash
            this.enableHash(CnsType.SHARE, hash, shareHashOld);
            logger.info("[enableShareCns] enable the hash {} successFully.", hash);
            return "success";
        } catch (Exception e) {
            logger.error("[enableShareCns] enable the hash error.", e);
            return "fail";
        }
    }
    
    
    public boolean setRoleType(String roleType) {
        File roleFile = new File(OTHER_PATH, ROLE_FILE);
        FileUtils.writeToFile(roleType, roleFile.getAbsolutePath(), FileOperator.OVERWRITE);
        return true;
    }
    
    public String getRoleType() {
        File roleFile = new File(OTHER_PATH, ROLE_FILE);
        if (!roleFile.exists()) {
            return StringUtils.EMPTY;
        }
        return FileUtils.readFile(roleFile.getAbsolutePath());
    }
    
    public boolean setGuideStatus(String step) {
        File guideFile = new File(OTHER_PATH, GUIDE_FILE);
        FileUtils.writeToFile(step, guideFile.getAbsolutePath(), FileOperator.OVERWRITE);
        return true;
    }
    
    public String getGuideStatus() {
        File guideFile = new File(OTHER_PATH, GUIDE_FILE);
        if (!guideFile.exists()) {
            return StringUtils.EMPTY;
        }
        return FileUtils.readFile(guideFile.getAbsolutePath());
    }
}
