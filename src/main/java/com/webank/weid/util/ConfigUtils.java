/*
 *       Copyright© (2018-2019) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-java-sdk.
 *
 *       weidentity-java-sdk is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-java-sdk is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-java-sdk.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.webank.weid.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;

/**
 * tools for properties.
 *
 * @author tonychen 2019/3/21
 */
public final class ConfigUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer()
        .withDefaultPrettyPrinter();
    private static Properties prop = new Properties();

    /**
     * load properties from specific config file.
     *
     * @param filePath properties config file.
     */
    public static void loadProperties(String filePath) throws IOException {

        InputStream in;
        in = new FileInputStream(new File(filePath));
        prop.load(in);
        in.close();
    }

    /**
     * get property value by specific key.
     *
     * @param key property key
     */
    public static String getProperty(String key) {
        return prop.getProperty(key);
    }

    /**
     * get property value by specific key.
     *
     * @param key property key
     */
    public static String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }

    public static String serialize(Object object) {

        String result;
        try {
            result = OBJECT_WRITER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result = StringUtils.EMPTY;
        }
        return result;
    }
    
    public static <T extends Map> T objToMap(Object obj, Class<T> cls) throws IOException {
        return (T)OBJECT_MAPPER.readValue(serialize(obj), cls);
    }
}
