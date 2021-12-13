package org.crumbs.json;

import java.util.ArrayList;
import java.util.List;

public class JsonNode {
    private String key;
    private JsonType type;
    private Object value;
    private List<JsonNode> children = new ArrayList<>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JsonType getType() {
        return type;
    }

    public void setType(JsonType type) {
        this.type = type;
    }

    public List<JsonNode> getChildren() {
        return children;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
