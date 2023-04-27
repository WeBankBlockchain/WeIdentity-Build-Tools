

package com.webank.weid.service;

import com.webank.weid.blockchain.config.FiscoConfig;
import com.webank.weid.blockchain.constant.CnsType;
import com.webank.weid.blockchain.deploy.v2.DeployContractV2;
import com.webank.weid.blockchain.deploy.v2.DeployEvidenceV2;
import com.webank.weid.blockchain.deploy.v3.DeployContractV3;
import com.webank.weid.blockchain.deploy.v3.DeployEvidenceV3;
import com.webank.weid.blockchain.protocol.base.HashContract;
import com.webank.weid.blockchain.service.fisco.BaseServiceFisco;
import com.webank.weid.blockchain.service.fisco.engine.DataBucketServiceEngine;
import com.webank.weid.constant.*;
import com.webank.weid.contract.v2.WeIdContract;
import com.webank.weid.dto.CnsInfo;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.ShareInfo;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.blockchain.protocol.response.ResponseData;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

import static com.webank.weid.constant.ChainVersion.FISCO_V2;

@Service
@Slf4j
public class ContractService {

    private static String preMainHash;

    private ConfigService configService = new ConfigService();

    private WeIdSdkService weIdSdkService = new WeIdSdkService();
    
    private WeBaseService weBaseService = new WeBaseService();
    
    private static final String CONTRACT_JAR_PEFIX = "weid-contract-java-";

    static {
        FileUtils.mkdirs(BuildToolsConstant.ADMIN_PATH);
        FileUtils.mkdirs(BuildToolsConstant.DEPLOY_PATH);
    }

    public ResponseData<String> deploy(String chainId, String applyName) {
        try {
            FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
            fiscoConfig.setChainId(chainId);
            String hash = deployContract(fiscoConfig, DataFrom.WEB);
            configService.updateChainId(chainId);
            log.info("[deploy] the hash: {}", hash);
            //将应用名写入配置中
            applyName = StringEscapeUtils.unescapeHtml(applyName);
            WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getWeIdPrivateKey(hash);
            com.webank.weid.blockchain.protocol.response.ResponseData<Boolean> response = WeIdSdkUtils.getDataBucket(CnsType.DEFAULT)
                    .put(hash, BuildToolsConstant.APPLY_NAME, applyName, currentPrivateKey.getPrivateKey());
            log.info("[deploy] put applyName: {}", response);
            return new ResponseData<>(hash, ErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("[deploy] the contract depoly error.", e);
            return new ResponseData<>(StringUtils.EMPTY, ErrorCode.UNKNOW_ERROR);
        } finally {
            FileUtils.clearDeployFile();
        }
    }

    // 执行部署weId合约
    public String deployContract(FiscoConfig fiscoConfig, DataFrom from) {
        log.info("begin deploy contract...");
        File targetDir = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ADMIN_KEY);
        String privateKey = null;
        if (targetDir.exists()) {
            privateKey = FileUtils.readFile(targetDir.getAbsolutePath());
        }
        if (FISCO_V2.getVersion() == Integer.parseInt(fiscoConfig.getVersion())) {
            DeployContractV2.deployContract(privateKey, fiscoConfig, false);
        } else {
            DeployContractV3.deployContract(privateKey, fiscoConfig, false);
        }
        log.info("the contract deploy finish.");
        //开始保存文件
        //将私钥移动到/output/admin中
        copyKeyPair();
        return saveDeployInfo(fiscoConfig, from);
    }

    private void copyKeyPair() {
        log.info("[copyKeypair] begin copy the ecdsa to admin...");
        File keypairFile = new File(BuildToolsConstant.ADMIN_KEY);
        File targetDir = new File(BuildToolsConstant.ADMIN_PATH);
        FileUtils.copy(keypairFile, new File(targetDir.getAbsoluteFile(), BuildToolsConstant.ADMIN_KEY));
        
        File ecdsaPubFile = new File(BuildToolsConstant.ADMIN_PUB_KEY);
        FileUtils.copy(ecdsaPubFile, new File(targetDir.getAbsoluteFile(), BuildToolsConstant.ADMIN_PUB_KEY));
        log.info("[copyKeypair] the keypair copy successfully.");
    }

