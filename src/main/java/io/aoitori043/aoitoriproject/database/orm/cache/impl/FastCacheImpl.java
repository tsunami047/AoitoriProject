package io.aoitori043.aoitoriproject.database.orm.cache.impl;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.RedisCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.cache.semaphore.LockUtil;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.util.*;

import static io.aoitori043.aoitoriproject.database.orm.cache.impl.CacheImpl.UpdateType.NOT_COPY_NULL;

public class FastCacheImpl extends CacheImpl{
    public FastCacheImpl(SQLClient sqlClient) {
        super(sqlClient);
    }

    public RedisCacheImpl myRedisCore = new RedisCacheImpl(){
        @Override
        public Jedis getJedisConnection(){
            return RedisCore.mainRedis.getConnection();
        }
        @Override
        public void setList(String key, List<String> list){
            try (Jedis jedis = getJedisConnection()) {
                jedis.rpush(key,list.toArray(new String[0]));
            }
        }
        @Override
        public void putMap(String key, Map<String,String> map){
            try (Jedis jedis = getJedisConnection()) {
                jedis.hmset(key,map);
            }
        }
    };

    public <T> boolean update(T updateEntity, T anchorEntity, UpdateType updateType) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(anchorEntity.getClass());
            List<T> entities = find(anchorEntity);
            for (T entity : entities) {
                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    FieldAccess fieldAccess = entityAttribute.getFieldAccess();
                    for (String fieldName : fieldAccess.getFieldNames()) {
                        if (fieldName.equalsIgnoreCase("id")) {
                            continue;
                        }
                        Object value = fieldAccess.get(updateEntity, fieldName);
                        if (value == null && updateType == NOT_COPY_NULL) {
                            continue;
                        }
                        fieldAccess.set(entity, fieldName, value);
                    }

                    this.myRedisCore.putMap(aggregateRoot, serializeObject(entity));
                });
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T> boolean delete(T whereEntity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
            List<T> entities = find(whereEntity);
            for (T entity : entities) {
                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    cascadingDelete(entityAttribute, entity);
                    this.sqlClient.caffeineCache.del(aggregateRoot);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.caffeineCache.del(insertDiscreteRoot);
                    }
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    this.sqlClient.redisCache.del(aggregateRoot);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.delListElement(insertDiscreteRoot, aggregateRoot);
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T> boolean apply(T entity) {
        return false;
    }

    @Override
    public <T> boolean insert(T entity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
            String aggregateRoot = entityAttribute.getAggregateRoot(entity);
            if (aggregateRoot == null) {
                List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                String redisIdKey = entityAttribute.getRedisIdKey();
                int incr;
                try(Jedis jedisConnection = sqlClient.redisCache.getJedisConnection()){
                    incr = (int)jedisConnection.incr(redisIdKey);
                }
                String myAggregateRoot = entityAttribute.getAggregateRootById(incr);
                LockUtil.syncLock(myAggregateRoot, () -> {
                    entityAttribute.getFieldAccess().set(entity, "id", incr);

                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        caffeineAddDiscreteRoot(insertDiscreteRoot, entity);
                    }
                    sqlClient.caffeineCache.put(myAggregateRoot, entity);
                });
                LockUtil.asyncLock(myAggregateRoot, () -> {
                    initialEmbeddedObject(entityAttribute, entity, incr);
                    HashMap<String, String> serializedObject = serializeObject(entity);
                    this.sqlClient.redisCache.putMap(myAggregateRoot, serializedObject);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.pushUnduplicateList(insertDiscreteRoot, myAggregateRoot);
                    }
                });
            } else {
                List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        caffeineAddDiscreteRoot(insertDiscreteRoot, entity);
                    }
                    sqlClient.caffeineCache.put(aggregateRoot, entity);
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    HashMap<String, String> serializedObject = serializeObject(entity);
                    this.sqlClient.redisCache.putMap(aggregateRoot, serializedObject);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.pushUnduplicateList(insertDiscreteRoot, aggregateRoot);
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void cachingCaffeine(SQLClient.EntityAttributes entityAttribute, String aggregateRoot, Object entity) {
        this.injectForeignEntities(entity);
        super.cachingCaffeine(entityAttribute, aggregateRoot, entity);
    }

    @Override
    public <T> T getEntityFromRedis(SQLClient.EntityAttributes entityAttribute, @NotNull String aggregateRoot) {
        return LockUtil.syncLockSubmit(aggregateRoot, () -> {
            Map<String, String> map = this.sqlClient.redisCache.getMap(aggregateRoot);
            if (map.isEmpty()) {
                return null;
            }
            T tEntity = (T) injectData(entityAttribute, map);

            cachingCaffeine(entityAttribute, aggregateRoot, tEntity);
            return tEntity;
        });
    }

    public <T> List<T> find(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if (aggregateRoot == null) {
            String discreteRoot = entityAttribute.getDiscreteRoot(whereEntity);
            Object o = this.sqlClient.caffeineCache.get(discreteRoot);
            if (o != null) {
                //可以在这里增加锁算法，但是会降低效率
                return o == CacheImplUtil.emptyObject ? null : (List<T>) o;
            }
            List<String> aggregateRoots = this.sqlClient.redisCache.getList(discreteRoot);
            if (!aggregateRoots.isEmpty()) {
                List<T> entities = new ArrayList<>();
                for (String loopAggregateRoot : aggregateRoots) {
                    if (!CacheImplUtil.addNotNullElement(entities, getEntityFromRedis(entityAttribute, loopAggregateRoot))) {

                    }
                }
                return entities;
            }
        } else {
            return LockUtil.syncLockSubmit(aggregateRoot, () -> {
                Object entityFromCaffeine = this.sqlClient.caffeineCache.get(aggregateRoot);
                if (entityFromCaffeine != null) {
                    return entityFromCaffeine == CacheImplUtil.emptyObject ? null : Collections.singletonList((T) entityFromCaffeine);
                }
                Object entityFromRedis = getEntityFromRedis(entityAttribute, aggregateRoot);
                if (entityFromRedis != null) {
                    return Collections.singletonList((T) entityFromRedis);
                }
                return null;
            });
        }
        return null;
    }

    @Override
    public boolean hasApplyOverride() {
        return false;
    }
}