package org.crumbs.json;

import org.crumbs.json.exception.JsonMarshalException;
import org.crumbs.json.exception.JsonUnmarshalException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.crumbs.json.JsonToken.*;

public class JsonMapper {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private Charset charset = DEFAULT_CHARSET;

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String marshal(Object input) throws JsonMarshalException, IllegalAccessException {

        JsonNode root = new JsonNode();
        root.setValue(input);
        JsonMarshaler.buildTree(root);
        if (root.getType().equals(JsonType.OBJECT)) {
            StringBuilder sb = new StringBuilder();
            sb.append(TOKEN_OBJECT_START);
            JsonMarshaler.buildJson(root, sb);
            sb.append(TOKEN_OBJECT_END);
            return sb.toString();
        } else if (root.getType().equals(JsonType.ARRAY)) {
            StringBuilder sb = new StringBuilder();
            sb.append(TOKEN_ARRAY_START);
            JsonMarshaler.buildJson(root, sb);
            sb.append(TOKEN_ARRAY_END);
            return sb.toString();
        } else {
            throw new JsonMarshalException("Can not serialize to json from type: " + root.getClass().getName());
        }
    }


    public <T> T unmarshal(byte[] input, Class<T> clazz) throws JsonUnmarshalException {
        String json = new String(input, charset);
        JsonNode root = JsonUnmarshaler.buildTree(json);
        return (T) mapTo(root, clazz, null);
    }

    private Object mapTo(JsonNode node, Class<?> clazz, Class<?> genericType) throws JsonUnmarshalException {
        switch (node.getType()) {
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case NULL:
                return node.getValue();
            case ARRAY:
                List list = new ArrayList(node.getChildren().size());
                for(JsonNode child: node.getChildren()) {
                    list.add(mapTo(child, genericType == null ? Map.class : genericType, null));
                }
                return list;
            case OBJECT:
                if(Map.class.isAssignableFrom(clazz)) {
                    Map<String, Object> map = new LinkedHashMap<>(node.getChildren().size());
                    for(JsonNode child: node.getChildren()) {
                        map.put(child.getKey(), mapTo(child, child.getValue() == null ? Map.class :
                                child.getValue().getClass(), null));
                    }
                    return map;
                } else {
                    return mapObject(node, clazz);
                }
            default:
                throw new JsonUnmarshalException("Unknown JsonType: " + node.getType());
        }
    }

    private Object mapObject(JsonNode node, Class<?> clazz) throws JsonUnmarshalException {
        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                String name = field.getName().toLowerCase();
                for (JsonNode child : node.getChildren()) {
                    if (child.getKey().toLowerCase().equals(name)) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        switch (child.getType()) {
                            case BOOLEAN:
                            case NULL:
                            case STRING:
                            case NUMBER:
                                field.set(instance, child.getValue());
                                break;
                            case ARRAY:
                                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                                Class<?> genericType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                                field.set(instance, mapTo(child, List.class, genericType));
                                break;
                            case OBJECT:
                                field.set(instance, mapTo(child, type, null));
                        }
                        field.setAccessible(false);
                    }
                }
            }
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new JsonUnmarshalException("Reflection exception", e);
        }
        return instance;
    }
}