    private String saveDeployInfo(FiscoConfig fiscoConfig, DataFrom from) {
        log.info("[saveDeployInfo] begin to save deploy info...");
        //创建部署目录
        String hash = FileUtils.readFile(BuildToolsConstant.HASH);
        DeployInfo deployInfo = buildInfo(fiscoConfig, hash, from);
        saveDeployInfo(deployInfo);
        log.info("[saveDeployInfo] save the deploy info successfully.");
        saveContractToWeBase(deployInfo);
        return hash;
    }

    private void saveContractToWeBase(DeployInfo deployInfo) {
        String groupId = configService.getMasterGroupId();
        String version = getContractVersion();
        String hash = deployInfo.getHash();
        weBaseService.contractSave(groupId, "WeIdContract", deployInfo.getWeIdAddress(),version, hash);
        weBaseService.contractSave(groupId, "CptController", deployInfo.getCptAddress(), version, hash);
        weBaseService.contractSave(groupId, "AuthorityIssuerController", deployInfo.getAuthorityAddress(), version, hash);
        weBaseService.contractSave(groupId, "SpecificIssuerController", deployInfo.getEvidenceAddress(), version, hash);
        weBaseService.contractSave(groupId, "EvidenceContract", deployInfo.getSpecificAddress(), version, hash);
    }

    public String getContractVersion() {
        return ClassUtils.getVersionByClass(WeIdContract.class, CONTRACT_JAR_PEFIX);
    }

    private  void saveDeployInfo(DeployInfo info) {
        File deployDir = new File(BuildToolsConstant.DEPLOY_PATH);
        File deployFile = new File(deployDir.getAbsoluteFile(), info.getHash());
        String jsonData = DataToolUtils.serialize(info);
        FileUtils.writeToFile(jsonData, deployFile.getAbsolutePath(), FileOperator.OVERWRITE);
    }

    private DeployInfo buildInfo(FiscoConfig fiscoConfig, String hash, DataFrom from) {
        DeployInfo info = new DeployInfo();
        info.setHash(hash);
        long time = System.currentTimeMillis();
        info.setTime(time);
        info.setEcdsaKey(FileUtils.readFile(BuildToolsConstant.ADMIN_KEY));
        BigInteger privateKey = new BigInteger(info.getEcdsaKey());
        info.setEcdsaPublicKey(DataToolUtils.publicKeyFromPrivate(privateKey).toString());
        try {
            info.setNodeVerion(BaseServiceFisco.getVersion());
        } catch (Exception e) {
            info.setNodeVerion(fiscoConfig.getVersion()); 
        }
        info.setNodeAddress(fiscoConfig.getNodes());
        info.setAuthorityAddress(FileUtils.readFile(BuildToolsConstant.AUTH_ADDRESS_FILE_NAME));
        info.setCptAddress(FileUtils.readFile(BuildToolsConstant.CPT_ADDRESS_FILE_NAME));
        info.setWeIdAddress(FileUtils.readFile(BuildToolsConstant.WEID_ADDRESS_FILE_NAME));
        info.setEvidenceAddress(FileUtils.readFile(BuildToolsConstant.EVID_ADDRESS_FILE_NAME));
        info.setSpecificAddress(FileUtils.readFile(BuildToolsConstant.SPECIFIC_ADDRESS_FILE_NAME));
        info.setChainId(fiscoConfig.getChainId());
        info.setContractVersion(ClassUtils.getJarNameByClass(WeIdContract.class));
        info.setWeIdSdkVersion(ClassUtils.getJarNameByClass(WeIdServiceImpl.class));
        info.setFrom(from.name());
        return info;
    }

    public ResponseData<LinkedList<CnsInfo>> getDeployList() {
        LinkedList<CnsInfo> dataList = new LinkedList<>();
        //如果没有部署databuket则直接返回
        com.webank.weid.blockchain.protocol.response.CnsInfo cnsInfo = BaseServiceFisco.getBucketByCns(CnsType.DEFAULT);
        if (cnsInfo == null) {
            return new ResponseData<>(dataList, ErrorCode.BASE_ERROR);
        }
        //如果没有部署databuket则直接返回
        cnsInfo = BaseServiceFisco.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return new ResponseData<>(dataList, ErrorCode.BASE_ERROR);
        }
        String currentHash = WeIdSdkUtils.getMainHash();
        List<HashContract> result = WeIdSdkUtils.getDataBucket(CnsType.DEFAULT).getAllBucket().getResult();
        String roleType = configService.getRoleType().getResult();
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
            
