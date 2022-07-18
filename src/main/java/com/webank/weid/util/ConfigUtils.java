
package com.webank.weid.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.webank.weid.exception.DataTypeCastException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/**
 * tools for properties.
 *
 * @author tonychen 2019/3/21
 */
@Slf4j
public final class ConfigUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer()
        .withDefaultPrettyPrinter();

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

    public static String serializeWithPrinter(Object object) {
        String result;
        try {
            result = OBJECT_WRITER.withDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result = StringUtils.EMPTY;
        }
        return result;
    }

    /**
     * deserialize a JSON String to an class instance.
     *
     * @param json json string
     * @param clazz Class.class
     * @param <T> the type of the element
     * @return class instance
     */
    public static <T> T deserialize(String json, Class<T> clazz) {
        Object object = null;
        try {
            object = OBJECT_MAPPER.readValue(json, TypeFactory.rawClass(clazz));
        } catch (JsonParseException e) {
            log.error("JsonParseException when deserialize json to object", e);
            throw new DataTypeCastException(e);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException when deserialize json to object", e);
            throw new DataTypeCastException(e);
        } catch (IOException e) {
            log.error("IOException when deserialize json to object", e);
            throw new DataTypeCastException(e);
        }
        return (T) object;
    }

    /**
     * deserialize a JSON String to List.
     *
     * @param json json string
     * @param clazz Class.class
     * @param <T> the type of the element
     * @return class instance
     */
    public static <T> List<T> deserializeToList(String json, Class<T> clazz) {
        List<T> object = null;
        try {
            JavaType javaType =
                OBJECT_MAPPER.getTypeFactory()
                    .constructParametricType(ArrayList.class, TypeFactory.rawClass(clazz));
            object = OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonParseException e) {
            log.error("JsonParseException when serialize object to json", e);
            throw new DataTypeCastException(e);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException when serialize object to json", e);
            new DataTypeCastException(e);
        } catch (IOException e) {
            log.error("IOException when serialize object to json", e);
        }
        return object;
    }

    /**
     * Convert a MAP to POJO.
     *
     * @param map the input data
     * @param <T> the type of the element
     * @param clazz the output class type
     * @return object in T type
     * @throws Exception IOException
     */
    public static <T> T mapToObj(Map<String, Object> map, Class<T> clazz) throws Exception {
        final T pojo = (T) OBJECT_MAPPER.convertValue(map, clazz);
        return pojo;
    }

    public static <T extends Map> T objToMap(Object obj, Class<T> cls) throws IOException {
        return (T)OBJECT_MAPPER.readValue(serialize(obj), cls);
    }

    public static String getCurrentOrgId() {
        return PropertyUtils.getProperty("blockchain.orgid");
    }

    public static int getEncryptType() {
        return Integer.parseInt(PropertyUtils.getProperty("encrypt.type"));
    }
}
