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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Field[] fields = ConfigMapping.getAllFields(object.getClass());
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

    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public static void loadFromConfig(Object parent,Object beInjectedObject,String parentName,ConfigurationSection section) {
        Class<?> clazz = beInjectedObject.getClass();
        if (isInnerClass(clazz) || clazz.isAnnotationPresent(ConfigProperties.class)) {
            ConfigProperties annotation = clazz.getAnnotation(ConfigProperties.class);
            String append = annotation!=null?annotation.appendPath():"";
            for (Field field : ConfigMapping.getAllFields(beInjectedObject.getClass())) {
                if (field.isAnnotationPresent(NonConfigProperty.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(parent,beInjectedObject, section, field,parentName);
                }else{
                    String propertyName = field.getName();
                    try {
                        getValue(parent,beInjectedObject, section, field, append+propertyName,parentName);
                    } catch (IllegalAccessException e) {
                        printlnError(beInjectedObject);
                        e.printStackTrace();
                    }
                }
            }
        }else {
            for (Field field : ConfigMapping.getAllFields(beInjectedObject.getClass())) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(parent,beInjectedObject, section, field,parentName);
                }
            }
        }
        
    }

    private static void findConversion(Object parent,Object object, ConfigurationSection section, Field field,String parentName) {
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
            getValue(parent,object, section, field, propertyName,parentName);
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