            DeployInfo deployInfo = WeIdSdkUtils.getDepolyInfoByHash(cns.getHash());
            if (deployInfo != null && !deployInfo.isDeploySystemCpt()) {
                cns.setNeedDeployCpt(true);
            }
            if (cns.isEnable() && cns.isNeedDeployCpt()) { //如果是启用状态
                cns.setShowDeployCptBtn(true);
            }
            // 查询此部署账户的权威机构名
            if (cns.isEnable()) {
                AuthorityIssuer issuer = weIdSdkService.getIssuerByWeId(cns.getWeId());
                cns.setIssuer(issuer);
            }
            String applyName = WeIdSdkUtils.getDataBucket(CnsType.DEFAULT).get(cns.getHash(), BuildToolsConstant.APPLY_NAME).getResult();
            cns.setApplyName(applyName);
            dataList.add(cns);
        }
        Collections.sort(dataList);
        dataList.forEach(cns -> {
            cns.setGroupId("group-" + WeIdSdkUtils.loadNewFiscoConfig().getGroupId());
            if (cns.isEnable()) { // 如果是启用状态
                //如果上一个地址不为空，并且新hash地址跟上一个地址不相同则reloadAddress
                if (StringUtils.isNotBlank(preMainHash) && !preMainHash.equals(cns.getHash())) {
                    configService.reloadAddress();
                }
                //当前账户创建weId，（内部有判断如果已创建则不会调用区块链创建）
                createWeIdForCurrentUser(DataFrom.WEB);
                preMainHash = cns.getHash();
            }
        });
        return new ResponseData<>(dataList, ErrorCode.SUCCESS);
    }

    /**
     * 根据hash从链上获取地址信息.
     * @param hash 获取部署数据的hash值
     * @return 返回当前hash的部署信息
     */
    public DeployInfo getDeployInfoByHashFromChain(String hash) {
        //判断本地是否有次hash记录
        DeployInfo deploy = WeIdSdkUtils.getDepolyInfoByHash(hash);
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
        return WeIdSdkUtils.getDataBucket(cnsType).get(hash, key).getResult();
    }

    public ResponseData<Boolean> deploySystemCpt(String hash, DataFrom from) {
        try {
            DeployInfo deployInfo = WeIdSdkUtils.getDepolyInfoByHash(hash);
            if (deployInfo == null) {
                log.error("[deploySystemCpt] can not found the admin ECDSA.");
                return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), "can not found the admin ECDSA.");
            }
            // 注册weid
            createWeId(deployInfo, from, true);

            log.info("[deploySystemCpt] begin register systemCpt...");
            //部署系统CPT, 
            if (!registerSystemCpt(deployInfo)) {
                log.error("[deploySystemCpt] systemCpt is register fail.");
                return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), "systemCpt is register fail.");
            }

            log.info("[deploySystemCpt] systemCpt is registed and to save.");
            //更新部署信息,表示已部署系统CPT
            deployInfo.setDeploySystemCpt(true);
            saveDeployInfo(deployInfo);
        } catch (Exception e) {
            log.error("[deploySystemCpt] the System Cpt depoly error.", e);
            return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR);
        }

        return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
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

        CptServiceImpl cptService = new CptServiceImpl();
        for (Integer cptId : BuildToolsConstant.CPTID_LIST) {
            String cptJsonSchema = DataToolUtils.generateDefaultCptJsonSchema(cptId);
            if (cptJsonSchema.isEmpty()) {
                log.info("[registerSystemCpt] Cannot generate CPT json schema with ID: " + cptId);
                return false;
            }
            cptStringArgs.setCptJsonSchema(cptJsonSchema);
            ResponseData<CptBaseInfo> responseData = cptService.registerCpt(cptStringArgs, cptId);
            if (responseData.getResult() == null) {
                log.info("[registerSystemCpt] Register System CPT failed with ID: " + cptId);
                return false;
            }
        }
        return true;
    }

    private void createWeId(DeployInfo deployInfo, DataFrom from, boolean isAdmin) {
        log.info("[createWeId] begin createWeid for admin");
        CreateWeIdArgs arg = new CreateWeIdArgs();
        arg.setPublicKey(deployInfo.getEcdsaPublicKey());
        WeIdPrivateKey pkey = new WeIdPrivateKey();
        pkey.setPrivateKey(deployInfo.getEcdsaKey());
        arg.setWeIdPrivateKey(pkey);
        String weId = WeIdUtils.convertPublicKeyToWeId(arg.getPublicKey());
        boolean checkWeId = weIdSdkService.checkWeId(weId);
        if (!checkWeId) {
            String result = weIdSdkService.createWeId(arg, from, isAdmin).getResult();
            log.info("[createWeId]  createWeId for admin result = {}", result);
            System.out.println("createWeId for admin result = " + result);
        } else {
            log.info("[createWeId] the weId is exist."); 
        }
        // 默认将当前weid注册成为权威机构，并认证
        String orgId = ConfigUtils.getCurrentOrgId();
        // 注册权威机构
        weIdSdkService.registerIssuer(weId, orgId, null);
        // 认证权威机构
        weIdSdkService.recognizeAuthorityIssuer(weId);
    }

    /**
     * 给当前账户创建WeId.
     * @param from 创建来源
     * @return 返回创建的weId
     */
    public String createWeIdForCurrentUser(DataFrom from) {
        //判断当前私钥账户对应的weid是否存在，如果不存在则创建weId
        CreateWeIdArgs arg = new CreateWeIdArgs();
        arg.setWeIdPrivateKey(WeIdSdkUtils.getCurrentPrivateKey());
        arg.setPublicKey(DataToolUtils.publicKeyFromPrivate(new BigInteger(arg.getWeIdPrivateKey().getPrivateKey())).toString());
        String weId = WeIdUtils.convertPublicKeyToWeId(arg.getPublicKey());
        log.info("[createWeIdForCurrentUser] the current weId is = {}", weId);
        boolean checkWeId = weIdSdkService.checkWeId(weId);
        if (!checkWeId) {
            log.info("[createWeIdForCurrentUser] the current weId is not exist and begin create.");
            String result = weIdSdkService.createWeId(arg, from, true).getResult();
            log.info("[createWeIdForCurrentUser] create weid for current account result = {}", result);
            return result;
        } else {
            log.info("[createWeIdForCurrentUser] the current weId is exist.");
            return weId;
        }
    }

    public void enableHash(CnsType cnsType, String hash, String oldHash) {
        log.info("[enableHash] begin enable the hash: {}", hash);
        //启用新hash
        WeIdPrivateKey privateKey = WeIdSdkUtils.getWeIdPrivateKey(hash);
        com.webank.weid.blockchain.protocol.response.ResponseData<Boolean> enableHash = WeIdSdkUtils.getDataBucket(cnsType).enable(hash, privateKey.getPrivateKey());
        log.info("[enableHash] enable the hash {} --> result: {}", hash, enableHash);
        //如果原hash不为空，则停用原hash
        if (StringUtils.isNotBlank(oldHash)) {
            com.webank.weid.blockchain.protocol.response.ResponseData<Boolean> disableHash = WeIdSdkUtils.getDataBucket(cnsType).disable(oldHash, privateKey.getPrivateKey());
            log.info("[enableHash] disable the old hash {} --> result: {}", oldHash, disableHash);
        } else {
            log.info("[enableHash] no old hash to disable");
        }
    }

    public ResponseData<Boolean> removeHash(CnsType cnsType, String hash) {
        log.info("[removeHash] begin remove the hash: {}", hash);
        WeIdPrivateKey privateKey = WeIdSdkUtils.getWeIdPrivateKey(hash);
        com.webank.weid.blockchain.protocol.response.ResponseData<Boolean> resp = WeIdSdkUtils.getDataBucket(cnsType).removeDataBucketItem(hash, false, privateKey.getPrivateKey());
        return new ResponseData<>(resp.getResult(), ErrorCode.getTypeByErrorCode(resp.getErrorCode()));
    }

    /**
     * 获取所有的hash列表
     * @return 返回shareInfo列表数据
     */
    public ResponseData<List<ShareInfo>> getShareList() {
        List<ShareInfo> result = new ArrayList<>();
        // 如果没有部署databuket则直接返回
        com.webank.weid.blockchain.protocol.response.CnsInfo cnsInfo = BaseServiceFisco.getBucketByCns(CnsType.SHARE);
        if (cnsInfo == null) {
            log.warn("[getShareList] the cnsType does not regist, please deploy the evidence.");
            return new ResponseData<>(result, ErrorCode.BASE_ERROR);
        }
        String orgId = ConfigUtils.getCurrentOrgId();
        DataBucketServiceEngine dataBucket = WeIdSdkUtils.getDataBucket(CnsType.SHARE);
        List<HashContract> list = dataBucket.getAllBucket().getResult();
        // 判断机构配置私钥是否匹配所有者，如果不匹配页面可以不用显示按钮
        boolean isMatch = isMatchThePrivateKey();
        Map<String, AuthorityIssuer> cache = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> allGroup = weIdSdkService.getAllGroup(true);
            for (HashContract hashContract : list) {
                ShareInfo share = new ShareInfo();
                share.setTime(hashContract.getTime());
                share.setOwner(hashContract.getOwner());
                share.setHash(hashContract.getHash());
                share.setShowBtn(isMatch);
                // 获取hash的群组
                String groupId = dataBucket.get(hashContract.getHash(), WeIdConstant.CNS_GROUP_ID).getResult();
                if(StringUtils.isNotBlank(groupId) && allGroup.contains(groupId)) {
                    share.setGroupId(groupId);
                    //判断是否启用此hash
                    String enableHash = WeIdSdkUtils.getDataBucket(CnsType.ORG_CONFING).get(orgId, WeIdConstant.CNS_EVIDENCE_HASH + groupId).getResult();
                    share.setEnable(hashContract.getHash().equals(enableHash));
                    //查询此部署账户的权威机构名
                    if(cache.containsKey(share.getOwner())) {
                        share.setIssuer(cache.get(share.getOwner()));
                    } else {
                        AuthorityIssuer issuer = weIdSdkService.getIssuerByWeId(share.getOwner());
                        share.setIssuer(issuer);
                        cache.put(share.getOwner(), issuer);
                    }
                    String evidenceName = dataBucket.get(share.getHash(), BuildToolsConstant.EVIDENCE_NAME).getResult();
                    share.setEvidenceName(evidenceName);
                    result.add(share);
                }
            }
        }
        Collections.sort(result);
        return new ResponseData<>(result, ErrorCode.SUCCESS);
    }

    /**
     * 根据群组部署Evidence合约.
     * @param fiscoConfig 当前配置信息
     * @param groupId 群组编号
     * @param from 部署来源
     * @return 返回是否部署成功
     */
    public String deployEvidence(FiscoConfig fiscoConfig, String groupId, DataFrom from) {
        log.info("[deployEvidence] begin deploy the evidence, groupId = {}.", groupId);
        try {
            //  获取私钥
            WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getCurrentPrivateKey();
            String hash = null;
            if (FISCO_V2.getVersion() == Integer.parseInt(fiscoConfig.getVersion())) {
                hash = DeployEvidenceV2.deployContract(
                        fiscoConfig,
                        currentPrivateKey.getPrivateKey(),
                        groupId,
                        false
                );
            } else {
                hash = DeployEvidenceV3.deployContract(
                        fiscoConfig,
                        currentPrivateKey.getPrivateKey(),
                        groupId,
                        false
                );
            }
            if (StringUtils.isBlank(hash)) {
                log.error("[deployEvidence] deploy the evidence fail, please check the log.");
                return StringUtils.EMPTY;
            }
            // 写部署文件
            ShareInfo share = buildShareInfo(fiscoConfig, hash, groupId, currentPrivateKey, from);
            saveShareInfo(share);
            // 导入合约到WeBase中
            String version =  this.getContractVersion();
            weBaseService.contractSave(groupId, "EvidenceContract", share.getEvidenceAddress(), version, hash);
            log.info("[deployEvidence] the evidence deploy successfully.");
            return hash;
        } catch (Exception e) {
            log.error("[deployEvidence] deploy the evidence has error.", e);
            return StringUtils.EMPTY;
        }
    }

    private  void saveShareInfo(ShareInfo info) {
        File deployDir = new File(BuildToolsConstant.SHARE_PATH);
        File deployFile = new File(deployDir.getAbsoluteFile(), info.getHash());
        String jsonData = DataToolUtils.serialize(info);
        FileUtils.writeToFile(jsonData, deployFile.getAbsolutePath(), FileOperator.OVERWRITE);
    }

    private ShareInfo buildShareInfo(
        FiscoConfig fiscoConfig, 
        String hash,
        String groupId,
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
            info.setNodeVerion(BaseServiceFisco.getVersion());
        } catch (Exception e) {
            info.setNodeVerion(fiscoConfig.getVersion()); 
        }
        info.setNodeAddress(fiscoConfig.getNodes());
        String evidenceAddress = WeIdSdkUtils.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_EVIDENCE_ADDRESS).getResult();
        info.setEvidenceAddress(evidenceAddress);
        info.setContractVersion(ClassUtils.getJarNameByClass(WeIdContract.class));
        info.setWeIdSdkVersion(ClassUtils.getJarNameByClass(WeIdServiceImpl.class));
        info.setGroupId(groupId);
        info.setFrom(from.name());
        return info;
    }

    public ResponseData<ShareInfo> getShareInfo(String hash) {
        ShareInfo shareInfo = getShareInfoByHash(hash);
        if (shareInfo != null) {
            shareInfo.setLocal(true);
            return new ResponseData<>(shareInfo, ErrorCode.SUCCESS);
        }

        shareInfo = new ShareInfo();
        shareInfo.setHash(hash);
        String groupId = WeIdSdkUtils.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_GROUP_ID).getResult();
        if(StringUtils.isNotBlank(groupId)) {
            shareInfo.setGroupId(groupId);
        }
        String evidenceAddress = WeIdSdkUtils.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_EVIDENCE_ADDRESS).getResult();
        if(StringUtils.isNotBlank(evidenceAddress)) {
            shareInfo.setEvidenceAddress(evidenceAddress);
        }
        return new ResponseData<>(shareInfo, ErrorCode.SUCCESS);
    }

    private static File getShareFileByHash(String hash) {
        hash = FileUtils.getSecurityFileName(hash);
        return new File(BuildToolsConstant.SHARE_PATH, hash);
    }

    private static ShareInfo getShareInfoByHash(String hash) {
        File shareFile = getShareFileByHash(hash);
        if (shareFile.exists()) {
            String jsonData = FileUtils.readFile(shareFile.getAbsolutePath());
            return DataToolUtils.deserialize(jsonData, ShareInfo.class);
        } else {
            return null;
        }
    }

    public ResponseData<Boolean> enableShareCns(String hash) {
        log.info("[enableShareCns] begin enable new hash...");
        try {
            List<String> allGroup = weIdSdkService.getAllGroup(true);
            // 查询hash对应的群组
            String groupId = WeIdSdkUtils.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_GROUP_ID).getResult();
            String evidenceAddress = WeIdSdkUtils.getDataBucket(CnsType.SHARE).get(hash, WeIdConstant.CNS_EVIDENCE_ADDRESS).getResult();
            if (!allGroup.contains(groupId)) {
                log.error("[enableShareCns] the groupId of hash is not in your groupList. groupId = {}" , groupId);
                return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(),
                        "the groupId of hash is not in your groupList");
            }
            // 获取原hash
            String shareHashOld = getEvidenceHash(groupId);
            // 更新配置到链上机构配置中 evidenceAddress.<groupId> 
            String orgId = ConfigUtils.getCurrentOrgId();
            WeIdPrivateKey privateKey = WeIdSdkUtils.getWeIdPrivateKey(hash);
            com.webank.weid.blockchain.protocol.response.ResponseData<Boolean> result = WeIdSdkUtils.getDataBucket(CnsType.ORG_CONFING).
                    put(orgId, WeIdConstant.CNS_EVIDENCE_HASH + groupId, hash, privateKey.getPrivateKey());
            if (result.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                return new ResponseData<>(Boolean.FALSE, result.getErrorCode(), result.getErrorMessage());
            }
            result = WeIdSdkUtils.getDataBucket(CnsType.ORG_CONFING).put(orgId, WeIdConstant.CNS_EVIDENCE_ADDRESS + groupId, evidenceAddress, privateKey.getPrivateKey());
            if (result.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                return new ResponseData<>(Boolean.FALSE, result.getErrorCode(), result.getErrorMessage());
            }
            // 启用新hash并停用原hash
            this.enableHash(CnsType.SHARE, hash, shareHashOld);
            log.info("[enableShareCns] enable the hash {} successFully.", hash);
        } catch (Exception e) {
            log.error("[enableShareCns] enable the hash error.", e);
            return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), "enable the hash error");
        }

        return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
    }

    // 判断当前机构配置跟当前私钥是否匹配
    private boolean isMatchThePrivateKey() {
        HashContract hashFromOrgIdCns = getHashFromOrgCns(ConfigUtils.getCurrentOrgId());
        // 如果不存在机构配置，则可以匹配
        if (hashFromOrgIdCns == null) {
            log.info("[isMatchThePrivateKey] the orgId does not exist in orgConfig cns, default match.");
            return true;//不存在
        }
        WeIdPrivateKey currentPrivateKey = WeIdSdkUtils.getCurrentPrivateKey();
        String publicKey = DataToolUtils.publicKeyFromPrivate(
                new BigInteger(currentPrivateKey.getPrivateKey())).toString();
        String address = DataToolUtils.addressFromPublic(new BigInteger(publicKey));
        if (address.equals(hashFromOrgIdCns.getOwner())) {
            log.info("[isMatchThePrivateKey] the orgId is exist in orgConfig cns, match the private key.");
            return true;//存在orgId并且为当前机构所有
        }
        log.info("[isMatchThePrivateKey] the orgId is exist, but misMatch the private key.");
        return false; //存在机构id不为当前机构所有，私钥不匹配
    }

    /**
     * 根据orgId获取org_config里面的hash数据.
     * @param orgId 机构编码
     * @return 返回hash对象信息
     */
    private HashContract getHashFromOrgCns(String orgId) {
        com.webank.weid.blockchain.protocol.response.CnsInfo cnsInfo = BaseServiceFisco.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return null;
        }
        List<HashContract> allHash = WeIdSdkUtils.getDataBucket(CnsType.ORG_CONFING).getAllBucket().getResult();
        for (HashContract hashContract : allHash) {
            if (hashContract.getHash().equals(orgId)) {
                return hashContract;
            }
        }
        return null;
    }

    public String getEvidenceHash(String groupId) {
        com.webank.weid.blockchain.protocol.response.CnsInfo cnsInfo = BaseServiceFisco.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return StringUtils.EMPTY;
        }
        String orgId = ConfigUtils.getCurrentOrgId();
        return WeIdSdkUtils.getDataBucket(CnsType.ORG_CONFING).get(
                orgId, WeIdConstant.CNS_EVIDENCE_HASH + groupId).getResult();
    }

    public ResponseData<List<AuthorityIssuer>> getUserListByHash(String hash) {
        List<AuthorityIssuer> rList = new ArrayList<>();
        com.webank.weid.blockchain.protocol.response.ResponseData<List<String>> responseData = WeIdSdkUtils.getDataBucket(CnsType.SHARE).getActivatedUserList(hash);
        if (responseData.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
            return new ResponseData<>(rList, responseData.getErrorCode(), responseData.getErrorMessage());
        }
        for (String weIdAddress : responseData.getResult()) {
            AuthorityIssuer issuer = weIdSdkService.getIssuerByWeId(weIdAddress);
            if (issuer == null) {
                issuer = new AuthorityIssuer();
                issuer.setWeId(WeIdUtils.convertAddressToWeId(weIdAddress));
            }
            rList.add(issuer);
        }
        return new ResponseData<>(rList, ErrorCode.SUCCESS);
    }
}
