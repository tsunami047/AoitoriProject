package io.aoitori043.aoitoriproject.database.orm.impl;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.orm.sign.AggregateRoot;
import io.aoitori043.aoitoriproject.database.orm.sign.Key;
import io.aoitori043.aoitoriproject.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  21:46
 * @Description: ?
 */
public class CacheImplUtil {

    public static final String DISCRETE_SIGN = "%";
    public static HashMap<Class, List<String>> realKeyFieldMap = new HashMap<>();
    public static HashMap<String, String> map = new HashMap<>();
    public static List<String> list = new ArrayList<>();
    public static EmptyObject emptyObject = new EmptyObject();

    static {
        list.add("null");
        map.put("null", "null");
    }

    public static String getAggregateRootKey(String tableName, String value) {
        return "$" + tableName + ":" + value;
    }

    public static <T> boolean addNotNullElement(List<T> list, Object element) {
        if (element != null && element != emptyObject) {
            if(element instanceof List){
                list.addAll((List) element);
            }else {
                list.add((T) element);
            }
            return true;
        }
        return false;
    }

    @NotNull
    public static <T> String getDiscreteKey(Class<T> clazz, String tableName, T instance) {
        StringBuilder keys = new StringBuilder(DISCRETE_SIGN + tableName + ":");
        FieldAccess fieldAccess = FieldAccess.get(clazz);
        List<String> list = realKeyFieldMap.get(clazz);
        int num = 0;
        if (list != null) {
            for (String fieldName : list) {
                Object value = fieldAccess.get(instance, fieldName);
                if (value != null) {
                    if (num != 0) {
                        keys.append("-");
                    }
                    keys.append(value);
                    num++;
                }
            }
            return keys.toString();
        }
        list = new ArrayList<>();
        for (Field field : fieldAccess.getFields()) {
            if (field.isAnnotationPresent(Key.class) && !field.isAnnotationPresent(AggregateRoot.class)) {
                Object value = fieldAccess.get(instance, field.getName());
                list.add(field.getName());
                if (value != null) {
                    if (num != 0) {
                        keys.append("-");
                    }
                    keys.append(value);
                    num++;
                }
            }
        }
        realKeyFieldMap.put(clazz, list);
        return keys.toString();
    }


    public static String getAggregateRootKey(String tableName, FieldAccess fieldAccess, Object object) {
        try {
            Object id = fieldAccess.get(object, "id");
            if (id == null) {
                return null;
            }
            return getAggregateRootKey(tableName, String.valueOf(id));
        } catch (Exception e) {
            return null;
        }
    }

    @FunctionalInterface
    public interface Lock {
        void run();
    }

    public interface SubmitLock<T> {
        T run();
    }


    public static HashMap<Class,String> aggregateRoot = new HashMap<>();
    public static HashMap<Class,List<String>> keysMap = new HashMap<>();

    public static String getQueryAggregateRoot(Object o){
        Class<?> aClass = o.getClass();
        FieldAccess fieldAccess = FieldAccess.get(aClass);
        String root = aggregateRoot.get(aClass);
        if(root!=null){
            Object o1 = fieldAccess.get(o, root);
            if(o1==null){
                return null;
            }
            return o1.toString();
        }
        for (Field field : fieldAccess.getFields()) {
            if(!field.isAnnotationPresent(AggregateRoot.class)){
                continue;
            }
            aggregateRoot.put(aClass,field.getName());
            Object o1 = fieldAccess.get(o, field.getName());
            if(o1 == null){
                return null;
            }
            return o1.toString();
        }
        return null;
    }


    public static List<String> getClassKeys(Class clazz) {
        List<String> kvList = keysMap.get(clazz);
        if(kvList != null){
            return kvList;
        }
        List<String> list = new ArrayList<>();
        FieldAccess fieldAccess = FieldAccess.get(clazz);
        for (Field field : fieldAccess.getFields()) {
            if (field.isAnnotationPresent(AggregateRoot.class) || !field.isAnnotationPresent(Key.class)) {
                continue;
            }
            list.add(field.getName());
        }
//        Collections.sort(list);
        keysMap.put(clazz,list);
        return list;
    }

    private static String discreteRootFormat(String tableName, String rootIndex,String value) {
        return DISCRETE_SIGN + tableName + ":" +rootIndex+"_"+ value;
    }


    //获得键的所有组合方式，用于入缓
    public static List<String> generateCombinations(String tableName,List<Pair<Integer,String>> map) {
        List<String> combinations = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {
            StringBuilder sb = new StringBuilder();
            StringBuilder index = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                Pair<Integer, String> pair = map.get(j);
                sb.append(pair.getValue());
                index.append(pair.getKey());
                if (j < i) {
                    sb.append("-");
                }
            }
            combinations.add(discreteRootFormat(tableName,index.toString(),sb.toString()));
        }
        return combinations;
    }

    public static class EmptyObject {

    }
}
