package io.aoitori043.aoitoriproject.database.orm;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.CanaryClientImpl;
import io.aoitori043.aoitoriproject.database.orm.cache.EmbeddedHashMap;
import io.aoitori043.aoitoriproject.database.orm.impl.*;
import io.aoitori043.aoitoriproject.database.orm.cache.CaffeineCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.cache.RedisCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.fetch.TableFetch;
import io.aoitori043.aoitoriproject.database.orm.sign.*;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLDeleteImpl;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLInsertImpl;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLQueryImpl;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLUpdateImpl;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import io.aoitori043.aoitoriproject.utils.Pair;
import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
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
//    public JVMCacheSync jvmCacheSync;
    public TableFetch tableFetch = new TableFetch(this);


    public SQLClient() {
    }


    public void build(){
        registerCacheImpl(PLAYER_EXCLUSIVE_DATA,new ExclusiveCacheImpl(this));
        registerCacheImpl(ONLY_MYSQL,new OnlyMySQLImpl(this));
        registerCacheImpl(ONLY_REDIS,new OnlyRedisImpl(this));
        registerCacheImpl(HIGH_VALUE_DATA,new HighValueCacheImpl(this));
        registerCacheImpl(FAST,new FastCacheImpl(this));
//        jvmCacheSync = new JVMCacheSync(this);

    }

    @Data
    public static class EntityAttributes{
        Class clazz;
        FieldAccess fieldAccess;
        String tableName;
        Cache.CacheType cacheType;

        String playerNameFieldName;
        String idFieldName;
        //离散根
        LinkedHashMap<Integer,String> discreteRootFieldNames = new LinkedHashMap<>();
        //外联映射
        HashMap<String,ForeignProperty> embeddedMapFieldProperties = new HashMap<>();
        //外键
        HashMap<Class,String> foreignKeyMap = new HashMap<>();
        //实体字段
        List<String> declaredFieldNames = new ArrayList<>();
        //可以修改的字段
        List<String> updateFields;

        //用于查询的字段
        String[] queryFields;

        Class playerNameSuperClass;

        public String getPlayerName(Object entity){
//            if(playerNameSuperClass!=null){
//                EntityAttributes superClassEntityAttribute = CanaryClientImpl.sqlClient.getEntityAttribute(playerNameSuperClass);
//                String foreignKey = getForeignKey(playerNameSuperClass);
//                Object o = fieldAccess.get(entity, foreignKey);
//                Object superEntity = ReflectASMUtil.createInstance(playerNameSuperClass);
//                superClassEntityAttribute.getFieldAccess().set(superEntity,superClassEntityAttribute.getIdFieldName(),o);
//                List<Object> query = CanaryClientImpl.sqlClient.query(superEntity);
//                if(query == null || query.isEmpty()){
//                    return null;
//                }
//                return superClassEntityAttribute.getPlayerName(query.get(0));
//            }
            if(playerNameSuperClass!=null){
                EntityAttributes superClassEntityAttribute = CanaryClientImpl.sqlClient.getEntityAttribute(playerNameSuperClass);
                String foreignKey = getForeignKey(playerNameSuperClass);
                String aggregateRootById = superClassEntityAttribute.getAggregateRootById(fieldAccess.get(entity, foreignKey));
                ExclusiveCacheImpl cache = (ExclusiveCacheImpl) CanaryClientImpl.sqlClient.getCacheHashMap().get(PLAYER_EXCLUSIVE_DATA);
                ExclusiveCacheImpl.CacheWrapper cacheWrapper = cache.aggregateRootCacheImpl.get(aggregateRootById);
                if(cacheWrapper == null){
                    return null;
                }
                return cacheWrapper.getPlayerName();
            }
            return fieldAccess.get(entity, playerNameFieldName).toString();
        }

        public void preloadPlayerName(){
            for (Class aClass : foreignKeyMap.keySet()) {
                EntityAttributes entityAttribute = CanaryClientImpl.sqlClient.getEntityAttribute(aClass);
                if (entityAttribute.playerNameFieldName !=null) {
                    playerNameSuperClass = aClass;
                    return;
                }
            }
        }

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

        public void setForeignRootFieldValue(Object o,Class superEntityClass,Long id){
            String foreignKey = this.getForeignKeyMap().get(superEntityClass);
            fieldAccess.set(o,foreignKey,id);
        }

        public void setId(Object o,Long id){
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


            List<String> queryFieldsList = new ArrayList<>();
            Field[] fields = fieldAccess.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
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
                if(!field.isAnnotationPresent(AggregateRoot.class) &&
                        !field.isAnnotationPresent(OneToMany.class) &&
                        !field.isAnnotationPresent(OneToOne.class) &&
                        !field.isAnnotationPresent(ManyToMany.class)){
                    queryFieldsList.add(field.getName());
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
                if (field.isAnnotationPresent(Index.class) || field.isAnnotationPresent(Key.class) || field.isAnnotationPresent(ForeignAggregateRoot.class)) {
                    discreteRootFieldNames.put(i,field.getName());
                }
            }
            preloadPlayerName();
            if(cacheType == PLAYER_EXCLUSIVE_DATA && (playerNameFieldName == null && playerNameSuperClass == null)){
                try {
                    throw new Exception("使用PLAYER_EXCLUSIVE_DATA存储类型，必须为你的玩家名字段加上注解！");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            queryFields = queryFieldsList.toArray(new String[queryFieldsList.size()]);
        }
        public static final String DISCRETE_SIGN = "%";

        public String getRedisIdKey(){
            return tableName+"_"+idFieldName;
        }

        public Object getQueryEntity(String aggregateRoot){
            Object instance = ReflectASMUtil.createInstance(clazz);
            fieldAccess.set(instance,idFieldName,Long.parseLong(aggregateRoot.split(":")[1]));
            return instance;
        }

        public List<String> getInsertDiscreteRoots(Object entity){
            List<Pair<Integer,String>> discreteRoots = new ArrayList<>();
            FieldAccess fieldAccess = FieldAccess.get(clazz);
            for (Map.Entry<Integer, String> entry : discreteRootFieldNames.entrySet()) {
                String fieldName = entry.getValue();
                Object o = fieldAccess.get(entity, fieldName);
                if(o == null){
                    continue;
                }
                discreteRoots.add(new Pair(entry.getKey(),o.toString()));
            }
            return CacheImplUtil.generateCombinations(tableName,discreteRoots);
        }

        public String getDiscreteRoot(Object entity){
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder index = new StringBuilder();
            int num = 0;
            if (discreteRootFieldNames != null) {
                for (Map.Entry<Integer, String> entry : discreteRootFieldNames.entrySet()) {
                    String fieldName = entry.getValue();
                    Object value = fieldAccess.get(entity, fieldName);
                    if (value != null) {
                        if (num != 0) {
                            stringBuilder.append("-");
                        }
                        index.append(entry.getKey());
                        stringBuilder.append(value);
                        num++;
                    }

                }
                return DISCRETE_SIGN + tableName + ":"+ index +"_"+ stringBuilder;
            }
            return null;
        }

        public String getAggregateRootById(Object id){
            return CacheImplUtil.getAggregateRootKey(tableName, String.valueOf(id));
        }

        public long getDatabaseId(Object entity){
            Object o = fieldAccess.get(entity, idFieldName);
            if(o == null){
                return -1;
            }
            return Long.valueOf(String.valueOf(o));
        }

        public String getAggregateRoot(Object entity){
            Object o = fieldAccess.get(entity, idFieldName);
            if(o == null){
                return null;
                //查询聚合根
//                List<Object> query = CanaryClientImpl.sqlClient.query(entity);
//                if(query == null ||query.isEmpty()){
//                    //要么插入数据获取聚合根
//                    return null;
//                }
//                Object o1 = fieldAccess.get(query.get(0), idFieldName);
//                if(o1 == null){
//                    return null;
//                }
//                return CacheImplUtil.getAggregateRootKey(tableName, o1.toString());
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

    public <T> T insert(Class<T> clazz,Consumer<T> insertEntity){
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
    public <T> T insert(T insertEntity){
        EntityAttributes entityAttributes = attributesHashMap.get(insertEntity.getClass());
        injectEmbeddedHashMap(insertEntity);
        this.nonnull(entityAttributes);
        return cacheHashMap.get(entityAttributes.cacheType).insert(insertEntity)?insertEntity:null;
    }


    public <T> List<T> query(Class<T> clazz,Consumer<T> insertEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.query(instance);
    }

    //从redis查询数据的时候，根据离散根查询到的数据，还需要判断其它变量是否对得上！
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

    public <T> T apply(Class<T> clazz,Consumer<T> insertEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.apply(instance);
    }

    //不存在就插入，存在就更新
    public <T> T apply(T entity){
        EntityAttributes entityAttributes = attributesHashMap.get(entity.getClass());
        this.nonnull(entityAttributes);
        CacheImpl cache = cacheHashMap.get(entityAttributes.cacheType);
        if(cache.hasApplyOverride()){
            return cache.apply(entity)?entity:null;
        }else {
            List<T> tEntities = cache.find(entity);
            if (tEntities == null || tEntities.isEmpty()) {
                //不存在，插入
                cache.insert(entity);
            } else {
                cache.update(entity,entity, CacheImpl.UpdateType.COPY_NULL);
            }
            return entity;
        }
    }

    public <T> T update(Class<T> clazz,T updateEntity,Consumer<T> insertEntity, CacheImpl.UpdateType updateType){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        insertEntity.accept(instance);
        return this.update(updateEntity,instance,updateType);
    }

    public <T> T update(T updateEntity, T anchorEntity, CacheImpl.UpdateType updateType){
        EntityAttributes entityAttributes = attributesHashMap.get(updateEntity.getClass());
        this.nonnull(entityAttributes);
        return cacheHashMap.get(entityAttributes.cacheType).update(updateEntity,anchorEntity,updateType)?updateEntity:null;
    }
}
