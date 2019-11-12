package io.github.gasparbarancelli;

import org.apache.commons.text.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NativeQueryParameter {

    private String name;

    private Object value;

    public NativeQueryParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    static List<NativeQueryParameter> ofDeclaredMethods(String parentName, Class nqClass, Object object) {
        ArrayList<NativeQueryParameter> parameterList = new ArrayList<NativeQueryParameter>();

        class FieldInfo {

            NativeQueryParam param;

            Class type;

            public FieldInfo(NativeQueryParam param, Class type) {
                this.param = param;
                this.type = type;
            }
        }

        Map<String, FieldInfo> mapField = new HashMap<>();
        for (Field field : nqClass.getDeclaredFields()) {
            NativeQueryParam param = null;
            if (field.isAnnotationPresent(NativeQueryParam.class)) {
                param = field.getAnnotation(NativeQueryParam.class);
            }
            mapField.put(WordUtils.capitalize(field.getName()), new FieldInfo(param, field.getType()));
        }

        for (Method method : nqClass.getDeclaredMethods()) {
            if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                Object value = null;
                try {
                    value = method.invoke(object);
                } catch (Exception ignore) {
                }

                String methodName = method.getName().substring(method.getName().startsWith("get") ? 3 : 2);

                FieldInfo fieldInfo = mapField.get(methodName);
                // todo implement ready method wihout field
                if (fieldInfo != null) {
                    NativeQueryParam queryParam;
                    if (method.isAnnotationPresent(NativeQueryParam.class)) {
                        queryParam = method.getAnnotation(NativeQueryParam.class);
                    } else {
                        queryParam = fieldInfo.param;
                    }

                    if (queryParam != null) {
                        if (queryParam.addChildren()) {
                            String parentNameChildren = parentName + WordUtils.capitalize(queryParam.value());
                            parameterList.addAll(ofDeclaredMethods(parentNameChildren, fieldInfo.type, value));
                        } else {
                            String paramName = parentName + WordUtils.capitalize(queryParam.value());
                            Object paramValue = queryParam.operator().getTransformParam().apply(value);
                            parameterList.add(new NativeQueryParameter(paramName, paramValue));
                        }
                    } else {
                        Object paramValue = NativeQueryOperator.DEFAULT.getTransformParam().apply(value);
                        parameterList.add(new NativeQueryParameter(parentName + methodName, paramValue));
                    }
                }
            }
        }

        return parameterList;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }
}
