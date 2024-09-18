package cn.fyzzz.spider.flow.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * @author fyzzz
 * 2024/9/18 19:18
 */
public class JacksonUtil {


    private JacksonUtil() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JacksonUtil.class);

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void setDateFormat(DateFormat dateFormat) {
        OBJECT_MAPPER.setDateFormat(dateFormat);
    }

    public static void setTimeZone(TimeZone timeZone) {
        OBJECT_MAPPER.setTimeZone(timeZone);
    }

    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 对象 转 json
     *
     * @param object 对象
     * @return json字符串
     */
    public static String objectToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("对象转换json异常{}", e.getMessage());
        }
        return null;
    }

    /**
     * 对象 转 jsonNode
     *
     * @param object 对象
     * @return jsonNode对象
     */
    public static JsonNode objectToJsonNode(Object object) {
        return stringToNode(objectToJson(object));
    }

    /**
     * json 转 对象
     *
     * @param info  字符串信息
     * @param clazz 类
     * @return 对象
     */
    public static <T> T jsonToObject(String info, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(info, clazz);
        } catch (JsonProcessingException e) {
            logger.error("json转换对象异常{}", e.getMessage());
        }
        return null;
    }


    /**
     * json 转 集合
     *
     * @param info  json
     * @return 集合
     */
    public static <T> List<T> jsonToList(String info, Class<T> clazz) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
            return OBJECT_MAPPER.readValue(info, javaType);
        } catch (JsonProcessingException e) {
            logger.error("json转换集合异常", e);
        }
        return Collections.emptyList();
    }

    /**
     * json 转 集合
     *
     * @param info  json
     * @return 集合
     */
    public static <T> T jsonToParameterizedType(String info, Class<T> parametrized, Class<?>... parameterClasses) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(parametrized, parameterClasses);
            return OBJECT_MAPPER.readValue(info, javaType);
        } catch (JsonProcessingException e) {
            logger.error("json转换集合异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * json获取jsonNode
     *
     * @param info json
     * @return jsonNode
     */
    public static JsonNode stringToNode(String info) {
        try {
            return OBJECT_MAPPER.readTree(info);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * json获取jsonNode
     *
     * @param info json
     * @return jsonNode
     */
    public static ArrayNode stringToArray(String info) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(info);
            if (jsonNode instanceof ArrayNode) {
                return (ArrayNode) jsonNode;
            }
        } catch (JsonProcessingException e) {
            logger.error("json获取jsonNode异常{}", e.getMessage());
        }
        return null;
    }

    /**
     * jsonNode 转 对象
     *
     * @param jsonNode jsonNode
     * @param clazz    类
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T jsonNodeToObject(JsonNode jsonNode, Class<T> clazz) {
        try {
            if (JsonNode.class.isAssignableFrom(clazz)) {
                // 想要获取JsonNode,ObjectNode,ArrayNode时
                return (T) jsonNode;
            }
            if (clazz.isAssignableFrom(String.class)) {
                return (T) jsonNode.toString();
            }
            return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(jsonNode), clazz);
        } catch (JsonProcessingException e) {
            logger.error("jsonNode转对象异常", e);
        }
        return null;
    }

    /**
     * jsonNode 转 集合
     *
     * @param jsonNode jsonNode
     * @return 集合
     */
    public static <T> List<T> jsonNodeToList(JsonNode jsonNode) {
        try {
            return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(jsonNode), new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public static boolean validateJson(String json) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node != null;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

}