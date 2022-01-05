package org.crumbs.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReference<T> {

    public Class<?> getTypeArgument() {
        return fromType(((ParameterizedType)((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]).getActualTypeArguments()[0]);
    }

    public Class<?> getGenericType() {
        return fromType(((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    private Class<?> fromType(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        return (Class<T>) type;
    }
}
