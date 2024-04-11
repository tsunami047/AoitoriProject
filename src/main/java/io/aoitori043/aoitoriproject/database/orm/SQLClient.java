package io.aoitori043.aoitoriproject.database.orm;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.orm.cache.EmbeddedHashMap;
import io.aoitori043.aoitoriproject.database.orm.cache.impl.*;
import io.aoitori043.aoitoriproject.database.orm.cache.CaffeineCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.cache.JVMCacheSync;
import io.aoitori043.aoitoriproject.database.orm.cache.RedisCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.fetch.TableFetch;
import io.aoitori043.aoitoriproject.database.orm.sign.*;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLDeleteImpl;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLInsertImpl;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLQueryImpl;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLUpdateImpl;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static io.aoitori043.aoitoriproject.database.orm.sign.Cache.CacheType.*;


/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  16:56
 * @Description: ?
 */
@Getter
public class SQLClient {

    public NameStructure nameStructure = new NameStructure();
    public SQLDeleteImpl sqlDelete = new SQLDeleteImpl(this);
    public SQLInsertImpl sqlInsert = new SQLInsertImpl(this);
    public SQLQueryImpl sqlQuery = new SQLQueryImpl(this);
    public SQLUpdateImpl sqlUpdate = new SQLUpdateImpl(this);
    public TableBuilder tableBuilder = new TableBuilder(this);
    public RedisCore redis = RedisCore.mainRedis;
    public RedisCacheImpl redisCache = new RedisCacheImpl(Duration.ofHours(3));
    public CaffeineCacheImpl caffeineCache = new CaffeineCacheImpl(512,Duration.ofMinutes(10));
//    public NonCacheImpl cache = new NonCacheImpl(this);
    public JVMCacheSync jvmCacheSync;
    public TableFetch tableFetch = new TableFetch(this);


    public SQLClient() {
    }


    public void build(){
        registerCacheImpl(PLAYER_EXCLUSIVE_DATA,new ExclusiveCacheImpl(this));
        registerCacheImpl(ONLY_MYSQL,new OnlyMySQLImpl(this));
        registerCacheImpl(ONLY_REDIS,new OnlyRedisImpl(this));
        registerCacheImpl(HIGH_VALUE_DATA,new HighValueCacheImpl(this));
        registerCacheImpl(FAST,new FastCacheImpl(this));
        jvmCacheSync = new JVMCacheSync(this);

    }

    @Data
    public static class EntityAttributes{
        Class clazz;
        FieldAccess fieldAccess;
        String tableName;
        Cache.CacheType cacheType;

        String playerNameFieldName;
        String idFieldName;

        List<String> discreteRootFieldNames = new ArrayList<>();
        HashMap<String,ForeignProperty> embeddedMapFieldProperties = new HashMap<>();
        HashMap<Class,String> foreignKeyMap = new HashMap<>();
        List<String> declaredFieldNames = new ArrayList<>();

        List<String> updateFields;

        public EmbeddedHashMap<?,?> getForeignMap(Object o,String fieldName){
            Object o1 = fieldAccess.get(o, fieldName);
            if(o1 == null){
                return null;
            }
            return (EmbeddedHashMap<?, ?>) o1;
        }

        public void preloadUpdateField(Class clazz){
            updateFields = new ArrayList<>();
            for (Field field : fieldAccess.getFields()) {
                if(field.isAnnotationPresent(Key.class) ||
                        field.isAnnotationPresent(AggregateRoot.class) ||
                        field.isAnnotationPresent(ManyToMany.class) ||
                        field.isAnnotationPresent(OneToMany.class) ||
                        field.isAnnotationPresent(OneToOne.class) ||
                        field.isAnnotationPresent(PlayerName.class)){
                    continue;
                }
                updateFields.add(field.getName());
            }
        }

        public String getForeignKey(Class clazz){
            return foreignKeyMap.get(clazz);
        }

        @Data
        public static class ForeignProperty{
            String foreignFieldName;
            Class keyType;
            Class valueType;
        }

        public void setForeignRootFieldValue(Object o,Class superEntityClass,Integer id){
            String foreignKey = this.getForeignKeyMap().get(superEntityClass);
            fieldAccess.set(o,foreignKey,id);
        }

        public void setId(Object o,Integer id){
            fieldAccess.set(o,idFieldName,id);
        }

