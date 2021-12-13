package org.crumbs.json;

import org.crumbs.json.exception.JsonMarshalException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.crumbs.json.JsonToken.*;

public class JsonMarshaler {
    static void buildTree(JsonNode node) throws IllegalAccessException, JsonMarshalException {

        Object nodeValue = node.getValue();
        if(nodeValue == null) {
            node.setType(JsonType.NULL);
            return;
        }
        Class<?> clazz = node.getValue().getClass();

        if (clazz.equals(String.class)) {
            node.setType(JsonType.STRING);
        } else if (clazz.equals(Integer.class) || clazz.equals(Double.class) || clazz.equals(Long.class) ||
                clazz.equals(Float.class) || clazz.equals(Short.class)) {
            node.setType(JsonType.NUMBER);
        } else if (clazz.equals(Boolean.class)) {
            node.setType(JsonType.BOOLEAN);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            node.setType(JsonType.ARRAY);
            Class<?> arrayType = null;
            for (Object element : ((Collection) node.getValue())) {
                if (arrayType == null) {
                    arrayType = element.getClass();
                }
                if (!element.getClass().equals(arrayType)) {
                    throw new JsonMarshalException("Json array types must be from the same class");
                }
                JsonNode newChild = new JsonNode();
                newChild.setValue(element);
                node.getChildren().add(newChild);
                buildTree(newChild);
            }
        }
        // map support
        else if (Map.class.isAssignableFrom(clazz)) {
            node.setType(JsonType.OBJECT);
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) node.getValue()).entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (!key.getClass().equals(String.class)) {
                    throw new JsonMarshalException("Could not build tree out of non string type key: " +
                            key.getClass().getName());
                }
                JsonNode newChild = new JsonNode();
                newChild.setKey((String) key);
                newChild.setValue(value);
                node.getChildren().add(newChild);
                buildTree(newChild);
            }
        } else {
            node.setType(JsonType.OBJECT);
            for (Field field : clazz.getDeclaredFields()) {
                // skip transient fields
                if (!Modifier.isTransient(field.getModifiers())) {
                    JsonNode newChild = new JsonNode();
                    newChild.setKey(field.getName());
                    newChild.setValue(field.get(node.getValue()));
                    node.getChildren().add(newChild);
                    buildTree(newChild);
                }
            }
        }
    }

    static void buildJson(JsonNode node, StringBuilder json) {
        List<JsonNode> children = node.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            JsonNode child = children.get(i);
            Object value = child.getValue();
            if (node.getType().equals(JsonType.OBJECT)) {
                json.append(TOKEN_STRING_QUOTES);
                json.append(child.getKey());
                json.append(TOKEN_STRING_QUOTES);
                json.append(TOKEN_KEY_VAL_SEPARATOR);
            }
            switch (child.getType()) {
                case STRING:
                    json.append(TOKEN_STRING_QUOTES);
                    json.append(value);
                    json.append(TOKEN_STRING_QUOTES);
                    break;
                case NUMBER:
                case BOOLEAN:
                case NULL:
                    json.append(value);
                    break;
                case ARRAY:
                    json.append(TOKEN_ARRAY_START);
                    buildJson(child, json);
                    json.append(TOKEN_ARRAY_END);
                    break;
                case OBJECT:
                    json.append(TOKEN_OBJECT_START);
                    buildJson(child, json);
                    json.append(TOKEN_OBJECT_END);
                    break;
            }
            if (i < size - 1) {
                json.append(TOKEN_ENTRY_SEPARATOR);
            }
        }
    }
}
