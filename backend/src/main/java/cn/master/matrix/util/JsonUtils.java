package cn.master.matrix.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mybatisflex.processor.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
@Slf4j
public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    private static final TypeFactory TYPE_FACTORY = OBJECT_MAPPER.getTypeFactory();

    public static String toJsonString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toJsonBytes(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            log.error("json parse err,json:{}", json, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(bytes, valueType);
        } catch (IOException e) {
            log.error("json parse err,json:{}", bytes, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String json, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            log.error("json parse err,json:{}", json, e);
            throw new RuntimeException(e);
        }
    }

    public static Object parseObject(String content) {
        return parseObject(content, Object.class);
    }

    public static <T> List<T> parseArray(String content, Class<T> valueType) {
        CollectionType javaType = TYPE_FACTORY.constructCollectionType(List.class, valueType);
        try {
            return OBJECT_MAPPER.readValue(content, javaType);
        } catch (IOException e) {
            log.error("json parse err,json:{}", content, e);
            throw new RuntimeException(e);
        }
    }

    public static List parseArray(String content) {
        return parseArray(content, Object.class);
    }

    public static <T> List<T> parseArray(String content, TypeReference<T> valueType) {
        try {
            JavaType subType = TYPE_FACTORY.constructType(valueType);
            CollectionType javaType = TYPE_FACTORY.constructCollectionType(List.class, subType);
            return OBJECT_MAPPER.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(String text) {
        try {
            return OBJECT_MAPPER.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(byte[] text) {
        try {
            return OBJECT_MAPPER.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static Map parseMap(String jsonObject) {
        try {
            return OBJECT_MAPPER.readValue(jsonObject, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNumber(CharSequence str) {
        if (StringUtils.isBlank(str)) {
            return false;
        } else {
            char[] chars = str.toString().toCharArray();
            int sz = chars.length;
            boolean hasExp = false;
            boolean hasDecPoint = false;
            boolean allowSigns = false;
            boolean foundDigit = false;
            int start = chars[0] != '-' && chars[0] != '+' ? 0 : 1;
            int i;
            if (sz > start + 1 && chars[start] == '0' && (chars[start + 1] == 'x' || chars[start + 1] == 'X')) {
                i = start + 2;
                if (i == sz) {
                    return false;
                } else {
                    while (i < chars.length) {
                        if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                            return false;
                        }

                        ++i;
                    }

                    return true;
                }
            } else {
                --sz;

                for (i = start; i < sz || i < sz + 1 && allowSigns && !foundDigit; ++i) {
                    if (chars[i] >= '0' && chars[i] <= '9') {
                        foundDigit = true;
                        allowSigns = false;
                    } else if (chars[i] == '.') {
                        if (hasDecPoint || hasExp) {
                            return false;
                        }

                        hasDecPoint = true;
                    } else if (chars[i] != 'e' && chars[i] != 'E') {
                        if (chars[i] != '+' && chars[i] != '-') {
                            return false;
                        }

                        if (!allowSigns) {
                            return false;
                        }

                        allowSigns = false;
                        foundDigit = false;
                    } else {
                        if (hasExp) {
                            return false;
                        }

                        if (!foundDigit) {
                            return false;
                        }

                        hasExp = true;
                        allowSigns = true;
                    }
                }

                if (i < chars.length) {
                    if (chars[i] >= '0' && chars[i] <= '9') {
                        return true;
                    } else if (chars[i] != 'e' && chars[i] != 'E') {
                        if (chars[i] == '.') {
                            return !hasDecPoint && !hasExp && foundDigit;
                        } else if (allowSigns || chars[i] != 'd' && chars[i] != 'D' && chars[i] != 'f' && chars[i] != 'F') {
                            if (chars[i] != 'l' && chars[i] != 'L') {
                                return false;
                            } else {
                                return foundDigit && !hasExp;
                            }
                        } else {
                            return foundDigit;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return !allowSigns && foundDigit;
                }
            }
        }
    }
}