        public EntityAttributes(SQLClient sqlClient,Class clazz) {
            this.clazz = clazz;
            fieldAccess = FieldAccess.get(clazz);
            Entity entityAnnotation = (Entity) clazz.getAnnotation(Entity.class);
            if(entityAnnotation != null){
                tableName = entityAnnotation.tableName();
            }else{
                tableName = sqlClient.nameStructure.getTableName(clazz);
            }
            Cache cacheAnnotation = (Cache) clazz.getAnnotation(Cache.class);
            if(cacheAnnotation == null){
                cacheType = HIGH_VALUE_DATA;
            }else {
                cacheType = cacheAnnotation.cacheType();
            }
            preloadUpdateField(clazz);
            for (Field field : fieldAccess.getFields()) {
                if(field.isAnnotationPresent(ForeignAggregateRoot.class)){
                    ForeignAggregateRoot annotation = field.getAnnotation(ForeignAggregateRoot.class);
                    Class aClass = annotation.mapEntity();
                    foreignKeyMap.put(aClass,field.getName());
                }
                if (field.isAnnotationPresent(ManyToMany.class) ) {
                    ManyToMany annotation = field.getAnnotation(ManyToMany.class);
                    ForeignProperty foreignProperty = new ForeignProperty();
                    foreignProperty.setValueType(annotation.mapEntity());
                    FieldAccess annotationFieldAccess = FieldAccess.get(annotation.mapEntity());
                    Class fieldType = annotationFieldAccess.getFieldTypes()[annotationFieldAccess.getIndex(annotation.mapFieldName())];
                    foreignProperty.setForeignFieldName(annotation.mapFieldName());
                    foreignProperty.setKeyType(fieldType);
                    embeddedMapFieldProperties.put(field.getName(),foreignProperty);
                }
                if (field.isAnnotationPresent(OneToMany.class)) {
                    OneToMany annotation = field.getAnnotation(OneToMany.class);
                    ForeignProperty foreignProperty = new ForeignProperty();
                    foreignProperty.setValueType(annotation.mapEntity());
                    FieldAccess annotationFieldAccess = FieldAccess.get(annotation.mapEntity());
                    Class fieldType = annotationFieldAccess.getFieldTypes()[annotationFieldAccess.getIndex(annotation.mapFieldName())];
                    foreignProperty.setForeignFieldName(annotation.mapFieldName());
                    foreignProperty.setKeyType(fieldType);
                    embeddedMapFieldProperties.put(field.getName(),foreignProperty);
                }
                if (field.isAnnotationPresent(OneToOne.class)) {
                    OneToOne annotation = field.getAnnotation(OneToOne.class);
                    ForeignProperty foreignProperty = new ForeignProperty();
                    foreignProperty.setValueType(annotation.mapEntity());
                    foreignProperty.setForeignFieldName(annotation.mapFieldName());
                    FieldAccess annotationFieldAccess = FieldAccess.get(annotation.mapEntity());
                    embeddedMapFieldProperties.put(field.getName(),foreignProperty);
                }
                if(!field.isAnnotationPresent(OneToMany.class) &&
                        !field.isAnnotationPresent(OneToOne.class) &&
                        !field.isAnnotationPresent(ManyToMany.class)){
                    declaredFieldNames.add(field.getName());
                }
                if(field.isAnnotationPresent(AggregateRoot.class)){
                    idFieldName = field.getName();
                    continue;
                } else if (field.isAnnotationPresent(PlayerName.class)) {
                    playerNameFieldName = field.getName();
                }
                if (field.isAnnotationPresent(Key.class) || field.isAnnotationPresent(ForeignAggregateRoot.class)) {
                    discreteRootFieldNames.add(field.getName());
                }
            }

        }
        public static final String DISCRETE_SIGN = "%";

        public String getRedisIdKey(){
            return tableName+"_"+idFieldName;
        }

        public Object getQueryEntity(String aggregateRoot){
            Object instance = ReflectASMUtil.createInstance(clazz);
            fieldAccess.set(instance,idFieldName,Integer.parseInt(aggregateRoot.split(":")[1]));
            return instance;
        }

        public List<String> getInsertDiscreteRoots(Object entity){
            List<String> valueList = new ArrayList<>();
            FieldAccess fieldAccess = FieldAccess.get(clazz);
            for (String fieldName : discreteRootFieldNames) {
                Object o = fieldAccess.get(entity, fieldName);
                if(o == null){
                    continue;
                }
                valueList.add(o.toString());
            }
            return CacheImplUtil.generateCombinations(tableName,valueList);
        }

        public String getDiscreteRoot(Object entity){
            StringBuilder stringBuilder = new StringBuilder(DISCRETE_SIGN + tableName + ":");
            int num = 0;
            if (discreteRootFieldNames != null) {
                for (String fieldName : discreteRootFieldNames) {
                    Object value = fieldAccess.get(entity, fieldName);
                    if (value != null) {
                        if (num != 0) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append(value);
                        num++;
                    }
                }
                return stringBuilder.toString();
            }
            return null;
        }

        public String getPlayerName(Object entity){
            return fieldAccess.get(entity, playerNameFieldName).toString();
        }

        public String getAggregateRootById(Object id){
            return CacheImplUtil.getAggregateRootKey(tableName, String.valueOf(id));
        }

        public int getDatabaseId(Object entity){
            Object o = fieldAccess.get(entity, idFieldName);
            if(o == null){
                return -1;
            }
            return Integer.valueOf(String.valueOf(o));
        }

        public String getAggregateRoot(Object entity){
            Object o = fieldAccess.get(entity, idFieldName);
            if(o == null){
                return null;
            }
            return CacheImplUtil.getAggregateRootKey(tableName, o.toString());
        }

        public void injectEmbeddedHashMap(Object o){
            for (String fieldName : embeddedMapFieldProperties.keySet()) {
                fieldAccess.set(o,fieldName,new EmbeddedHashMap<>(o));
            }
        }
    }

