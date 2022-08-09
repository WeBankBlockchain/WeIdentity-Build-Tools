

package com.webank.weid.service;

import com.webank.weid.constant.ChainVersion;
import com.webank.weid.service.v3.CheckNodeServiceV3;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.config.Config;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.DataDriverConstant;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.impl.AbstractService;
import com.webank.weid.service.v2.CheckNodeServiceV2;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.PropertyUtils;
import com.webank.weid.util.WeIdSdkUtils;

import static com.webank.weid.constant.ChainVersion.FISCO_V2;
import static com.webank.weid.constant.ChainVersion.FISCO_V3;

/**
 * read run.config file to load configuration
 */
@Service
@Slf4j
public class ConfigService {

    private static boolean nodeCheck = false;
    
    private WeBaseService weBaseService = new WeBaseService();
    
    // private FiscoConfig fiscoConfig;
    
    public boolean isExistsForProperties() {
        if(this.getClass().getClassLoader().getResource("fisco.properties") == null) {
            return false;
        }
        if(this.getClass().getClassLoader().getResource("weidentity.properties") == null) {
            return false;
        }
        return true;
    }
    
    public Map<String, String> loadConfig() {
        log.info("[loadConfig] begin load the run.config");
        //读取基本配置
        List<String> listStr = FileUtils.readFileToList("run.config");
        Map<String, String> map = processConfig(listStr);
        //判断如果节点配置为空，则重载备份配置(output/.run.config)
        if (StringUtils.isBlank(map.get("blockchain_address"))) {
            listStr = FileUtils.readFileToList(BuildToolsConstant.RUN_CONFIG_BAK);
            if (listStr.size() > 0) {
                map = processConfig(listStr);
            }
        }
        //判断证书配置是否存在
        if(FISCO_V3.getVersion() == Integer.parseInt(map.get("blockchain_fiscobcos_version"))) {
            map.put("ca.crt", String.valueOf(FileUtils.exists("resources/conf/ca.crt")));
            map.put("sdk.crt", String.valueOf(FileUtils.exists("resources/conf/sdk.crt")));
            map.put("sdk.key", String.valueOf(FileUtils.exists("resources/conf/sdk.key")));
            map.put("smca.crt", String.valueOf(FileUtils.exists("resources/conf/smca.crt")));
            map.put("smsdk.crt", String.valueOf(FileUtils.exists("resources/conf/smsdk.crt")));
            map.put("smsdk.key", String.valueOf(FileUtils.exists("resources/conf/smsdk.key")));
            map.put("smensdk.crt", String.valueOf(FileUtils.exists("resources/conf/smensdk.crt")));
            map.put("smensdk.key", String.valueOf(FileUtils.exists("resources/conf/smensdk.key")));
            map.put("useWeBase", String.valueOf(weBaseService.isIntegrateWebase()));
        } else {
            map.put("ca.crt", String.valueOf(FileUtils.exists("resources/conf/ca.crt")));
            map.put("sdk.crt", String.valueOf(FileUtils.exists("resources/conf/sdk.crt")));
            map.put("sdk.key", String.valueOf(FileUtils.exists("resources/conf/sdk.key")));
            map.put("gmca.crt", String.valueOf(FileUtils.exists("resources/conf/gm/gmca.crt")));
            map.put("gmsdk.crt", String.valueOf(FileUtils.exists("resources/conf/gm/gmsdk.crt")));
            map.put("gmsdk.key", String.valueOf(FileUtils.exists("resources/conf/gm/gmsdk.key")));
            map.put("gmensdk.crt", String.valueOf(FileUtils.exists("resources/conf/gm/gmensdk.crt")));
            map.put("gmensdk.key", String.valueOf(FileUtils.exists("resources/conf/gm/gmensdk.key")));
            map.put("useWeBase", String.valueOf(weBaseService.isIntegrateWebase()));
        }
        return map;
    }
    
