package com.webank.weid.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);
    
    public static ClassLoader loadJar(String jarPath) throws MalformedURLException {
        File jarFile = new File(jarPath);

        if (jarFile.exists() == false) {
            logger.error("[loadJar] jar file not found, use default ClassLoader.");
            return Thread.currentThread().getContextClassLoader();
        }
        URL url = jarFile.toURI().toURL();
        return  new URLClassLoader(new URL[] { url }, Thread.currentThread()
                .getContextClassLoader());
    }

}
