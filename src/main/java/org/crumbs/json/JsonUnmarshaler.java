package org.crumbs.json;

import org.crumbs.json.exception.JsonUnmarshalException;

import static org.crumbs.json.JsonToken.*;

public class JsonUnmarshaler {

    private static final int VALUE_MAX_LENGTH = 50;

    static JsonNode buildTree(String input) throws JsonUnmarshalException {
        Iterator iterator = new Iterator(Util.spaceRemover(input.toCharArray()));
        if(iterator.get() == TOKEN_OBJECT_START) {
            return readObject(iterator);
        }
        if(iterator.get() == TOKEN_ARRAY_START) {
            return readArray(iterator);
        }
        throw new JsonUnmarshalException("Unexpected JSON start char: " + iterator.get());
    }

    private static JsonNode readObject(Iterator iterator) throws JsonUnmarshalException {
        JsonNode node = new JsonNode();
        node.setType(JsonType.OBJECT);
        char token = iterator.nextToken();
        while (token != TOKEN_OBJECT_END && token != Character.MAX_VALUE) {
            if(token == TOKEN_STRING_QUOTES) {
                node.getChildren().add(readObjectEntry(iterator));
            }
            token = iterator.get();
            if(token == TOKEN_ENTRY_SEPARATOR) {
                token = iterator.nextToken();
            }
        }
        if(token == Character.MAX_VALUE) {
            throw new JsonUnmarshalException("Unexpected end of input");
        }
        iterator.nextToken();
        return node;
    }

    private static JsonNode readObjectEntry(Iterator iterator) throws JsonUnmarshalException {
        String key = readString(iterator);
        if(iterator.get() != TOKEN_KEY_VAL_SEPARATOR) {
            throw new JsonUnmarshalException("Expected char: " + TOKEN_KEY_VAL_SEPARATOR);
        }
        iterator.nextToken();
        JsonNode node = readJsonValue(iterator);
        node.setKey(key);
        return node;
    }

    private static JsonNode readJsonValue(Iterator iterator) throws JsonUnmarshalException {
        JsonNode node;
        switch (iterator.get()) {
            case TOKEN_OBJECT_START:
                node = readObject(iterator);
                break;
            case TOKEN_STRING_QUOTES:
                node = new JsonNode();
                node.setType(JsonType.STRING);
                node.setValue(readString(iterator));
                break;
            case TOKEN_ARRAY_START:
                node = readArray(iterator);
                break;
            default:
                node = readValue(iterator);
                break;
        }
        return node;
    }

    private static JsonNode readValue(Iterator iterator) throws JsonUnmarshalException {
        JsonNode node = new JsonNode();
        StringBuilder sb = new StringBuilder();

        char token = iterator.get();
        while (token != TOKEN_ARRAY_END && token != TOKEN_OBJECT_END && token != TOKEN_ENTRY_SEPARATOR &&
                token != Character.MAX_VALUE) {
            sb.append(token);
            if(sb.length() > VALUE_MAX_LENGTH) {
                throw new JsonUnmarshalException("Json values cannot be longer than " + VALUE_MAX_LENGTH + " chars");
            }
            token = iterator.nextToken();
        }
        if(token == Character.MAX_VALUE) {
            throw new JsonUnmarshalException("Unexpected end of input");
        }
        String result = sb.toString();
        if(result.isEmpty()) {
            throw new JsonUnmarshalException("Could not unmarshal empty value");
        } else if(result.equals("true")) {
            node.setType(JsonType.BOOLEAN);
            node.setValue(Boolean.TRUE);
        } else if(result.equals("false")) {
            node.setType(JsonType.BOOLEAN);
            node.setValue(Boolean.FALSE);
        } else if(result.equals("null")) {
            node.setType(JsonType.NULL);
        } else if(result.matches("^-*\\d*\\.?\\d*$")) {
            node.setType(JsonType.NUMBER);
            try {
                if(result.contains(".")) {
                    node.setValue(Double.parseDouble(result));
                } else {
                    try {
                        node.setValue(Integer.parseInt(result));
                    } catch (NumberFormatException e) {
                        node.setValue(Long.parseLong(result));
                    }
                }
            } catch (NumberFormatException ex) {
                throw new JsonUnmarshalException("Could not read number value: " + result);
            }
        } else {
            throw new JsonUnmarshalException("Could not read value: " + result);
        }
        return node;
    }

    private static JsonNode readArray(Iterator iterator) throws JsonUnmarshalException {
        JsonNode node = new JsonNode();
        node.setType(JsonType.ARRAY);
        char token = iterator.nextToken();
        while (token != TOKEN_ARRAY_END && token != Character.MAX_VALUE) {
            node.getChildren().add(readJsonValue(iterator));
            token = iterator.get();
            if(token == TOKEN_ENTRY_SEPARATOR) {
                token = iterator.nextToken();
            }
        }
        if(token == Character.MAX_VALUE) {
            throw new JsonUnmarshalException("Unexpected end of input");
        }
        iterator.nextToken();
        return node;
    }

    private static String readString(Iterator iterator) throws JsonUnmarshalException {
        StringBuilder sb = new StringBuilder();
        char token = iterator.nextToken();
        while (token != TOKEN_STRING_QUOTES && token != Character.MAX_VALUE) {
            sb.append(token);
            token = iterator.nextToken();
        }
        if(token == Character.MAX_VALUE) {
            throw new JsonUnmarshalException("Unexpected end of input");
        }
        String result = sb.toString();
        if(result.isEmpty()) {
            throw new JsonUnmarshalException("Json entry cannot have empty key");
        }
        iterator.nextToken();
        return result;
    }

    private static class Iterator {
        int idx;
        private char[] chars;

        public Iterator(char[] chars) {
            this.chars = chars;
        }

        public char nextToken() {
            idx++;
            return get();
        }

        public char get() {
            return idx == chars.length ? Character.MAX_VALUE : chars[idx];
        }
    }
}