    private Map<String, String> processConfig(List<String> listStr) {
        Map<String, String> map = new HashMap<String, String>();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                continue;
            }
            String[] values = string.split("=");
            if (values.length == 2) {
                map.put(values[0], values[1]);
            } else {
                map.put(values[0], StringUtils.EMPTY);
            }
        }
        return map;
    }

    /**
     *
     * @param address node ip port
     * @param version 2 or 3
     * @param useSmCrypto todo 是否使用国密SSL
     * @param orgId
     * @param amopId
     * @param groupId
     * @param profileActive
     * @return
     */
    private boolean processNodeConfig(
            String address,
            String version,
            String useSmCrypto,
            String orgId,
            String amopId,
            String groupId,
            String profileActive
    ) {
        List<String> listStr = FileUtils.readFileToList("run.config");
        StringBuffer buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }
            if (string.startsWith("blockchain_address")) {
                buffer.append("blockchain_address=").append(address).append("\n");
            } else if (string.startsWith("blockchain_fiscobcos_version")) {
                buffer.append("blockchain_fiscobcos_version=").append(version).append("\n");
            } else  if (string.startsWith("org_id")) {
                buffer.append("org_id=").append(orgId).append("\n");
            } else  if (string.startsWith("amop_id")) {
                buffer.append("amop_id=").append(amopId).append("\n");
            } else if (string.startsWith("group_id")) {
                buffer.append("group_id=").append(groupId).append("\n");
            } else if (string.startsWith("sm_crypto")) {
                buffer.append("sm_crypto=").append(useSmCrypto).append("\n");
            } else if (string.startsWith("cns_profile_active")) {
                buffer.append("cns_profile_active=").append(profileActive).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "run.config", FileOperator.OVERWRITE);
        backRunConfig();
        //根据模板生成配置文件
        return generateProperties();
    }
    
    private void backRunConfig() {
        FileUtils.copy(new File("run.config"), new File(BuildToolsConstant.RUN_CONFIG_BAK));
    }
    
    public void updateChainId(String chainId) {
        List<String> listStr = FileUtils.readFileToList("run.config");
        StringBuffer buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }
            if (string.startsWith("chain_id")) {
                buffer.append("chain_id=").append(chainId).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "run.config", FileOperator.OVERWRITE);
        backRunConfig();
    }


    public boolean setMasterGroupId(String groupId) {
        List<String> listStr = FileUtils.readFileToList("run.config");
        StringBuffer buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }
            if (string.startsWith("group_id")) {
                buffer.append("group_id=").append(groupId).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "run.config", FileOperator.OVERWRITE);
        backRunConfig();
        //根据模板生成配置文件
        return generateProperties();
    }

    public boolean processDbConfig(String persistenceType, String mysqlAddress, String database,
                                   String username,
                                   String mysqlPassword, String redisAddress,
                                   String redisPassword) {
        List<String> listStr = FileUtils.readFileToList("run.config");
        StringBuffer buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }

            if (string.startsWith("persistence_type")){
                buffer.append("persistence_type=").append(replaceNull(persistenceType)).append("\n");
            }else if (string.startsWith("mysql_address")) {
                buffer.append("mysql_address=").append(replaceNull(mysqlAddress)).append("\n");
            } else if (string.startsWith("mysql_database")) {
                buffer.append("mysql_database=").append(replaceNull(database)).append("\n");
            } else  if (string.startsWith("mysql_username")) {
                buffer.append("mysql_username=").append(replaceNull(username)).append("\n");
            } else if (string.startsWith("mysql_password")) {
                buffer.append("mysql_password=").append(replaceNull(mysqlPassword)).append("\n");
            } else if (string.startsWith("redis_address")) {
                buffer.append("redis_address=").append(replaceNull(redisAddress)).append("\n");
            } else if (string.startsWith("redis_password")) {
                buffer.append("redis_password=").append(replaceNull(redisPassword)).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "run.config", FileOperator.OVERWRITE);
        backRunConfig();
        //根据模板生成配置文件
        return generateProperties();
    }

    //将空值转换成空字符串
    private String replaceNull(String v) {
        if (v == null) {
            return "";
        }
        return v;
    }
    
    public boolean checkDb() {
        log.info("[checkDb] begin reload the properties...");
        PropertyUtils.reload();
        String dbUrl = PropertyUtils.getProperty("datasource1." + DataDriverConstant.JDBC_URL);
        String userNameKey = "datasource1." + DataDriverConstant.JDBC_USER_NAME;
        String userName = PropertyUtils.getProperty(userNameKey);
        String passWordKey = "datasource1." + DataDriverConstant.JDBC_USER_PASSWORD;
        String passWord = PropertyUtils.getProperty(passWordKey);
        if (StringUtils.isBlank(dbUrl) || StringUtils.isBlank(userName) || StringUtils.isBlank(passWord)) {
            return false;
        }
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(dbUrl, userName, passWord);
            if(connection != null) {
                log.info("[checkDb] the db check successfully.");
               return true;
            } else {
                log.error("[checkDb] the db check fail...");
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("[checkDb] the db check has error.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("[checkDb] the connection close error.", e);
                }
            }
        }
        return false;
    }

    public boolean checkRedis() {
        log.info("[checkRedis] begin reload the properties...");
        PropertyUtils.reload();
        Config config = new Config();
        RedissonClient client = null;
        String redisUrl = PropertyUtils.getProperty(DataDriverConstant.REDIS_URL);
        if (StringUtils.isBlank(redisUrl)) {
            return false;
        }
        String redisPassword = PropertyUtils.getProperty(DataDriverConstant.PASSWORD);
        String passWord = null;
        if (StringUtils.isNoneBlank(redisPassword)) {
            passWord = redisPassword;
        }
        List<String> redisNodes = Arrays.asList(redisUrl.split(","));
        if (redisNodes.size() > 1) {
            List<String> clusterNodes = new ArrayList<>();
            for (int i = 0; i < redisNodes.size(); i++) {
                clusterNodes.add("redis://" + redisNodes.get(i));
            }
            config.useClusterServers().addNodeAddress(clusterNodes.toArray(
                    new String[clusterNodes.size()]))
                    .setPassword(passWord);
        } else {
            config.useSingleServer().setAddress("redis://" + redisUrl)
                    .setPassword(passWord);
        }

        try {
            client = Redisson.create(config);

            if(client != null) {
                log.info("[checkRedis] the redis check successfully.");
                return true;
            } else {
                log.error("[checkRedis] the redis check fail...");
            }
        } catch (RedisException e) {
            log.error("[checkRedis] the redis check has error.", e);
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (RedisException e) {
                    log.error("[checkRedis] the redis connection close error.", e);
                }
            }
        }
        return false;
    }

    /**
     * 根据run.config中生成
     * @return
     */
    private boolean generateProperties() {
        log.info("[generateProperties] begin generate properties and copy to classpath...");
        try {
            Map<String, String> loadConfig = loadConfig();
            generateFiscoProperties(loadConfig);
            generateWeidentityProperties(loadConfig);
            //windows 将resources下的文件复制到classPath中,linux上面默认指定resources为classpath
            String osName = System.getProperty("os.name").toLowerCase();
            if(osName.contains("windows")) {
                File[] resources = new File("resources/").listFiles();
                for (File file : resources) {
                    String bin = this.getClass().getClassLoader().getResource(".").getFile();
                    File targetFile = new File(bin + "/" + file.getName());
                    FileUtils.copy(file, targetFile); 
                }
            }
            return true;
        } catch (Exception e) {
            log.error("[generateProperties] generate error.", e);
            return false;
        }
    }
    
    private void generateFiscoProperties(Map<String, String> loadConfig) {
        log.info("[generateFiscoProperties] begin generate fisco.properties...");
        String fileStr = FileUtils.readFile("common/script/tpl/fisco.properties.tpl");
        fileStr = fileStr.replace("${FISCO_BCOS_VERSION}", loadConfig.get("blockchain_fiscobcos_version"));
//        fileStr = fileStr.replace("${CHAIN_ID}", loadConfig.get("chain_id"));
        fileStr = fileStr.replace("${MASTER_GROUP_ID}", loadConfig.get("group_id"));
        fileStr = fileStr.replace("${SDK_SM_CRYPTO}", loadConfig.get("sm_crypto"));
        fileStr = fileStr.replace("${WEID_ADDRESS}", "");
        fileStr = fileStr.replace("${CPT_ADDRESS}", "");
        fileStr = fileStr.replace("${ISSUER_ADDRESS}", "");
        fileStr = fileStr.replace("${EVIDENCE_ADDRESS}", "");
        fileStr = fileStr.replace("${SPECIFICISSUER_ADDRESS}", "");
        fileStr = fileStr.replace("${CNS_PROFILE_ACTIVE}", loadConfig.get("cns_profile_active"));
        //将文件写入resource目录
        FileUtils.writeToFile(fileStr, "resources/fisco.properties", FileOperator.OVERWRITE);
    }
    
    private void generateWeidentityProperties(Map<String, String> loadConfig) {
        log.info("[generateWeidentityProperties] begin generate weidentity.properties...");
        String fileStr = FileUtils.readFile("common/script/tpl/weidentity.properties.tpl");
        fileStr = fileStr.replace("${ORG_ID}", loadConfig.get("org_id"));
        fileStr = fileStr.replace("${AMOP_ID}", loadConfig.get("amop_id"));
        fileStr = fileStr.replace("${BLOCKCHIAN_NODE_INFO}", loadConfig.get("blockchain_address"));
        fileStr = fileStr.replace("${PERSISTENCE_TYPE}", loadConfig.get("persistence_type"));
        fileStr = fileStr.replace("${MYSQL_ADDRESS}", loadConfig.get("mysql_address"));
        fileStr = fileStr.replace("${MYSQL_DATABASE}", loadConfig.get("mysql_database"));
        fileStr = fileStr.replace("${MYSQL_USERNAME}", loadConfig.get("mysql_username"));
        fileStr = fileStr.replace("${MYSQL_PASSWORD}", loadConfig.get("mysql_password"));
        fileStr = fileStr.replace("${REDIS_ADDRESS}", loadConfig.get("redis_address"));
        fileStr = fileStr.replace("${REDIS_PASSWORD}", loadConfig.get("redis_password"));
        //将文件写入resource目录
        FileUtils.writeToFile(fileStr, "resources/weidentity.properties", FileOperator.OVERWRITE);
    }
    
    /**
     * 启用CNS.
     * @param hash 需要启用的cns值
     */
    @Deprecated
    public void enableHash(String hash) {
        //更新cns配置
        List<String> listStr = FileUtils.readFileToList("run.config");
        StringBuffer buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }
            if (string.startsWith("cns_contract_follow=")) {
                buffer.append("cns_contract_follow=").append(hash).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "run.config", FileOperator.OVERWRITE);
        //更新weidentity.properties配置
        listStr = FileUtils.readFileToList("resources/fisco.properties");
        buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }
            if (string.startsWith("cns.contract.follow=")) {
                buffer.append("cns.contract.follow=").append(hash).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "resources/fisco.properties", FileOperator.OVERWRITE);

        //window将配置文件更新到classpath linux中默认将resource设置为classpath
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("windows")) {
            File file = new File("resources/fisco.properties");
            String bin = this.getClass().getClassLoader().getResource(".").getFile();
            File targetFile = new File(bin + "/" + file.getName());
            //如果classpath目录不一样则复制（window需要）
            if (!file.getAbsoluteFile().equals(targetFile.getAbsoluteFile())) {
                FileUtils.copy(file, targetFile); 
            }
        }
        //重新加载地址
        reloadAddress();
    }
    
    public void reloadAddress() {
        PropertyUtils.reload();
        new AbstractService() {
            {
                reloadContract();
            }
        };
    }

    public ResponseData<Boolean> nodeConfigUpload(HttpServletRequest request) {
        nodeCheck = false;
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        File targetFile = new File(BuildToolsConstant.RESOURCES_PATH + "conf"); // 存在conf目录下
        if(request.getParameter("version").equals("2") && request.getParameter("useSmCrypto").equals("1")){
            targetFile = new File(BuildToolsConstant.RESOURCES_PATH + "conf/gm"); // 如果是2.0的国密链，存在conf/gm目录下
        }
        if (!targetFile.exists()) {
            boolean result = targetFile.mkdirs();
            log.info("create conf in resource result: {}", result);
        }
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            if(!fileName.endsWith(".crt") && !fileName.endsWith(".key")) {
                log.error("[nodeConfigUpload] the file type error, fileName = {}.", fileName);
                return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
            }
            File dest = new File(targetFile.getAbsoluteFile() + "/" + fileName);
            try {
                file.transferTo(dest);
                log.info("[nodeConfigUpload] the {} upload success", fileName);
            } catch (IOException e) {
                log.error("[nodeConfigUpload] the {} upload fail", fileName, e);
                return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
            }
        }
        log.info("[nodeConfigUpload] begin update run.config...");
        //更新run.config
        Map<String, String> configMap = loadConfig();
        String orgId = request.getParameter("orgId");
        String amopId = request.getParameter("amopId");
        String version = request.getParameter("version");
        String useSmCrypto = request.getParameter("useSmCrypto"); // todo
        String ipPort = request.getParameter("ipPort");
        String groupId = configMap.get("group_id");
        if (FISCO_V2.getVersion() == Integer.parseInt(version)) {
            groupId = "1";
        }
        if (FISCO_V3.getVersion() == Integer.parseInt(version)) {
            groupId = "group0";
        }
        log.info("nodeConfigUpload groupId:{}", groupId);
        if (!ChainVersion.contains(Integer.parseInt(version))) {
            throw new RuntimeException("chain version only support 2 or 3");
        }
        String profileActive = configMap.get("cns_profile_active");
        //根据模板生成配置文件
        if(processNodeConfig(ipPort, version, useSmCrypto, orgId, amopId, groupId, profileActive)) {
            return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
        }
        return new ResponseData<>(Boolean.FALSE, ErrorCode.UNKNOW_ERROR);
    }

    public ResponseData<Boolean> checkNode() {
        try {
            nodeCheck = false;
            log.info("[checkNode] begin check the node...");
            if (!isExistsForProperties()) {
                return new ResponseData<>(Boolean.FALSE,
                        ErrorCode.UNKNOW_ERROR.getCode(),
                        "the configuration file does not exist.");
            }

            CheckNodeFace checkNode = null;
            FiscoConfig fiscoConfig = WeIdSdkUtils.loadNewFiscoConfig();
            if (fiscoConfig.getVersion().startsWith(WeIdConstant.FISCO_BCOS_1_X_VERSION_PREFIX)) {
                return new ResponseData<>(Boolean.FALSE,
                        ErrorCode.UNKNOW_ERROR.getCode(),
                        "not support 1.x version.");
            } else if (fiscoConfig.getVersion().startsWith(WeIdConstant.FISCO_BCOS_2_X_VERSION_PREFIX)) {
                log.info("[checkNode] the node version is 2.x in your configuration.");
                checkNode = new CheckNodeServiceV2();
            } else {
                log.info("[checkNode] the node version is 3.x in your configuration.");
                checkNode = new CheckNodeServiceV3();
            }
            if (!checkNode.check(fiscoConfig)) {
                log.error("[checkNode] checkNode with fail.");
                //configService.reloadAddress();
                return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), "checkNode with fail.");
            }
            log.info("[checkNode] the node check successfull.");
            nodeCheck = true;
        } catch (WeIdBaseException e) {
            log.error("[checkNode] checkNode with same exception.", e);
            return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[checkNode] checkNode with unkonw exception.", e);
            return new ResponseData<>(Boolean.FALSE, ErrorCode.BASE_ERROR.getCode(), e.getMessage());
        }

        return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
    }

    public ResponseData<Boolean> nodeCheckState() {
        if (!nodeCheck) {
            return checkNode();
        }
        return new ResponseData<>(Boolean.TRUE, ErrorCode.SUCCESS);
    }

    public ResponseData<String> getRoleType() {
        File roleFile = new File(BuildToolsConstant.OTHER_PATH, BuildToolsConstant.ROLE_FILE);
        if (!roleFile.exists()) {
            return new ResponseData<>(StringUtils.EMPTY, ErrorCode.UNKNOW_ERROR.getCode(), "role file not exists");
        }
        return new ResponseData<>(FileUtils.readFile(roleFile.getAbsolutePath()), ErrorCode.SUCCESS);
    }

    public void setNodeCheck(boolean flag) {
        nodeCheck = flag;
    }

    public String getMasterGroupId() {
        return this.loadConfig().get("group_id");
    }
}
