package io.aoitori043.aoitoriproject.database.orm.impl;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.CanaryClientImpl;
import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.EmbeddedHashMap;
import io.aoitori043.aoitoriproject.database.orm.semaphore.LockUtil;
import io.aoitori043.aoitoriproject.database.orm.sign.ManyToMany;
import io.aoitori043.aoitoriproject.database.orm.sign.OneToMany;
import io.aoitori043.aoitoriproject.database.orm.sign.OneToOne;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

import static io.aoitori043.aoitoriproject.utils.ReflectASMUtil.createInstance;

@AllArgsConstructor
public abstract class CacheImpl {

    public SQLClient sqlClient;

    public enum UpdateType {
        NOT_COPY_NULL, COPY_NULL
    }

    public abstract <T> boolean update(T updateEntity, T anchorEntity, CacheImpl.UpdateType updateType);
    public abstract <T> boolean delete(T whereEntity);
    public <T> boolean apply(T entity){
        return true;
    }
    public abstract <T> boolean insert(T entity);
    public abstract <T> List<T> find(T whereEntity);
    public <T> List<T> findAll(Class<T> clazz){
        return null;
    }


    public abstract boolean hasApplyOverride();

    public <T> void initialEmbeddedObject(SQLClient.EntityAttributes entityAttribute,T entity,long id){
        for (String fieldName : entityAttribute.getEmbeddedMapFieldProperties().keySet()) {
            EmbeddedHashMap<?, ?> foreignMap = entityAttribute.getForeignMap(entity, fieldName);
            foreignMap.completeSuperClassInsert(id);
        }
    }

