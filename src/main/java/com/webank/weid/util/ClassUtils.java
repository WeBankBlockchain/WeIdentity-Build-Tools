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
