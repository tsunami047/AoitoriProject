package io.aoitori043.aoitoriproject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-04  22:42
 * @Description: 反射
 */

public class ReflectionUtil {

    public static Object invokeSuperMethod(Object object, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = object.getClass();
        Method method = null;
        while (clazz != null) {
            try {
                method = clazz.getDeclaredMethod(methodName);
                break;
            } catch (NoSuchMethodException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        if (method == null) {
            throw new NoSuchMethodException("Method not found: " + methodName);
        }
        method.setAccessible(true);
        return method.invoke(object);
    }

    /**
     * @date 2023/6/8 13:14
     * @param object
     * @param methodName
     * @param parameterTypes
     * @param args
     * @return Object
     * @description 执行父类方法
     */
    public static Object invokeSuperMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = object.getClass();
        Method method = null;
        while (clazz != null) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                break;
            } catch (NoSuchMethodException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        if (method == null) {
            throw new NoSuchMethodException("Method not found: " + methodName);
        }
        method.setAccessible(true);
        return method.invoke(object, args);
    }

    /**
     * @date 2023/6/5 22:30
     * @param obj
     * @param methodName

     * @description 执行一个没有参数的对象私有方法
     */
    public static Object invokePrivateMethod(Object obj, String methodName) throws Exception {
        Class<?> clazz = obj.getClass();
        Method privateMethod = clazz.getDeclaredMethod(methodName);
        privateMethod.setAccessible(true);
        return privateMethod.invoke(obj);
    }

    /**
     * @date 2023/6/5 15:29
     * @param obj

     * @description 判断一个对象内的字段是否为空指针
     */
    public static void checkNullFields(Object obj, Consumer<String> reportConsumer) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) {
                    String fieldName = field.getName();
                    if (fieldName.contains("intensify_skill")) continue;
                    reportConsumer.accept("Error: Field '" + fieldName + "' is null.");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @date 2023/6/5 15:29
     * @param obj

     * @description 判断一个对象内的字段是否为空指针
     */
    public static void checkNullFieldsFindFirst(Object obj, Consumer<String> reportConsumer) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String fieldName = field.getName();
                if(fieldName.contains("money")){
                    continue;
                }
                if(value instanceof Double && ((double)value)==0){
                    reportConsumer.accept("可能是 '" + fieldName + "',请根据英文意思找对应配置项");
                    return;
                }
                if(value instanceof Integer && ((int)value)==0){
                    reportConsumer.accept("可能是 '" + fieldName + "',请根据英文意思找对应配置项");
                    return;
                }
                if (value == null) {
                    reportConsumer.accept("可能是 '" + fieldName + "',请根据英文意思找对应配置项");
                    return;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @date 2023/6/5 13:24
     * @param clazz
     * @param fieldName
     * @return Object
     * @description 取private修饰静态字段值
     */
    public static Object getPrivateStaticFieldValue(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @date 2023/6/8 12:44
     * @param object
     * @param fieldName
     * @param newValue

     * @description 修改子类或者父类属性
     */
    public static void setPrivateAndSuperField(Object object, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field field = null;

        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }

        if (field == null) {
            throw new NoSuchFieldException("Field not found: " + fieldName);
        }

        field.setAccessible(true);
        field.set(object, newValue);
    }

    /**
     * @date 2023/6/8 0:41
     * @param object
     * @param fieldName
     * @return Object
     * @description 获取对象的私有成员
     */
    public static Object getPrivateAndSuperField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field field = null;
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException ignored) {
                // 当前类没有该字段，继续在父类中查找
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException("Field not found: " + fieldName);
        }
        field.setAccessible(true);
        return field.get(object);
    }

}
