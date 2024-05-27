package io.aoitori043.aoitoriproject.config;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-01-17  06:00
 * @Description: ?
 */
public class InvalidUtil {
    public static void requireNonNull(Object obj) {
        if (obj == null) {
            throw new NullPointerException("这个值不可以是空的");
        }
    }

    public static void performNullCheck(Object obj) throws IllegalAccessException {
        Class<?> aClass = obj.getClass();
        NotNullProperties notnull = aClass.getAnnotation(NotNullProperties.class);
        if (notnull != null) {
            for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(DefaultValue.class)) {
                    DefaultValue annotation = field.getAnnotation(DefaultValue.class);
                    if (field.getType() == String.class) {
                        field.set(obj, annotation.stringValue());
                    } else if (field.getType() == int.class) {
                        field.set(obj, annotation.intValue());
                    } else if (field.getType() == double.class) {
                        field.set(obj, annotation.doubleValue());
                    } else if (field.getType() == boolean.class) {
                        field.set(obj, annotation.booleanValue());
                    } else if (field.getType() == float.class) {
                        field.set(obj, annotation.floatValue());
                    } else if (field.getType() == List.class) {
                        field.set(obj, Arrays.asList(annotation.listValue()));
                    }
                }
                if (field.isAnnotationPresent(NullableProperty.class)) {
                    continue;
                }
                field.setAccessible(true);
                ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
                if (field.get(obj) == null) {
                    if(annotation!=null && !annotation.values()[0].equals("useVariableName")){
                        throw new IllegalStateException("对象信息： " + obj + " 无效的值：" + annotation.values()[0] + " 不可以为空！");
                    }else {
                        throw new IllegalStateException("对象信息： " + obj + " 无效的值：" + field.getName() + " 不可以为空！");
                    }
                }

            }
        }
        for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(DefaultValue.class)) {
                DefaultValue annotation = field.getAnnotation(DefaultValue.class);
                if (field.getType() == String.class) {
                    field.set(obj, annotation.stringValue());
                } else if (field.getType() == int.class) {
                    field.set(obj, annotation.intValue());
                } else if (field.getType() == double.class) {
                    field.set(obj, annotation.doubleValue());
                } else if (field.getType() == boolean.class) {
                    field.set(obj, annotation.booleanValue());
                } else if (field.getType() == float.class) {
                    field.set(obj, annotation.floatValue());
                } else if (field.getType() == List.class) {
                    field.set(obj, Arrays.asList(annotation.listValue()));
                }
            }
            if (field.isAnnotationPresent(NotNullProperty.class)) {
                field.setAccessible(true);
                if (field.get(obj) == null) {
                    throw new IllegalStateException("对象信息： " + obj + " 无效的值：" + field.getName() + " 不可以为空！");
                }
            }
        }
    }

}
