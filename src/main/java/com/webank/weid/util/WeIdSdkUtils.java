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
package com.webank.weid.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.JarURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.dto.ContractSolInfo;
import com.webank.weid.dto.DeployInfo;
import com.webank.weid.dto.PojoInfo;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.service.BaseService;
import com.webank.weid.service.impl.engine.DataBucketServiceEngine;
import com.webank.weid.service.impl.engine.EngineFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeIdSdkUtils {

    public static boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

    public static FiscoConfig loadNewFiscoConfig() {
        log.info("[loadNewFiscoConfig] reload the properties...");
        PropertyUtils.reload();
        FiscoConfig fiscoConfig = new FiscoConfig();
        fiscoConfig.load();
        log.info("[loadNewFiscoConfig] the properties reload successfully.");
        return fiscoConfig;
    }

    public static DataBucketServiceEngine getDataBucket(CnsType cnsType) {
        return EngineFactory.createDataBucketServiceEngine(cnsType);
    }

    public static String getMainHash() {
        org.fisco.bcos.web3j.precompile.cns.CnsInfo cnsInfo = BaseService.getBucketByCns(CnsType.ORG_CONFING);
        if (cnsInfo == null) {
            return StringUtils.EMPTY;
        }
        return getDataBucket(CnsType.ORG_CONFING).get(
                WeIdConstant.CNS_GLOBAL_KEY, WeIdConstant.CNS_MAIN_HASH).getResult();
    }

    /**
     * 根据java文件生成jar包
     * @param sourceFile 源文件路径
     * @param cptIds jar包含的cpt集合
     * @param fromType  转换来源
     * @param from  数据来源
     * @return 返回是否创建成功
     */
    public static boolean createJar(File sourceFile, Integer[] cptIds, String fromType, DataFrom from) {
        log.info("[createJar] begin create jar.");
        String javaClassPath = sourceFile.getParentFile().getAbsolutePath() + "/classes";
        String targetPath = sourceFile.getParentFile().getAbsolutePath() + "/" + BuildToolsConstant.CPT_JAR_NAME;
        try {
            CompilerAndJarTools.instance(sourceFile.getAbsolutePath(), javaClassPath, targetPath).complier().generateJar();
            log.info("[createJar] create jar successfully.");
            log.info("[createJar] begin save the pojo info.");
            //保存pojo信息
            File pojoFile = new File(sourceFile.getParentFile().getAbsolutePath(), "info");
            String data = DataToolUtils.serialize(
                    buildPojoInfo(sourceFile.getParentFile().getName(), cptIds, fromType, from)
            );
            FileUtils.writeToFile(data, pojoFile.getAbsolutePath(), FileOperator.OVERWRITE);
            return true;
        } catch (Exception e) {
            log.error("[createJar] create jar has error.", e);
            return false;
        }
    }

    public static File getJarFile(String pojoId) {
        pojoId = FileUtils.getSecurityFileName(pojoId);
        return new File(getPojoDir().getAbsoluteFile() + "/" + pojoId,
                BuildToolsConstant.CPT_JAR_NAME);
    }

    public static void deletePojoInfo(String pojoId) {
        FileUtils.deleteAll(new File(getPojoDir().getAbsoluteFile() + "/" + pojoId));
    }

    public static File getSourceFile(String pojoId) {
        return new File(getPojoDir().getAbsoluteFile() + "/" + pojoId + "/java");
    }

    private static PojoInfo buildPojoInfo(String id, Integer[] cptIds, String fromType, DataFrom from) {
        PojoInfo info = new PojoInfo();
        info.setCptIds(cptIds);
        info.setHash(WeIdSdkUtils.getMainHash());
        info.setId(id);
        info.setTime(System.currentTimeMillis());
        info.setFrom(from.name());
        info.setFromType(fromType);
        return info;
    }

    private static File getPojoDir() {
        return new File(BuildToolsConstant.POJO_PATH + "/" + WeIdSdkUtils.getMainHash());
    }

    public static boolean toZip(String srcPath, String targetPath) {
        File resourcesFile = new File(targetPath);
        if (resourcesFile.exists()) {
            log.info("[toZip] {} is exists, begin to delete it...", targetPath);
            FileUtils.delete(resourcesFile);
        }
        FileOutputStream out = null;
        try {
            log.info("[toZip] begin to zipFile...");
            out = new FileOutputStream(targetPath);
            ZipUtils.toZip(srcPath, out, true);
            return true;
        } catch (FileNotFoundException e) {
            log.error("[toZip] the file to zip error.", e);
        } finally {
            FileUtils.close(out);
        }
        return false;
    }

    public static byte[] getJarBytes(String id) {
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
            log.error("[getJarBytes] read byte error for down cpt jar.", e);
            return null;
        } finally {
            FileUtils.close(in);
            FileUtils.close(bos);
        }
    }

    public static WeIdPrivateKey getCurrentPrivateKey() {
        WeIdPrivateKey weIdPrivate = new WeIdPrivateKey();
        File targetDir = new File(BuildToolsConstant.ADMIN_PATH, BuildToolsConstant.ECDSA_KEY);
        weIdPrivate.setPrivateKey(FileUtils.readFile(targetDir.getAbsolutePath()));
        return weIdPrivate;
    }

    public static WeIdPrivateKey getWeIdPrivateKey(String hash) {
        // 根据部署编码获取当次部署的私钥
        DeployInfo deployInfo = getDepolyInfoByHash(hash);
        WeIdPrivateKey weIdPrivateKey = new WeIdPrivateKey();
        if (deployInfo != null) {
            weIdPrivateKey.setPrivateKey(deployInfo.getEcdsaKey());
            return weIdPrivateKey;
        }
        return getCurrentPrivateKey();
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

    private static File getDeployFileByHash(String hash) {
        hash = FileUtils.getSecurityFileName(hash);
        return new File(BuildToolsConstant.DEPLOY_PATH, hash);
    }

    public static List<ContractSolInfo> getContractList(Integer encryptType) {
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("windows")) {
            return getContractListForWin(encryptType);
        }
        return getContractListForLinux(encryptType);
    }
    
    public static List<ContractSolInfo> getContractListForWin(Integer encryptType) {
        URL contractUri = Thread.currentThread()
            .getContextClassLoader().getResource(BuildToolsConstant.CONTRACT_PATH);
        File file = new File(contractUri.getFile());
        List<ContractSolInfo> contractSolList = new ArrayList<ContractSolInfo>();
        for (File contractFile : file.listFiles()) {
            String contractName = contractFile.getName();
            contractName = contractName.substring(0, contractName.indexOf("."));
            ContractSolInfo contract = buildContractSolInfo(contractName, encryptType);
            String contractSource= FileUtils.readFile(contractFile.getAbsolutePath());
            contract.setContractSource(Base64.encodeBase64String(contractSource.getBytes()));
            contractSolList.add(contract);
        }
        return contractSolList;
    }

    private static List<ContractSolInfo> getContractListForLinux(Integer encryptType) {
        JarURLConnection jarCon = null;
        JarFile jarFile = null;
        List<ContractSolInfo> contractSolList = new ArrayList<ContractSolInfo>();
        try {
            String path = BuildToolsConstant.CONTRACT_PATH + "/";
            URL url = WeIdSdkUtils.class.getClassLoader().getResource(path);
            String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);
            jarCon  = (JarURLConnection) new URL(jarPath).openConnection();
            jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntrys = jarFile.entries();
            while (jarEntrys.hasMoreElements()) {
                JarEntry entry = jarEntrys.nextElement();
                String name = entry.getName();
                if (name.startsWith(path) && !entry.isDirectory()) {
                    String contractName = name.substring(name.indexOf("/") + 1, name.indexOf("."));
                    ContractSolInfo contract = buildContractSolInfo(contractName, encryptType);
                    String contractSource= FileUtils.readFileAsStream(name);
                    contract.setContractSource(Base64.encodeBase64String(contractSource.getBytes()));
                    contractSolList.add(contract);
                }
            }
        } catch (IOException e) {
            log.error("[getContractListForLinux] get contractList has error!", e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.error("[getContractListForLinux] jarFile close error!", e);
                }
            }
        }
        return contractSolList;
    }

    private static ContractSolInfo buildContractSolInfo(String contractName, Integer encryptType) {
        ContractSolInfo contract = new ContractSolInfo();
        contract.setContractName(contractName);
        try {
            Class<?> forName = Class.forName("com.webank.weid.contract.v2." + contractName);
            contract.setContractAbi(forName.getField("ABI").get(forName).toString());
            if (encryptType == 0) {
                contract.setBytecodeBin(forName.getField("BINARY").get(forName).toString());
            } else {
                contract.setBytecodeBin(forName.getField("SM_BINARY").get(forName).toString());
            }
        } catch(Exception e) {
            log.error("[buildContractSolInfo] get the abi and bin has error. ", e);
        }
        return contract;
    }
}
