package org.crumbs.json.exception;

public class JsonUnmarshalException extends Exception {
    public JsonUnmarshalException(String message) {
        super(message);
    }

    public JsonUnmarshalException(String message, Throwable cause) {
        super(message, cause);
    }
}
