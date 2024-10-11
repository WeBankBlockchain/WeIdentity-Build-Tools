package com.webank.weid.service;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.dto.ContractSolInfo;
import com.webank.weid.dto.PageDto;
import com.webank.weid.dto.webase.request.RegisterInfo;
import com.webank.weid.dto.webase.response.BaseListResponse;
import com.webank.weid.dto.webase.response.BaseResponse;
import com.webank.weid.dto.webase.response.Certificate;
import com.webank.weid.dto.webase.response.NodeInfo;
import com.webank.weid.dto.webase.response.UserInfo;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.util.ConfigUtils;
import com.webank.weid.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 1. 注册服务:
 *     如果执行注册服务成功，则说明前端使用webase集成模式，本地记录webase ip + 端口等相关信息
 *     如果服务注册成功，则调用获取证书信息接口
 * 2. 获取证书，并且本地保存
 * 3. 获取节点信息  （页面初始化，主要获取节点类型（国密、非国密））
 * 4. 获取节点列表信息  （用于页面选择节点）
 * 5. 获取账户信息  （用于页面选择账户）
 * 6. 根据账户获取私钥信息   （页面选择账户后获取私钥信息到本地保存）
 * 7. 创建账户的时候导入私钥信息到webase   （如果选择本地创建账户，则将账户导入到webase）
 * 8. 合约部署导入到webase   （合约部署完导入到webase中）
 * 
 * @author v_wbgyang
 *
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class WeBaseService {

    private static final String WEBASE_CONTEXT = "/WeBASE-Node-Manager/";

    private RestTemplate restTemplate = new RestTemplate();
    
    private GuideService guideService = new GuideService();

    /**
     * 服務注冊接口，weId向WeBase注册服务。
     * 
     * @param registerInfo 注册信息
     * @return 返回注册结果
     */
    public BaseResponse<Object> registerService(RegisterInfo registerInfo) {
        String requestUrl = getRequestUrl(registerInfo, "appRegister");
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("appIp", registerInfo.getWeIdHost().split(":")[0]);
        request.put("appPort", registerInfo.getWeIdHost().split(":")[1]);
        request.put("appLink", "http://" + registerInfo.getWeIdHost());
        try {
            BaseResponse<Object> res = restTemplate.postForObject(requestUrl, request, BaseResponse.class);
            log.info("[registerService] code: {}, msg: {}", res.getCode(), res.getMessage());
            return res;
        } catch (Exception e) {
            log.error("[registerService] register service has error.", e);
            return new BaseResponse<>(ErrorCode.UNKNOW_ERROR.getCode(), ErrorCode.UNKNOW_ERROR.getCodeDesc());
        }
    }

    /**
     * 保存Webase注册信息。
     * @param registerInfo 服务注册信息
     */
     public void saveRegisterInfo(RegisterInfo registerInfo) {
        // 记录Webase数据
        File weBaseFile = getWeBaseFile();
        registerInfo.setUseWebase(true);
        String data = ConfigUtils.serialize(registerInfo);
        FileUtils.writeToFile(data, weBaseFile.getAbsolutePath(), FileOperator.OVERWRITE);
    }
 
    /**
     * 设置为非Webase集成模式。
     */
     public void setNoWeBaseMode() {
        File weBaseFile = getWeBaseFile();
        if (weBaseFile.exists()) {
            String info = FileUtils.readFile(weBaseFile.getAbsolutePath());
            RegisterInfo registerInfo = ConfigUtils.deserialize(info, RegisterInfo.class);
            registerInfo.setUseWebase(false);
            String data = ConfigUtils.serialize(registerInfo);
            FileUtils.writeToFile(data, weBaseFile.getAbsolutePath(), FileOperator.OVERWRITE);
        }
    }

    /**
     * 判断是否集成WeBase。
     * @return boolean true:集成了WeBase  false：没有集成Webase
     */
     public Boolean isIntegrateWebase() {
        File weBaseFile = getWeBaseFile();
        if (weBaseFile.exists()) {
            String info = FileUtils.readFile(weBaseFile.getAbsolutePath());
            RegisterInfo registerInfo = ConfigUtils.deserialize(info, RegisterInfo.class);
            return registerInfo.isUseWebase();
        }
        return false;
    }

    public Boolean syncCertificate() {
        String requestUrl = getApi("sdkCert");
        try {
            BaseResponse<List<Certificate>> res = restTemplate.getForObject(requestUrl, BaseResponse.class);
            log.info("[syncCertificate] code: {}, msg: {}", res.getCode(), res.getMessage());
            if (res.getCode() == 0) {
                buildForList(res, Certificate.class);
                // 保存证书
                for (Certificate certificate : res.getData()) {
                    String certificateName = certificate.getName();
                    // todo javasdk用的是sdk证书，不是node证书
                    if ("sdk.crt".equals(certificateName)) {
                        certificateName = "node.crt";
                    } else if ("sdk.key".equals(certificateName)) {
                        certificateName = "node.key";
                    }
                    File file = new File(BuildToolsConstant.RESOURCES_PATH, certificateName);
                    FileUtils.writeToFile(certificate.getContent(), file.getAbsolutePath(), FileOperator.OVERWRITE);
                }
                return true;
            }
        } catch (Exception e) {
            log.error("[queryNodeList] query nodeList form WeBase has error.", e);
        }
        return false;
    }

    public Boolean syncContractToWeBase(List<ContractSolInfo> contractList, String contractVersion) {
        if (CollectionUtils.isEmpty(contractList)) {
            log.warn("[syncContractToWeBase] the contractList is empty.");
            return false;
        }
        String requestUrl = getApi("contractSourceSave");
        try {
            Map<String, Object> request = new HashMap<String, Object>();
            request.put("contractList", contractList);
            request.put("contractVersion", contractVersion);
            request.put("account", "admin");
            BaseResponse<?> res = restTemplate.postForObject(requestUrl, request, BaseResponse.class);
            log.info("[syncContractToWeBase] code: {}, msg: {}", res.getCode(), res.getMessage());
            return res.getCode() == 0;
        } catch (Exception e) {
            log.error("[syncContractToWeBase] query nodeList form WeBase has error.", e);
        }
        return false;
    }

    /**
     * 获取节点类型：1: 国密，0：非国密。
     * @return 返回节点类型
     */
    public BaseResponse<Integer> queryNodeType() {
        String requestUrl = getApi("encrypt");
        try {
            BaseResponse<Integer> res = restTemplate.getForObject(requestUrl, new BaseResponse<Integer>().getClass());
            log.info("[queryNodeType] code: {}, msg: {}", res.getCode(), res.getMessage());
            return res;
        } catch (Exception e) {
            log.error("[queryNodeType] query nodeType form WeBase has error.", e);
            return new BaseResponse<>(ErrorCode.UNKNOW_ERROR.getCode(), ErrorCode.UNKNOW_ERROR.getCodeDesc());
        }
    }

    /**
     * 查询前置节点列表。
     * @return 返回节点列表
     */
    public BaseListResponse<List<NodeInfo>> queryNodeList() {
        String requestUrl = getApi("frontNodeList");
        try {
            BaseListResponse<List<NodeInfo>> res = restTemplate.getForObject(requestUrl, BaseListResponse.class);
            log.info("[queryNodeList] code: {}, msg: {}", res.getCode(), res.getMessage());
            if (res.getCode() == 0) {
                buildForList(res, NodeInfo.class);
            }
            return res;
        } catch (Exception e) {
            log.error("[queryNodeList] query nodeList form WeBase has error.", e);
            return new BaseListResponse<>(ErrorCode.UNKNOW_ERROR.getCode(), ErrorCode.UNKNOW_ERROR.getCodeDesc());
        }
    }

    /**
     * 分页查询账户列表。
     * @param pageDto 分页查询对象
     * @return 返回用户列表数据
     */
    public BaseListResponse<List<UserInfo>> queryUserList(PageDto<UserInfo> pageDto) {
        String requestUrl = getApi("userList");
        requestUrl += "&groupId=" + pageDto.getQuery().getGroupId() 
            + "&pageSize=" + pageDto.getPageSize() 
            + "&pageNumber=" + pageDto.getStartIndex() 
            + "&hasPrivateKey=" + pageDto.getQuery().getHasPk();
        try {
            BaseListResponse<List<UserInfo>> res = restTemplate.getForObject(requestUrl, BaseListResponse.class);
            log.info("[queryUserList] code: {}, msg: {}", res.getCode(), res.getMessage());
            if (res.getCode() == 0) {
                buildForList(res, UserInfo.class);
            } 
            return res;
        } catch (Exception e) {
            log.error("[queryUserList] query userList form WeBase has error.", e);
            return new BaseListResponse<>(ErrorCode.UNKNOW_ERROR.getCode(), ErrorCode.UNKNOW_ERROR.getCodeDesc());
        }
    }

    /**
     * 根据用户Id查询用户信息, 并将私钥导入到output/admin中, 返回账户信息。
     * @param userId 用户编号
     * @return String 返回用户账户
     */
    public String createAdmin(int userId) {
        String requestUrl = getApi("userInfo");
        requestUrl += "&userId=" + userId;
        try {
            BaseResponse<UserInfo> res = restTemplate.getForObject(requestUrl, BaseResponse.class);
            log.info("[createAdmin] code: {}, msg: {}", res.getCode(), res.getMessage());
            if (res.getCode() == 0) {//说明查询成功
                buildForObject(res, UserInfo.class);
                // 获取私钥
                BigInteger privateInt = getBigIntegerFromBase64(res.getData().getPrivateKey());
                // 根据私钥本地创建文件文件
                return guideService.createAdmin(privateInt.toString(10));
            } else {
                log.error("[createAdmin] the {} does not exists in webase.", userId);
            }
            return StringUtils.EMPTY;
        } catch (Exception e) {
            log.error("[createAdmin] create admin from WeBase has error.", e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 将私钥导入到WeBase中。
     * @param groupId 群组
     * @param userName 账户
     * @param privateKey 私钥
     * @return 返回是否导入成功
     */
    public boolean importPrivateKeyToWeBase(String groupId, String userName, String privateKey) {
        if (this.isIntegrateWebase()) {
            String requestUrl = getApi("importPrivateKey");
            Map<String, Object> request = new HashMap<String, Object>();
            String hexPri = Numeric.toHexStringNoPrefix(new BigInteger(privateKey));
            request.put("privateKey", Base64.encodeBase64String(hexPri.getBytes()));
            request.put("groupId", groupId);
            request.put("description", "From WeID");
            request.put("userName", userName);
            request.put("account", "admin");
            try {
                BaseResponse<?> res = restTemplate.postForObject(requestUrl, request, BaseResponse.class);
                log.info("[importPrivateKeyToWeBase] code: {}, msg: {}", res.getCode(), res.getMessage());
                return res.getCode() == 0;
            } catch (Exception e) {
                log.error("[importPrivateKeyToWeBase] import PrivateKey To WeBase has error.", e);
                return false;
            }
        } else {
            log.warn("[importPrivateKeyToWeBase] the integrate mode is not WeBase.");
            return false;
        }
    }
    
    /**
     * 将公钥导入到WeBase中。
     * @param groupId 群组
     * @param userName 账户
     * @param publicKey 私钥
     * @return 返回是否导入成功
     */
    public boolean importPublicKeyToWeBase(String groupId, String userName, String publicKey) {
        if (this.isIntegrateWebase()) {
            String requestUrl = getApi("importPublicKey");
            Map<String, Object> request = new HashMap<String, Object>();
            String hexPub = Numeric.toHexStringWithPrefix(new BigInteger(publicKey));
            request.put("publicKey", hexPub);
            request.put("groupId", groupId);
            request.put("description", "From WeID");
            request.put("userName", userName);
            request.put("account", "admin");
            try {
                BaseResponse<?> res = restTemplate.postForObject(requestUrl, request, BaseResponse.class);
                log.info("[importPublicKeyToWeBase] code: {}, msg: {}", res.getCode(), res.getMessage());
                return res.getCode() == 0;
            } catch (Exception e) {
                log.error("[importPublicKeyToWeBase] import publicKey To WeBase has error.", e);
                return false;
            }
        } else {
            log.warn("[importPublicKeyToWeBase] the integrate mode is not WeBase.");
            return false;
        }
    }
    
    /**
     * 将合约导入到WeBase中。
     * @param groupId 群组Id
     * @param contractName 合约名称
     * @param contractAddress 合约地址
     * @param contractVersion 合约版本
     * @param hash 合约部署编号
     * @return 返回是否导入成功
     */
    public boolean contractSave(
        String groupId,
        String contractName, 
        String contractAddress,
        String contractVersion,
        String hash
    ) {
        if (this.isIntegrateWebase()) {
            String requestUrl = getApi("contractAddressSave");
            Map<String, Object> request = new HashMap<String, Object>();
            request.put("groupId", groupId);
            request.put("contractName", contractName);
            request.put("contractVersion", contractVersion);
            request.put("contractPath", "weid_" + hash);
            request.put("contractAddress", contractAddress);
            try {
                BaseResponse<?> res = restTemplate.postForObject(requestUrl, request, BaseResponse.class);
                log.info("[contract] code: {}, msg: {}", res.getCode(), res.getMessage());
                return res.getCode() == 0;
            } catch (Exception e) {
                log.error("[contractSave] contract save To WeBase has error.", e);
                return false;
            }
        } else {
            log.warn("[contractSave] the integrate mode is not WeBase.");
            return false;
        }
    }

    private <T> void buildForObject(BaseResponse<T> res, Class<T> clz) {
        T data = res.getData();
        try {
           T v = (T)ConfigUtils.mapToObj((Map<String, Object>)data, clz);
           res.setData(v);
        } catch (Exception e) {
            log.error("[buildForObject] build response data for Object error.", e);
        }
    }

    private <T> void buildForList(BaseResponse<List<T>> res, Class<T> clz) {
        try {
           String json = ConfigUtils.serialize(res.getData());
           List<T> list = ConfigUtils.deserializeToList(json, clz);
           res.setData(list);
        } catch (Exception e) {
            log.error("[buildForList] build response data for list error!", e);
        }
    }

    private BigInteger getBigIntegerFromBase64(String base64) {
        return Numeric.toBigInt(new String(Base64.decodeBase64(base64)));
    }

    /**
     * 根据请求的方法名获取请求URL。
     * @param methodName 请求的方法名
     * @return 返回请求的URL
     */
    private static String getApi(String methodName) {
        File weBaseFile = getWeBaseFile();
        if (!weBaseFile.exists()) {
           throw new WeIdBaseException("the webase info does not exists.");
        }
        String info = FileUtils.readFile(weBaseFile.getAbsolutePath());
        RegisterInfo registerInfo =ConfigUtils.deserialize(info, RegisterInfo.class);
        return getRequestUrl(registerInfo, methodName);
    }

    /**
     * 根据WeBase注册信息构建请求URL。
     * @param registerInfo webase的注冊信息
     * @return 返回請求URL
     */
    private static String getRequestUrl(RegisterInfo registerInfo, String methodName) {
        long timestamp = System.currentTimeMillis();
        String appKey = registerInfo.getAppKey();
        String appSecret = registerInfo.getAppSecret();
        String signature = WeBaseService.md5Encrypt(timestamp, appKey, appSecret);
        String api = "http://" + registerInfo.getWeBaseHost() + WEBASE_CONTEXT + "api/" + methodName;
        String requestUrl = api + "?appKey=" + appKey + "&signature=" + signature + "&timestamp=" + timestamp;
        return requestUrl;
    }

    /**
     * 获取WeBaseFile文件。
     * @return 反WeBaseFile文件对象
     */
    private static File getWeBaseFile() {
        return new File(BuildToolsConstant.OTHER_PATH, BuildToolsConstant.WEBASE_FILE);
    }

    /**
     * webase请求md5签名处理。
     * @param timestamp 时间戳
     * @param appKey 应用key
     * @param appSecret 密码
     * @return 返回签名信息
     */
    private static String md5Encrypt(long timestamp, String appKey, String appSecret) {
        try {
            String dataStr = timestamp + appKey + appSecret;
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result.toUpperCase();
        } catch (Exception e) {
            log.error("[md5Encrypt] md5 error!", e);
        }
        return "";
    }
}
