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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.DataDriverConstant;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.service.impl.AbstractService;
import com.webank.weid.util.FileUtils;
import com.webank.weid.util.PropertyUtils;
import com.webank.weid.util.ZipUtils;

@Service
public class ConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    
    private static final String RUN_CONFIG_BAK = "output/.run.config";
    
    public FiscoConfig loadNewFiscoConfig() {
        logger.info("[loadNewFiscoConfig] reload the properties...");
        PropertyUtils.reload();
        FiscoConfig fiscoConfig = new FiscoConfig();
        fiscoConfig.load();
        logger.info("[loadNewFiscoConfig] the properties reload successfully.");
        return fiscoConfig;
    }
    
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
        logger.info("[loadConfig] begin load the run.config");
        //读取基本配置
        List<String> listStr = FileUtils.readFileToList("run.config");
        Map<String, String> map = processConfig(listStr);
        //判断如果节点配置为空，则重载备份配置
        if (StringUtils.isBlank(map.get("blockchain_address"))) {
            listStr = FileUtils.readFileToList(RUN_CONFIG_BAK);
            if (listStr.size() > 0) {
                map = processConfig(listStr);
            }
        }
        //判断证书配置是否存在
        map.put("ca.crt", String.valueOf(FileUtils.exists("resources/ca.crt")));
        map.put("client.keystore", String.valueOf(FileUtils.exists("resources/client.keystore")));
        map.put("node.crt", String.valueOf(FileUtils.exists("resources/node.crt")));
        map.put("node.key", String.valueOf(FileUtils.exists("resources/node.key")));
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
    
    public boolean processNodeConfig(
        String address, 
        String version, 
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
            }  else if (string.startsWith("group_id")) {
                buffer.append("group_id=").append(groupId).append("\n");
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
        FileUtils.copy(new File("run.config"), new File(RUN_CONFIG_BAK));
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
    
    public boolean processDbConfig(String address, String database, String username, String password) {
        List<String> listStr = FileUtils.readFileToList("run.config");
        StringBuffer buffer = new StringBuffer();
        for (String string : listStr) {
            if (string.startsWith("#") || string.indexOf("=") == -1) {
                buffer.append(string).append("\n");
                continue;
            }
            if (string.startsWith("mysql_address")) {
                buffer.append("mysql_address=").append(address).append("\n");
            } else if (string.startsWith("mysql_database")) {
                buffer.append("mysql_database=").append(database).append("\n");
            } else  if (string.startsWith("mysql_username")) {
                buffer.append("mysql_username=").append(username).append("\n");
            } else if (string.startsWith("mysql_password")) {
                buffer.append("mysql_password=").append(password).append("\n");
            } else {
                buffer.append(string).append("\n");
            }
        }
        FileUtils.writeToFile(buffer.toString(), "run.config", FileOperator.OVERWRITE);
        backRunConfig();
        //根据模板生成配置文件
        return generateProperties();
    }
    
    public boolean checkDb() {
        logger.info("[checkDb] begin reload the properties...");
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
                logger.info("[checkDb] the db check successfully.");
               return true;
            } else {
                logger.error("[checkDb] the db check fail...");
            }
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("[checkDb] the db check has error.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("[checkDb] the connection close error.", e);
                }
            }
        }
        return false;
    }
    
    private boolean generateProperties() {
        logger.info("[generateProperties] begin generate properties and copy to classpath...");
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
            logger.error("[generateProperties] generate error.", e);
            return false;
        }
    }
    
    private void generateFiscoProperties(Map<String, String> loadConfig) {
        logger.info("[generateFiscoProperties] begin generate fisco.properties...");
        String fileStr = FileUtils.readFile("common/script/tpl/fisco.properties.tpl");
        fileStr = fileStr.replace("${FISCO_BCOS_VERSION}", loadConfig.get("blockchain_fiscobcos_version"));
        fileStr = fileStr.replace("${CHAIN_ID}", loadConfig.get("chain_id"));
        fileStr = fileStr.replace("${GROUP_ID}", loadConfig.get("group_id"));
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
        logger.info("[generateWeidentityProperties] begin generate weidentity.properties...");
        String fileStr = FileUtils.readFile("common/script/tpl/weidentity.properties.tpl");
        fileStr = fileStr.replace("${ORG_ID}", loadConfig.get("org_id"));
        fileStr = fileStr.replace("${AMOP_ID}", loadConfig.get("amop_id"));
        fileStr = fileStr.replace("${BLOCKCHIAN_NODE_INFO}", loadConfig.get("blockchain_address"));
        fileStr = fileStr.replace("${MYSQL_ADDRESS}", loadConfig.get("mysql_address"));
        fileStr = fileStr.replace("${MYSQL_DATABASE}", loadConfig.get("mysql_database"));
        fileStr = fileStr.replace("${MYSQL_USERNAME}", loadConfig.get("mysql_username"));
        fileStr = fileStr.replace("${MYSQL_PASSWORD}", loadConfig.get("mysql_password"));
        //将文件写入resource目录
        FileUtils.writeToFile(fileStr, "resources/weidentity.properties", FileOperator.OVERWRITE);
    }
    
    public boolean toZip(String srcPath, String targetPath) {
        File resourcesFile = new File(targetPath);
        if (resourcesFile.exists()) {
            logger.info("[toZip] {} is exists, begin to delete it...", targetPath);
            FileUtils.delete(resourcesFile);
        }
        FileOutputStream out = null;
        try {
            logger.info("[toZip] begin to zipFile...");
            out = new FileOutputStream(targetPath);
            ZipUtils.toZip(srcPath, out, true);
            return true;
        } catch (FileNotFoundException e) {
            logger.error("[toZip] the file to zip error.", e);
        } finally {
            FileUtils.close(out);
        }
        return false;
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
    
}