    public HashMap<Class,EntityAttributes> attributesHashMap = new HashMap<>();
    public HashMap<Cache.CacheType,CacheImpl> cacheHashMap = new HashMap<>();

    public EntityAttributes getEntityAttribute(Class clazz){
        return attributesHashMap.get(clazz);
    }

    public void registerCacheImpl(Cache.CacheType cacheType,CacheImpl cache){
        cacheHashMap.put(cacheType, cache);
    }

    private List<Class> lostForeignKeyClasses = new ArrayList<>();

    public synchronized void bindEntity(Class clazz){
        EntityAttributes tEntityAttributes = new EntityAttributes(this,clazz);
        FieldAccess fieldAccess = FieldAccess.get(clazz);
        for (Field field : fieldAccess.getFields()) {
            if (field.getType().isPrimitive()) {
                System.out.println(clazz.getSimpleName()+"实体定义成员："+field.getName() + " 是基元类型，这会导致严重错误，此实体绑定失败。");
                return;
            }
        }
        attributesHashMap.put(clazz,tEntityAttributes);
        switch (tEntityAttributes.cacheType){
            case HIGH_VALUE_DATA:
            case PLAYER_EXCLUSIVE_DATA:
            case ONLY_MYSQL:
                switch (tableBuilder.createTable(clazz)) {
                    case LOST_FOREIGN_TABLE:
                        System.out.println(tEntityAttributes.getTableName()+" 表创建失败，缺少外键，将再下一次实体绑定中重新创建");
                        lostForeignKeyClasses.add(clazz);
                        break;
                    case CREATE_SUCCESS:{
                        System.out.println("成功绑定实体："+clazz.getSimpleName());
                        break;
                    }
                }
                break;
        }
        ArrayList<Class> classes = new ArrayList<>(lostForeignKeyClasses);
        for (Class aClass : classes) {
            switch (tableBuilder.createTable(aClass)) {
                case CREATE_SUCCESS:
                    System.out.println("成功绑定实体："+clazz.getSimpleName());
                    break;
                case CREATE_FAILURE:
                    lostForeignKeyClasses.remove(aClass);
                    break;
            }
        }
    }

    public <T> void injectEmbeddedHashMap(T instance){
        EntityAttributes entityAttribute = this.getEntityAttribute(instance.getClass());
        entityAttribute.injectEmbeddedHashMap(instance);
    }

    public <T> boolean insert(Class<T> clazz,Consumer<T> insertEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        injectEmbeddedHashMap(instance);
        insertEntity.accept(instance);
        return this.insert(instance);
    }

    public void nonnull(Object entity){
        if(entity == null){
            throw new NullPointerException("传入实体未绑定");
        }
    }

    @Deprecated
    public <T> boolean insert(T insertEntity){
        EntityAttributes entityAttributes = attributesHashMap.get(insertEntity.getClass());
        this.nonnull(entityAttributes);
        return cacheHashMap.get(entityAttributes.cacheType).insert(insertEntity);
    }


    public <T> List<T> query(Class<T> clazz,Consumer<T> insertEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.query(instance);
    }

    public <T> List<T> query(T insertEntity){
        EntityAttributes entityAttributes = attributesHashMap.get(insertEntity.getClass());
        this.nonnull(entityAttributes);
        return cacheHashMap.get(entityAttributes.cacheType).find(insertEntity);
    }

    public <T> boolean delete(Class<T> clazz,Consumer<T> insertEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.delete(instance);
    }

    public <T> boolean delete(T insertEntity){
        EntityAttributes entityAttributes = attributesHashMap.get(insertEntity.getClass());
        this.nonnull(entityAttributes);
        return cacheHashMap.get(entityAttributes.cacheType).delete(insertEntity);
    }

    public <T> boolean apply(Class<T> clazz,Consumer<T> insertEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.apply(instance);
    }

    //不存在就插入，存在就更新
    public <T> boolean apply(T entity){
        EntityAttributes entityAttributes = attributesHashMap.get(entity.getClass());
        this.nonnull(entityAttributes);
        CacheImpl cache = cacheHashMap.get(entityAttributes.cacheType);
        if(cache.hasApplyOverride()){
            return cache.apply(entity);
        }else {
            List<T> tEntities = cache.find(entity);
            if (tEntities == null || tEntities.isEmpty()) {
                //不存在，插入
                cache.insert(entity);
            } else {
                cache.apply(entity);
            }
            return true;
        }
    }

    public <T> boolean update(Class<T> clazz,T updateEntity,Consumer<T> insertEntity, CacheImpl.UpdateType updateType){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.update(updateEntity,instance,updateType);
    }

    public <T> boolean update(T updateEntity, T anchorEntity, CacheImpl.UpdateType updateType){
        EntityAttributes entityAttributes = attributesHashMap.get(updateEntity.getClass());
        this.nonnull(entityAttributes);
        return cacheHashMap.get(entityAttributes.cacheType).update(updateEntity,anchorEntity,updateType);
    }
}
