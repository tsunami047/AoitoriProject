package io.aoitori043.aoitoriproject.database.orm.impl;

import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.RedisCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.semaphore.LockUtil;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.util.*;

public class OnlyRedisImpl extends CacheImpl {

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

    public OnlyRedisImpl(SQLClient sqlClient) {
        super(sqlClient);
    }

    @Override
    public <T> boolean update(T updateEntity, T anchorEntity, UpdateType updateType) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(anchorEntity.getClass());
            List<T> entities = find(anchorEntity);
            for (T entity : entities) {
                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    myRedisCore.putMap(aggregateRoot, serializeObject(entity));
                    this.updateForeignObject(entity);
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
                LockUtil.syncLock(aggregateRoot,()->{
                    cascadingDelete(entityAttribute, whereEntity);
                    myRedisCore.del(aggregateRoot);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        myRedisCore.delListElement(insertDiscreteRoot, aggregateRoot);
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
    public <T> T getEntityFromRedis(SQLClient.EntityAttributes entityAttribute, @NotNull String aggregateRoot) {
        return LockUtil.syncLockSubmit(aggregateRoot, () -> {
            Map<String, String> map = this.myRedisCore.getMap(aggregateRoot);
            if (map.isEmpty()) {
                return null;
            }
            T tEntity = (T) injectData(entityAttribute, map);
            this.injectForeignEntities(tEntity);
            return tEntity;
        });
    }

    @Override
    public <T> boolean insert(T entity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
            String aggregateRoot = entityAttribute.getAggregateRoot(entity);
            if (aggregateRoot == null) {
                String redisIdKey = entityAttribute.getRedisIdKey();
                try(Jedis jedisConnection = this.myRedisCore.getJedisConnection()){
                    long id = jedisConnection.incr(redisIdKey);
                    String myAggregateRoot = entityAttribute.getAggregateRootById(id);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    LockUtil.syncLock(myAggregateRoot, () -> {
                        entityAttribute.getFieldAccess().set(entity,entityAttribute.getIdFieldName(),id);
                        initialEmbeddedObject(entityAttribute, entity, id);
                        HashMap<String, String> serializedObject = serializeObject(entity);
                        myRedisCore.putMap(myAggregateRoot, serializedObject);
                        for (String insertDiscreteRoot : insertDiscreteRoots) {
                            myRedisCore.pushUnduplicateList(insertDiscreteRoot, myAggregateRoot);
                        }
                    });
                }
            } else {
                List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    long id = entityAttribute.getDatabaseId(entity);
                    initialEmbeddedObject(entityAttribute, entity, id);
                    HashMap<String, String> serializedObject = serializeObject(entity);
                    myRedisCore.putMap(aggregateRoot, serializedObject);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        myRedisCore.pushUnduplicateList(insertDiscreteRoot, aggregateRoot);
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
    public <T> List<T> find(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if (aggregateRoot == null) {
            String discreteRoot = entityAttribute.getDiscreteRoot(whereEntity);
            List<String> aggregateRoots = myRedisCore.getList(discreteRoot);
            if (!aggregateRoots.isEmpty()) {
                List<T> entities = new ArrayList<>();
                for (String loopAggregateRoot : aggregateRoots) {
                    if (!CacheImplUtil.addNotNullElement(entities, getEntityFromRedis(entityAttribute, loopAggregateRoot))) {
                        //从redis无法根据redis已经给出的聚合根获取
                    }
                }
                return entities;
            }
            return null;
        } else {
                Object entityFromRedis = getEntityFromRedis(entityAttribute, aggregateRoot);
                if (entityFromRedis != null) {
                    return Collections.singletonList((T) entityFromRedis);
            }
        }
        return null;
    }

    @Override
    public boolean hasApplyOverride() {
        return false;
    }
}
