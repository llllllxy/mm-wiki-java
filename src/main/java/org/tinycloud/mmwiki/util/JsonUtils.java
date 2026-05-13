package org.tinycloud.mmwiki.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Json工具类（基于jackson）
 *
 * @author liuxingyu01
 * @version 2022-03-11-16:47
 **/
public class JsonUtils {
    final static Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper getObjectMapper() {
        return SpringContextUtils.getBean(ObjectMapper.class);
    }

    /**
     * 对象转JSON字符串
     *
     * @param value 待转换对象
     * @return JSON字符串
     */
    public static String writeValueAsString(Object value) {
        if (value != null) {
            try {
                return getObjectMapper().writeValueAsString(value);
            } catch (JsonProcessingException e) {
                if (log.isErrorEnabled()) {
                    log.error("JsonUtils -- writeValueAsString -- Exception=", e);
                }
            }
        }
        return null;
    }

    /**
     * 将 Json String 转化为Map
     *
     * @param content Json String
     * @return Map returnMap
     */
    public static Map<String, Object> readMap(String content) {
        return readMap(content, Object.class);
    }


    /**
     * 读取集合
     *
     * @param content    Json String
     * @param valueClass 值类型
     * @param <V>        泛型
     * @return 集合
     */
    public static <V> Map<String, V> readMap(@Nullable String content, Class<?> valueClass) {
        return readMap(content, String.class, valueClass);
    }

    /**
     * 读取集合
     *
     * @param jsonString json字符串
     * @param keyClass   key类型
     * @param valueClass 值类型
     * @param <K>        泛型
     * @param <V>        泛型
     * @return 集合
     */
    public static <K, V> Map<K, V> readMap(@Nullable String jsonString, Class<?> keyClass, Class<?> valueClass) {
        if (jsonString != null && !jsonString.trim().isEmpty()) {
            try {
                MapType mapType = getObjectMapper().getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
                return getObjectMapper().readValue(jsonString, mapType);
            } catch (Exception e) {
                log.error("JsonUtils -- readMap -- Exception=", e);
            }
        }
        return null;
    }


    /**
     * JSON字符串转对象
     *
     * @param content   JSON字符串
     * @param valueType 类型
     * @param <T>       对象
     * @return 对象
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        if (content != null && !content.trim().isEmpty()) {
            try {
                return getObjectMapper().readValue(content, valueType);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("JsonUtils -- readValue -- Exception=", e);
                }
            }
        }

        return null;
    }

    /**
     * JSON字符串转对象
     *
     * @param content      JSON字符串
     * @param valueTypeRef TypeReference
     * @param <T>          对象
     * @return 对象
     */
    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        if (content != null && !content.trim().isEmpty()) {
            try {
                return getObjectMapper().readValue(content, valueTypeRef);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("JsonUtils -- readValue -- Exception=", e);
                }
            }
        }
        return null;
    }


    /**
     * InputStream文件流转对象
     *
     * @param src       输入流
     * @param valueType 类型
     * @param <T>       对象
     * @return 对象
     */
    public static <T> T readValue(InputStream src, Class<T> valueType) {
        if (src != null) {
            try {
                return getObjectMapper().readValue(src, valueType);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("JsonUtils -- readValue -- Exception=", e);
                }
            }
        }
        return null;
    }

    /**
     * JSON字符串转List
     *
     * @param content JSON字符串
     * @param clazz   类型
     * @param <T>     对象
     * @return 对象
     */
    public static <T> List<T> readArrayValue(String content, Class<T> clazz) {
        if (content != null && !content.trim().isEmpty()) {
            try {
                return getObjectMapper().readValue(content, getObjectMapper().getTypeFactory().constructParametricType(ArrayList.class, clazz));
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("JsonUtils -- readArrayValue -- Exception=", e);
                }
            }
        }
        return null;
    }

    /**
     * 类型转换
     * <p>
     * 示例：
     * String redisKey = ip + ":" + method + ":" + requestURI;
     * Integer count = JsonUtils.convertValue(redisResult, Integer.class);
     *
     * @param fromValue   原始对象
     * @param toValueType 要转换成的对象类型
     * @param <T>         对象
     * @return 转换后的对象
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return getObjectMapper().convertValue(fromValue, toValueType);
    }

    /**
     * 判断传入的字符串是否是json格式的
     *
     * @param content JSON字符串
     * @return true 是，false 不是
     */
    public static boolean isJsonValid(String content) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(content);
            return jsonNode.isObject() || jsonNode.isArray();
        } catch (Exception e) {
            return false;
        }
    }
}
