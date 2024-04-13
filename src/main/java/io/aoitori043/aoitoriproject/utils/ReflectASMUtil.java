package io.aoitori043.aoitoriproject.utils;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import io.aoitori043.aoitoriproject.database.orm.sign.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  18:57
 * @Description: ?
 */
public class ReflectASMUtil {

    @Entity
    public static class Test{
    }

    public static void main(String[] args) {
        Test test = new Test();


    }

    public static String getSimpleClassName(Class<?> type) {
        return type.getSimpleName();
    }

    public static Field[] getFields(Class<?> type) {
        FieldAccess fieldAccess = FieldAccess.get(type);
        return fieldAccess.getFields();
    }

    public static String[] getFieldNames(Class<?> type) {
        FieldAccess fieldAccess = FieldAccess.get(type);
        return fieldAccess.getFieldNames();
    }

    public static Object getField(Object object, String fieldName) {
        FieldAccess fieldAccess = FieldAccess.get(object.getClass());
        return fieldAccess.get(object, fieldName);
    }
    public static void setField(Object object, String fieldName, Object value) {
        FieldAccess fieldAccess = FieldAccess.get(object.getClass());
        fieldAccess.set(object, fieldName, value);
    }

    public static Object invokeMethod(Object object, String methodName, Object... args) {
        MethodAccess methodAccess = MethodAccess.get(object.getClass());
        return methodAccess.invoke(object, methodName, args);
    }

    @NotNull
    public static <T> T createInstance(Class<T> type) {
        Object instance;
        try {
            instance = ConstructorAccess.get(type).newInstance();
        } catch (Exception e) {
            System.out.println("无法创建对象："+e.getMessage());
            return null;
        }
        return (T) instance;
    }
}
