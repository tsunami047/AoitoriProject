package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.InjectMappers;
import io.aoitori043.aoitoriproject.config.InjectMapper;
import io.aoitori043.aoitoriproject.config.NotToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static io.aoitori043.aoitoriproject.config.impl.ConfigMapping.isStaticField;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  21:21
 * @Description: ?
 */
public abstract class AutoConfigPrint {

    public void printToConsole() {
        for (Field field : getClass().getFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(InjectMapper.class) != null || field.getAnnotation(InjectMappers.class) != null) {
                try {
                    Object o = field.get(isStaticField(field) ? null : this);
                    printFieldValue(field, o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isToStringOverridden(Class<?> clazz) {
        try {
            Method toStringMethod = clazz.getDeclaredMethod("toString");
            toStringMethod.setAccessible(true);
            return toStringMethod.getDeclaringClass() == clazz;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static String toString(Object obj) {
        //HashSet存在hash冲突问题
        return toString(0, obj, new HashSet<>());
    }

    private void printFieldValue(Field parentField, Object o) {
        StringBuilder sb = new StringBuilder().append(System.lineSeparator());
        try {
            String content = toString(o);
            if (!content.equalsIgnoreCase("trash")) {
                sb.append(parentField.getName()).append("=").append(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(sb);
    }


    private static String toString(int deep, Object obj, HashSet<Integer> visited) {
        try {
            if (obj == null) {
                return "null";
            }
            if (visited.contains(System.identityHashCode(obj))) {
                return "reloop";
            }
            visited.add(System.identityHashCode(obj));
            Class<?> clazz = obj.getClass();
            StringBuilder sb = new StringBuilder();
            if (obj instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) obj;
                sb.append("[");
                if (collection.size() > 1) {
                    sb.append(System.lineSeparator());
                }
                deep++;
                for (Object element : collection) {
                    if (collection.size() > 1) {
                        for (int i = 0; i < deep; i++) {
                            sb.append("  ");
                        }
                    }
                    sb.append(toString(deep, element, visited)).append(", ").append(System.lineSeparator());
                }
                if (!collection.isEmpty()) {
                    sb.delete(sb.length() - 4, sb.length());
                }
                sb.append("]");
                return sb.toString();
            } else if (obj instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) obj;
                sb.append("{");
                if (map.size() > 1) {
                    sb.append(System.lineSeparator());
                }
                deep++;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (map.size() > 1) {
                        for (int i = 0; i < deep; i++) {
                            sb.append("  ");
                        }
                    }
                    sb.append(entry.getKey()).append("=").append(toString(deep, entry.getValue(), visited)).append(", ").append(System.lineSeparator());
                }
                if (!map.isEmpty()) {
                    sb.delete(sb.length() - 4, sb.length());
                }
                sb.append("}");
                return sb.toString();
            }
            if (clazz.isPrimitive() || clazz == String.class || isToStringOverridden(obj.getClass())) {
                return obj.toString();
            }
            if (!clazz.getName().startsWith("java.util") && (clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.") || clazz.getName().startsWith("sun."))) {
                return "trash";
            }
            sb.append("<");
            deep++;
            Field[] fields = clazz.getDeclaredFields();
            if (fields.length > 1) {
                sb.append(System.lineSeparator());
            }
            for (Field field : fields) {
                field.setAccessible(true);
                if(field.isAnnotationPresent(NotToString.class)){
                    continue;
                }
                String fieldName = field.getName();
                Object value;
                try {
                    value = field.get(isStaticField(field) ? null : obj);
                } catch (IllegalAccessException e) {
                    value = "N/A";
                }
                if (fieldName.equals("parent")) {
                    value = "nonnull";
                } else if (value != null && !field.getType().isPrimitive() && !field.getType().equals(String.class) && !Enum.class.isAssignableFrom(field.getType())) {
                    value = toString(deep, value, visited);
                }
                for (int i = 0; i < deep; i++) {
                    sb.append("  ");
                }
                sb.append(fieldName).append("=").append(value).append(", ");
                if (sb.charAt(sb.length() - 1) == ' ') {
                    sb.delete(sb.length() - 2, sb.length());
                }
                sb.append(System.lineSeparator());
            }
            sb.append(">");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }
}
