package com.webank.weid.service;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.contract.deploy.v1.DeployContractV1;
import com.webank.weid.contract.deploy.v2.DeployContractV2;
import com.webank.weid.contract.v2.WeIdContract;
import com.webank.weid.dto.CnsInfo;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.HashContract;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.request.CptStringArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.impl.CptServiceImpl;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.impl.engine.DataBucketServiceEngine;
import com.webank.weid.service.impl.engine.EngineFactory;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.WeIdUtils;

@Service
public class DeployService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeployService.class);
    private static DataBucketServiceEngine dataBucket;
    private static final String HASH = "hash";
    private static final String ECDSA_KEY = "ecdsa_key";
    private static final String ECDSA_PUB_KEY = "ecdsa_key.pub";
    private static final String ADMIN_PATH = "output/admin";
    public static final String DEPLOY_PATH = "output/deploy";
    
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
    
    public DataBucketServiceEngine getDataBucket() {
        if (dataBucket == null) {
            dataBucket =  EngineFactory.createDataBucketServiceEngine();
        }
        return dataBucket;
    }
    
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
            DeployContractV2.deployContract(privateKey);
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
        String currentHash = ConfigUtils.getCurrentHash();
        LinkedList<CnsInfo> dataList = new LinkedList<CnsInfo>();
        //如果没有部署databuket则直接返回
        String bucketAddress = BaseService.getBucketAddress();
        if (StringUtils.isBlank(bucketAddress)) {
            return dataList;
        }
        
        List<HashContract> result = getDataBucket().getAllHash().getResult();
        for (HashContract hashContract : result) {
            CnsInfo cns = new CnsInfo();
            cns.setHash(hashContract.getHash());
            cns.setTime(hashContract.getTime());
            cns.setWeId(WeIdUtils.convertAddressToWeId(hashContract.getOwner()));
            cns.setEnable(hashContract.getHash().equals(currentHash));
            DeployInfo deployInfo = getDepolyInfoByHash(cns.getHash());
            if (deployInfo != null && !deployInfo.isDeploySystemCpt()) {
                cns.setNeedDeployCpt(true);
            }
            if (cns.isEnable() && cns.isNeedDeployCpt()) { //如果是启用状态
                cns.setShowDeployCptBtn(true);
            }
            dataList.add(cns);
        }
        return dataList;
    }
    
    /**
     * 根据hash从链上获取地址信息.
     * @param hash
     * @return
     */
    public DeployInfo getDeployInfoByHashFromChain(String hash) {
        //判断本地是否有次hash记录
        DeployInfo deploy = getDepolyInfoByHash(hash);
        if (deploy != null) {
            deploy.setLocal(true);//本地有
        } else {
            deploy = new DeployInfo();
            String weIdAddr = getAddress(hash, WeIdConstant.CNS_WEID_ADDRESS);
            String authAddr = getAddress(hash, WeIdConstant.CNS_AUTH_ADDRESS);
            String cptAddr = getAddress(hash, WeIdConstant.CNS_CPT_ADDRESS);
            String specificAddr = getAddress(hash, WeIdConstant.CNS_SPECIFIC_ADDRESS);
            String evidenceAddr = getAddress(hash, WeIdConstant.CNS_EVIDENCE_ADDRESS); 
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
    
    private String getAddress(String hash, String key) {
        return getDataBucket().get(hash, key).getResult();
    }
    
    private static File getDeployFileByHash(String hash) {
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
        createWeId(deployInfo, from);
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

        List<Integer> cptIdList = Arrays.asList(101, 102, 103, 106, 107);
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
    
    private void createWeId(DeployInfo deployInfo, DataFrom from) {
        logger.info("[createWeId] begin createWeid for admin");
        CreateWeIdArgs arg = new CreateWeIdArgs();
        arg.setPublicKey(deployInfo.getEcdsaPublicKey());
        WeIdPrivateKey pkey = new WeIdPrivateKey();
        pkey.setPrivateKey(deployInfo.getEcdsaKey());
        arg.setWeIdPrivateKey(pkey);
        String result = buildToolService.createWeId(arg, from);
        logger.info("[createWeId]  createWeId for admin result = {}", result);
        System.out.println("createWeId for admin result = " + result);
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
        String currentHash = ConfigUtils.getCurrentHash();
        if (StringUtils.isNotBlank(currentHash)) {
            deployInfo = getDepolyInfoByHash(ConfigUtils.getCurrentHash());
            if (deployInfo != null) {
                weIdPrivateKey.setPrivateKey(deployInfo.getEcdsaKey());
                return weIdPrivateKey;
            } 
        }
        return getCurrentPrivateKey();
    }
    
    public void enableHash(String hash, FiscoConfig config) {
        logger.info("[enableHash] begin enable the hash: {}", hash);
        //启用新hash
        WeIdPrivateKey privateKey = getWeIdPrivateKey(hash);
        ResponseData<Boolean> enableHash = getDataBucket().enableHash(hash, privateKey);
        logger.info("[enableHash] enable the hash {} --> result: {}", hash, enableHash);
        //如果原hash不为空，则停用原hash
        if (StringUtils.isNotBlank(config.getCnsContractFollow())) {
            String oldHash = config.getCnsContractFollow();
            ResponseData<Boolean> disableHash = getDataBucket().disableHash(oldHash, privateKey);
            logger.info("[enableHash] disable the old hash {} --> result: {}", oldHash, disableHash);
        } else {
            logger.info("[enableHash] no old hash to disable");
        }
    }
    
    public String removeHash(String hash) {
        logger.info("[removeHash] begin remove the hash: {}", hash);
        WeIdPrivateKey privateKey = getWeIdPrivateKey(hash);
        ResponseData<Boolean> remove = getDataBucket().remove(hash, "", privateKey);
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
}
