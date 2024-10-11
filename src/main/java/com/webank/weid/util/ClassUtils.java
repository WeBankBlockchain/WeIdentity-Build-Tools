

package com.webank.weid.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassUtils {

    public static ClassLoader loadJar(String jarPath) throws MalformedURLException {
        File jarFile = new File(jarPath);

        if (!jarFile.exists()) {
            log.error("[loadJar] jar file not found, use default ClassLoader.");
            return Thread.currentThread().getContextClassLoader();
        }
        URL url = jarFile.toURI().toURL();
        return  new URLClassLoader(new URL[] { url }, Thread.currentThread()
                .getContextClassLoader());
    }

    public static String getJarNameByClass(Class<?> clazz) {
        String jarFile = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarFile = jarFile.substring(jarFile.lastIndexOf("/") + 1);
        return jarFile.substring(0, jarFile.lastIndexOf("."));
    }

    public static String getVersionByClass(Class<?> clazz, String replaceChar) {
        String jarName = getJarNameByClass(clazz);
        if (StringUtils.isNotBlank(jarName) || StringUtils.isNotBlank(replaceChar)) {
            return jarName.replace(replaceChar, "");
        }
        return jarName;
    }
}
