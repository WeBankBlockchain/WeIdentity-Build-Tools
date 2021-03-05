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
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.webank.weid.config.FiscoConfig;
import com.webank.weid.constant.BuildToolsConstant;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.constant.FileOperator;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.dto.PojoInfo;
import com.webank.weid.service.BaseService;
import com.webank.weid.service.impl.engine.DataBucketServiceEngine;
import com.webank.weid.service.impl.engine.EngineFactory;

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
}