    /*
    级联删除关联外键记录
     */
    public <T> void cascadingDelete(SQLClient.EntityAttributes entityAttribute,T entity){
        HashMap<String, SQLClient.EntityAttributes.ForeignProperty> embeddedMapFieldProperties = entityAttribute.getEmbeddedMapFieldProperties();
        for (Map.Entry<String, SQLClient.EntityAttributes.ForeignProperty> entry : embeddedMapFieldProperties.entrySet()) {
            String fieldName = entry.getKey();
            Object map = entityAttribute.getFieldAccess().get(entity, fieldName);
            if(map == null){
                continue;
            }
            EmbeddedHashMap<?,?> embeddedHashMap = (EmbeddedHashMap) map;
            Iterator<? extends Map.Entry<?, ?>> iterator = embeddedHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> next = iterator.next();
                CanaryClientImpl.sqlClient.delete(next.getValue());
            }
        }
    }


    public <T> void cachingRedis(SQLClient.EntityAttributes entityAttribute, String loopAggregateRoot, T tEntity) {
        HashMap<String, String> serializedObject = serializeObject(tEntity);
        this.sqlClient.redisCache.putMap(loopAggregateRoot, serializedObject);
        List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(tEntity);
        for (String insertDiscreteRoot : insertDiscreteRoots) {
            this.sqlClient.redisCache.pushUnduplicateList(insertDiscreteRoot, loopAggregateRoot);
        }
    }

    public void cachingCaffeine(SQLClient.EntityAttributes entityAttribute, String aggregateRoot, Object entity) {
//        this.injectForeignEntities(entity);
        List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
        for (String insertDiscreteRoot : insertDiscreteRoots) {
            caffeineAddDiscreteRoot(insertDiscreteRoot, aggregateRoot);
        }
        this.sqlClient.caffeineCache.put(aggregateRoot, entity);

    }


    public <T> void caffeineAddDiscreteRoot(String discreteRoot, String aggregateRoot) {
        Object o1 = this.sqlClient.caffeineCache.get(discreteRoot);
        if (o1 != null) {
            List list = (List) o1;
            if (!list.contains(aggregateRoot)) {
                list.add(aggregateRoot);
            }
        } else {
            this.sqlClient.caffeineCache.put(discreteRoot, new ArrayList<>(Collections.singletonList(aggregateRoot)));
        }
    }

    public <T> T getEntityFromRedis(SQLClient.EntityAttributes entityAttribute, @NotNull String aggregateRoot) {
        return LockUtil.syncLockSubmit(aggregateRoot, () -> {
            Map<String, String> map = this.sqlClient.redisCache.getMap(aggregateRoot);
            if (map.isEmpty()) {
                return null;
            }
            T tEntity = (T) injectData(entityAttribute, map);
            return tEntity;
        });
    }

    public <T> void updateEntity(SQLClient.EntityAttributes entityAttribute, T saveEntity, T copyEntity, UpdateType updateType) {
        FieldAccess fieldAccess = entityAttribute.getFieldAccess();
        for (String fieldName : entityAttribute.getUpdateFields()) {
            Object value = fieldAccess.get(copyEntity, fieldName);
            if (value == null && updateType == UpdateType.NOT_COPY_NULL) {
                continue;
            }
            fieldAccess.set(saveEntity, fieldName, value);
        }
        for (String fieldName : entityAttribute.getEmbeddedMapFieldProperties().keySet()) {
            Object value = fieldAccess.get(copyEntity, fieldName);
            if (value == null && updateType == UpdateType.NOT_COPY_NULL) {
                continue;
            }
            fieldAccess.set(saveEntity, fieldName, value);
        }
    }


    public HashMap<String, String> serializeObject(Object o) {
        HashMap<String, String> tempMap = new HashMap<>();
        FieldAccess fieldAccess = FieldAccess.get(o.getClass());
        SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(o.getClass());
        for (Field field : fieldAccess.getFields()) {
            String fieldName = field.getName();
            Object value = fieldAccess.get(o, fieldName);
            if(entityAttribute.getEmbeddedMapFieldProperties().containsKey(fieldName)){
                continue;
            }
            if (value == null) {
//                tempMap.put(fieldName, "null");
                continue;
            }
            if (field.isAnnotationPresent(ManyToMany.class) ||
                    field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(OneToOne.class)) {
                continue;
            }
            if (value instanceof Date) {
                tempMap.put(fieldName, String.valueOf(((Date) value).getTime()));
            } else {
                tempMap.put(fieldName, value.toString());
            }

        }
        return tempMap;
    }

    public <T> T injectData(SQLClient.EntityAttributes entityAttribute, Map<String, String> map) {
        T instance = (T) createInstance(entityAttribute.getClazz());
        FieldAccess fieldAccess = entityAttribute.getFieldAccess();
        for (Field field : fieldAccess.getFields()) {
            String name = field.getName();
            String value = map.get(name);
            if(entityAttribute.getEmbeddedMapFieldProperties().containsKey(name)){
                continue;
            }
            if (value == null || value.equals("null")) {
                continue;
            }
            Object o = fieldTypeCast(field.getType(), value);
            if (o == null) {
                continue;
            }
            fieldAccess.set(instance, name, o);
        }
        return instance;
    }

    /*
    更新外部实体
     */
    public void updateForeignObject(Object o){
        SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(o.getClass());
        for (Map.Entry<String, SQLClient.EntityAttributes.ForeignProperty> entry : entityAttribute.getEmbeddedMapFieldProperties().entrySet()) {
            String key = entry.getKey();
//            SQLClient.EntityAttributes.ForeignProperty property = entry.getValue();
            Object fieldValue = entityAttribute.getFieldAccess().get(o, key);
            if(fieldValue==null){

                //删除关联数据？
            }else{
                if(fieldValue instanceof Map){
                    Map<?, ?> map = (Map<?, ?>) fieldValue;
                    Collection<?> values = map.values();
                    for (Object value : values) {
                        this.sqlClient.apply(value);
                    }
                }else{
                    System.out.println("外实体结构定义错误");
                }
            }
        }
    }


    public <T> void injectForeignEntities(T instance) {
        SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(instance.getClass());
        HashMap<String, SQLClient.EntityAttributes.ForeignProperty> foreignFieldNames = entityAttribute.getEmbeddedMapFieldProperties();
        long databaseId = entityAttribute.getDatabaseId(instance);
        if(foreignFieldNames.isEmpty()){
            return;
        }
        for (Map.Entry<String, SQLClient.EntityAttributes.ForeignProperty> entry : foreignFieldNames.entrySet()) {
            String fieldName = entry.getKey();
            SQLClient.EntityAttributes.ForeignProperty property = entry.getValue();
            T foreignInstance = (T) ReflectASMUtil.createInstance(property.getValueType());
            String foreignFieldName = property.getForeignFieldName();
            SQLClient.EntityAttributes foreignEntityAttribute = this.sqlClient.getEntityAttribute(property.getValueType());
            String foreignKey = foreignEntityAttribute.getForeignKey(entityAttribute.getClazz());
            //填充数据，填充外聚合根
            foreignEntityAttribute.getFieldAccess().set(foreignInstance,foreignKey, databaseId);
            List<T> query = sqlClient.query(foreignInstance);
            EmbeddedHashMap<Object,Object> tempMap = new EmbeddedHashMap<>(instance);
            if(query!=null){
                for (T tForeignEntity : query) {
                    Object mappingId = foreignEntityAttribute.getFieldAccess().get(tForeignEntity, foreignFieldName);
                    tempMap.directPut(mappingId,tForeignEntity);
                }
            }
            entityAttribute.getFieldAccess().set(instance,fieldName,tempMap);
        }
    }

    public Object fieldTypeCast(Class<?> fieldType, String value) {
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.valueOf(value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.valueOf(value);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.valueOf(value);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.valueOf(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.valueOf(value);
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            return Byte.valueOf(value);
        } else if (fieldType == short.class || fieldType == Short.class) {
            return Short.valueOf(value);
        } else if (fieldType == char.class || fieldType == Character.class) {
            return value.charAt(0);
        } else if (fieldType == Date.class || fieldType == java.sql.Date.class || fieldType == java.sql.Timestamp.class) {
            return new Date(Long.parseLong(value));
        }
        System.out.println("Unsupported field type: " + fieldType);
        return null;
    }
}
