package io.aoitori043.aoitoriproject.database.orm.cache.impl;

import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.semaphore.LockUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-08  23:46
 * @Description: updateAfterFetch
 */
public class HighValueCacheImpl extends CacheImpl {


    public HighValueCacheImpl(SQLClient sqlClient) {
        super(sqlClient);
//        updateAfterFetch(Drops.class,
//                queryEntity->{
//            queryEntity.setPlayerName("tsunami047");
//            },
//                updateEntity->{
//            HashMap<Integer, DropItems> map = updateEntity.getMap();
//        });
    }

    public <T> void updateAfterFetch(Class<T> clazz, Consumer<T> queryEntity,Consumer<T> updateEntity){
        T instance = (T) ReflectASMUtil.createInstance(clazz);
        queryEntity.accept(instance);
        List<T> entities = find(instance);
        for (T entity : entities) {
            updateEntity.accept(entity);
        }
    }


    @Override
    public <T> boolean update(T updateEntity, T anchorEntity, UpdateType updateType) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(anchorEntity.getClass());
            List<T> entities = find(anchorEntity);
            for (T entity : entities) {
                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
                updateEntity(entityAttribute, entity, updateEntity, updateType);
                LockUtil.syncLock(aggregateRoot, () -> {
                    this.sqlClient.redisCache.putMap(aggregateRoot, serializeObject(entity));
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    this.sqlClient.sqlUpdate.updateNotCache(entity, entity);
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //必须级联删除持有外键的记录，再删除实体记录，以后有聚合根就不用查询了
    @Override
    public <T> boolean delete(T whereEntity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
            List<T> entities = find(whereEntity);
            for (T entity : entities) {
                cascadingDelete(entityAttribute, entity);
                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    this.sqlClient.redisCache.del(aggregateRoot);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.delListElement(insertDiscreteRoot, aggregateRoot);
                    }
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    this.sqlClient.sqlDelete.deleteNotCache(entityAttribute.getClazz(), entity);
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public <T> boolean insert(T entity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
            String aggregateRoot = entityAttribute.getAggregateRoot(entity);
            if (aggregateRoot == null) {
                int id = sqlClient.sqlInsert.directInsertObject(entity);
                String myAggregateRoot = entityAttribute.getAggregateRootById(id);
                List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                entityAttribute.setId(entity, id);
                LockUtil.syncLock(myAggregateRoot, () -> {
                    initialEmbeddedObject(entityAttribute, entity, id);
                    HashMap<String, String> serializedObject = serializeObject(entity);
                    this.sqlClient.redisCache.putMap(myAggregateRoot, serializedObject);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.pushUnduplicateList(insertDiscreteRoot, myAggregateRoot);
                    }
                });
            } else {
                List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    HashMap<String, String> serializedObject = serializeObject(entity);
                    this.sqlClient.redisCache.putMap(aggregateRoot, serializedObject);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.pushUnduplicateList(insertDiscreteRoot, aggregateRoot);
                    }
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    int id = sqlClient.sqlInsert.directInsertObject(entity);
                    initialEmbeddedObject(entityAttribute, entity, id);
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> List<T> getEntityFromMySQLByAggregateRoot(SQLClient.EntityAttributes entityAttribute, String queryAggregateRoot) {
        T queryEntity = (T) entityAttribute.getQueryEntity(queryAggregateRoot);
        return getEntityFromMySQLByQueryEntity(entityAttribute, queryEntity);
    }

    public <T> List<T> getEntityFromMySQLByQueryEntity(SQLClient.EntityAttributes entityAttribute, T whereEntity) {
        List<T> tEntities = this.sqlClient.sqlQuery.directFindByIds(whereEntity);
        if (tEntities == null || tEntities.isEmpty()) {
            String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
            if (aggregateRoot != null) {
                LockUtil.asyncLock(aggregateRoot, () -> {
                    this.sqlClient.redisCache.putMap(aggregateRoot, CacheImplUtil.map);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(whereEntity);
                    for (String discreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.setList(discreteRoot, CacheImplUtil.list);
                    }
                });
            }
            return null;
        }
        for (T tEntity : tEntities) {
            String loopAggregateRoot = entityAttribute.getAggregateRoot(tEntity);
            LockUtil.syncLock(loopAggregateRoot, () -> cachingRedis(entityAttribute, loopAggregateRoot, tEntity));
        }
        return tEntities;
    }

    @Override
    public <T> T getEntityFromRedis(SQLClient.EntityAttributes entityAttribute, @NotNull String aggregateRoot) {
        return LockUtil.syncLockSubmit(aggregateRoot, () -> {
            Map<String, String> map = this.sqlClient.redisCache.getMap(aggregateRoot);
            if (map.isEmpty() || map.containsKey("null")) {
                return null;
            }
            T tEntity = (T) injectData(entityAttribute, map);
            return tEntity;
        });
    }

    @NotNull
    @Override
    public <T> List<T> find(T whereEntity) {
        List<T> interceptor = findInterceptor(whereEntity);
        for (T t : interceptor) {
            this.injectForeignEntities(t);
        }
        return interceptor;
    }

    @NotNull
    public <T> List<T> findInterceptor(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if (aggregateRoot == null) {
            String discreteRoot = entityAttribute.getDiscreteRoot(whereEntity);
            List<String> aggregateRoots = this.sqlClient.redisCache.getList(discreteRoot);
            if (aggregateRoots.isEmpty()) {
                List<T> tlist = this.sqlClient.sqlQuery.directFindByIds(whereEntity);
                for (T tEntity : tlist) {
                    String loopAggregateRoot = entityAttribute.getAggregateRoot(tEntity);
                    LockUtil.asyncLock(loopAggregateRoot, () -> cachingRedis(entityAttribute, loopAggregateRoot, tEntity));
                }
                return tlist;
            } else {
                List<T> entities = new ArrayList<>();
                for (String loopAggregateRoot : aggregateRoots) {
                    if (!CacheImplUtil.addNotNullElement(entities, getEntityFromRedis(entityAttribute, loopAggregateRoot))) {
                        if (!CacheImplUtil.addNotNullElement(entities, getEntityFromMySQLByAggregateRoot(entityAttribute, loopAggregateRoot))) {
                            //在redis上移除这个聚合根，从mysql上也不能根据聚合根查询到数据
                        }
                    }
                }
                return entities;
            }
        } else {
//            List<T> resultList = LockUtil.syncLockSubmit(aggregateRoot, () -> {
            Object entityFromRedis = getEntityFromRedis(entityAttribute, aggregateRoot);
            if (entityFromRedis != null) {
                return Collections.singletonList((T) entityFromRedis);
            }
//                return null;
//            });
//            if (resultList == null) {
            List<Object> entityFromMySQLByAggregateRoot = getEntityFromMySQLByAggregateRoot(entityAttribute, aggregateRoot);
            if (entityFromMySQLByAggregateRoot != null) {
                Object entityFromMySQL = entityFromMySQLByAggregateRoot.get(0);
                LockUtil.asyncLock(aggregateRoot, () -> cachingRedis(entityAttribute, aggregateRoot, entityFromMySQL));
                return Collections.singletonList((T) entityFromMySQL);
            }
//            }
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasApplyOverride() {
        return false;
    }
}
