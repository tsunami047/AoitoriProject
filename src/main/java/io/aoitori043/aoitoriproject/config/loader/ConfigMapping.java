package io.aoitori043.aoitoriproject.config.loader;

import io.aoitori043.aoitoriproject.config.*;
import org.bukkit.configuration.ConfigurationSection;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static io.aoitori043.aoitoriproject.config.InvalidUtil.performNullCheck;
import static io.aoitori043.aoitoriproject.config.impl.MapperInjection.runAnnotatedMethodByField;
import static io.aoitori043.aoitoriproject.config.loader.MapperEvaluation.getValue;
import static io.aoitori043.aoitoriproject.config.loader.YamlMapping.printlnError;

/**
 * @Author: natsumi
 * @CreateTime: 2024-01-17  06:10
 * @Description: ?
 */
public class ConfigMapping {

    static ScriptEngineManager manager = new ScriptEngineManager();
    static ScriptEngine engine = manager.getEngineByName("js");

    private static String executeJavaScriptCode(String jsCode) {
        try {
            return String.valueOf(engine.eval(jsCode));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static void performJS(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExecuteJS.class)) {
                try {
                    try {
                        field.setAccessible(true);
                        String fieldValue = (String) field.get(object);
                        if(fieldValue == null){
                            continue;
                        }
                        field.set(object,String.valueOf(executeJavaScriptCode(fieldValue)));
                    } catch (IllegalAccessException e) {
                        printlnError(object);
                        e.printStackTrace();
                    }
                }catch (Exception e){
                    printlnError(object);
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isInnerClass(Class<?> clazz) {
        String className = clazz.getName();
        return className.contains("$");
    }

    public static void loadFromConfig(Object object,String parentName,ConfigurationSection section) {
        Class<?> clazz = object.getClass();
        if (isInnerClass(clazz) || clazz.isAnnotationPresent(ConfigProperties.class)) {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(NonConfigProperty.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(object, section, field,parentName);
                }else{
                    String propertyName = field.getName();
                    try {
                        getValue(object, section, field, propertyName,parentName);
                        runAnnotatedMethodByField(object,field.getName());
                    } catch (IllegalAccessException e) {
                        printlnError(object);
                        e.printStackTrace();
                    }
                }
            }
        }else {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(object, section, field,parentName);
                }
            }
        }
    }

    private static void findConversion(Object object, ConfigurationSection section, Field field,String parentName) {
        field.setAccessible(true);
        ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
        String[] values = annotation.values();
        try {
            String propertyName = null;
            if(section != null){
                if(values[0].equals("useVariableName")){
                    propertyName = field.getName();
                }else{
                    for (String value : values) {
                        if (section.get(value) != null) {
                            propertyName = value;
                        }
                    }
                    if(propertyName == null){
                        propertyName = field.getName();
                    }
                }
            }else{
                propertyName = field.getName();
            }
            getValue(object, section, field, propertyName,parentName);
            runAnnotatedMethodByField(object,field.getName());
        } catch (IllegalAccessException e) {
            printlnError(object);
            e.printStackTrace();
        }
    }

    public static <T> T createInstance(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    public static boolean isStaticField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers);
    }

}
