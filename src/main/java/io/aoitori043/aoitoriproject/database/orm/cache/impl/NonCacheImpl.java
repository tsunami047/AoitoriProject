//package io.aoitori043.aoitorilibrary.database.orm.cache.impl;
//
//import com.esotericsoftware.reflectasm.FieldAccess;
//import io.aoitori043.aoitorilibrary.database.orm.SQLClient;
//import io.aoitori043.aoitorilibrary.database.orm.cache.semaphore.LockUtil;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//import java.util.function.Consumer;
//
//import static io.aoitori043.aoitorilibrary.database.orm.ReflectASMUtil.createInstance;
//
///**
// * @Author: natsumi
// * @CreateTime: 2024-04-02  21:34
// * @Description: caffeine缓存只能在主线程上操作
// */
//public class NonCacheImpl {
//
//    public SQLClient sqlClient;
//
//
//    public NonCacheImpl(SQLClient sqlClient) {
//        this.sqlClient = sqlClient;
//    }
//
//
//    public <T> boolean delete(Class<T> clazz, Consumer<T> consumer) {
//        T queryEntity = createInstance(clazz);
//        consumer.accept(queryEntity);
//        return delete(clazz, queryEntity);
//    }
//
//    public <T> boolean delete(Class<T> clazz, T queryEntity) {
//        List<T> list = this.findByIDs(clazz, queryEntity);
//        String tableName = this.sqlClient.nameStructure.getTableName(clazz);
//        for (T entity : list) {
//            String aggregateRootKey = CacheImplUtil.getAggregateRootKey(tableName, CacheImplUtil.getQueryAggregateRoot(entity));
//            LockUtil.syncLock(aggregateRootKey, () -> {
//                this.sqlClient.caffeineCache.del(aggregateRootKey);
//                List<String> discreteRoots = CacheImplUtil.getDiscreteRoots(tableName, entity);
//                for (String discreteRoot : discreteRoots) {
//                    this.sqlClient.caffeineCache.del(discreteRoot);
//                }
//            });
//            LockUtil.asyncLock(aggregateRootKey, () -> {
//                this.sqlClient.redisCache.del(aggregateRootKey);
//                List<String> discreteRoots = CacheImplUtil.getDiscreteRoots(tableName, entity);
//                for (String discreteRoot : discreteRoots) {
//                    this.sqlClient.redisCache.delListElement(discreteRoot, aggregateRootKey);
//                }
//                this.sqlClient.sqlDelete.deleteNotCache(clazz, queryEntity);
//            });
//        }
//        return true;
//    }
//
//    public <T> void update(T updateEntity, T queryEntity) {
//        Class<T> aClass = (Class<T>) updateEntity.getClass();
//        String tableName = this.sqlClient.nameStructure.getTableName(aClass);
//        List<T> list = this.findByIDs(aClass, queryEntity);
//        FieldAccess fieldAccess = FieldAccess.get(aClass);
//        for (T cacheEntity : list) {
//            Object id = fieldAccess.get(cacheEntity, "id");
//            LockUtil.syncLock(CacheImplUtil.getAggregateRootKey(tableName, String.valueOf(id)), () -> {
//                for (String fieldName : fieldAccess.getFieldNames()) {
//                    if (fieldName.equalsIgnoreCase("id")) {
//                        continue;
//                    }
//                    Object value = fieldAccess.get(updateEntity, fieldName);
//                    if (value == null) {
//                        continue;
//                    }
//                    fieldAccess.set(cacheEntity, fieldName, value);
//                }
//            });
//        }
//        for (T cacheEntity : list) {
//            String aggregateRootKey = CacheImplUtil.getAggregateRootKey(tableName, fieldAccess, cacheEntity);
//            LockUtil.asyncLock(aggregateRootKey, () -> {
//                this.sqlClient.redisCache.putMap(aggregateRootKey, CacheImplUtil.serializeObject(cacheEntity));
//                this.sqlClient.sqlUpdate.updateNotCache(cacheEntity, queryEntity);
//            });
//        }
//    }
//
//    //有锁 TODO
//    public <T> void insert(Class<T> clazz, T insertEntity) {
//        String tableName = this.sqlClient.nameStructure.getTableName(clazz);
//        String aggregateRoot = CacheImplUtil.getQueryAggregateRoot(insertEntity);
//        if (aggregateRoot == null) {
//
//            long id = sqlClient.sqlInsert.directInsertObject(insertEntity);
//            String aggregateRootKey = CacheImplUtil.getAggregateRootKey(tableName, String.valueOf(id));
//            LockUtil.syncLock(aggregateRootKey, () -> {
//                FieldAccess fieldAccess = FieldAccess.get(clazz);
//                fieldAccess.set(insertEntity, "id", Long.valueOf(id).intValue());
//                this.sqlClient.caffeineCache.put(aggregateRootKey, insertEntity);
//                List<String> discreteKeys = CacheImplUtil.getDiscreteRoots(tableName, insertEntity);
//                for (String discreteKey : discreteKeys) {
//                    cacheDiscreteRootObjects(discreteKey, insertEntity);
//                }
//            });
//            //可脱手了，更新redis的缓存
//            LockUtil.asyncLock(aggregateRootKey, () -> {
//                this.sqlClient.redisCache.putMap(aggregateRootKey, CacheImplUtil.serializeObject(insertEntity));
//                List<String> discreteKeys = CacheImplUtil.getDiscreteRoots(tableName, insertEntity);
//                for (String discreteKey : discreteKeys) {
//                    this.sqlClient.redisCache.pushUnduplicateList(discreteKey, aggregateRootKey);
//                }
//            });
//        } else {
//            LockUtil.syncLock(aggregateRoot, () -> {
//                cacheEntityObjects(tableName, aggregateRoot, insertEntity);
//                sqlClient.sqlInsert.directInsertObject(insertEntity);
//            });
//        }
//    }
//
//    public <T> List<T> findByIDs(Class<T> clazz, @NotNull T queryEntity) {
//        String tableName = this.sqlClient.nameStructure.getTableName(clazz); //取出表名
//        String aggregateRoot = CacheImplUtil.getQueryAggregateRoot(queryEntity);
//        if (aggregateRoot == null) {
//            String queryDiscreteKey = CacheImplUtil.getDiscreteKey(clazz, tableName, queryEntity); //获取查询对象的 查询非聚合根
//            Object caffeineCache = this.sqlClient.caffeineCache.get(queryDiscreteKey);
//            if (caffeineCache != null) {
//                if (caffeineCache == CacheImplUtil.emptyObject) {
//                    return null;
//                }
//                return (List<T>) caffeineCache;
//            }
//            List<String> aggregateRoots = this.sqlClient.redisCache.getList(queryDiscreteKey); //转化成聚合根对象
//            if (aggregateRoots.isEmpty()) {
//                return getEntityFromMySQL(queryEntity, tableName);
//            } else {
//                List<T> entities = new ArrayList<>();
//                for (String aggregateRootKey : aggregateRoots) {
//                    if (!CacheImplUtil.addNotNullElement(entities, getEntityByRedis(tableName, clazz, aggregateRootKey))) {
//                        CacheImplUtil.addNotNullElement(entities, getEntityFromMySQLByAggregate(tableName, queryEntity, aggregateRootKey));
//                    }
//                }
//                return entities;
//            }
//        } else {
//            //聚合根查询
//            String aggregateRootKey = CacheImplUtil.getAggregateRootKey(tableName, aggregateRoot);
//            List<T> ts = (List<T>) LockUtil.syncLockSubmit(aggregateRootKey, () -> {
//                T entityByCaffeine = (T) this.sqlClient.caffeineCache.get(aggregateRootKey);
//                if (entityByCaffeine != null) {
//                    if (entityByCaffeine == CacheImplUtil.emptyObject) {
//                        return null;
//                    }
//                    return Collections.singletonList(entityByCaffeine);
//                }
//                return null;
//            });
//            if (ts != null) {
//                return ts;
//            }
//            Object entityByRedis = getEntityByRedis(tableName, clazz, aggregateRootKey);
//            if (entityByRedis != null) {
//                if (entityByRedis == CacheImplUtil.emptyObject) {
//                    return null;
//                }
//                return Collections.singletonList((T) entityByRedis);
//            }
//            T entityByMySQL = getEntityFromMySQLByAggregate(tableName, queryEntity, aggregateRootKey);
//            if (entityByMySQL == null) {
//                return null;
//            }
//            return Collections.singletonList(entityByMySQL);
//        }
//    }
//
//    public <T> void update(T updateEntity, Consumer<T> whereConsumer) {
//        Class<T> aClass = (Class<T>) updateEntity.getClass();
//        T queryEntity = createInstance(aClass);
//        whereConsumer.accept(queryEntity);
//        this.update(updateEntity, queryEntity);
//    }
//
//    public <T> void insert(Class<T> clazz, Consumer<T> consumer) {
//        T insertEntity = createInstance(clazz);
//        consumer.accept(insertEntity);
//        this.insert(clazz, insertEntity);
//    }
//
//    public <T> List<T> copyFindByIds(Class<T> clazz, @NotNull Consumer<T> consumer) {
//        T instance = createInstance(clazz);
//        consumer.accept(instance);
//        return copyFindByIds(clazz, instance);
//    }
//
//    public <T> List<T> copyFindByIds(Class<T> clazz, @NotNull T queryEntity) {
//        List<T> copyList = new ArrayList<>();
//        FieldAccess fieldAccess = FieldAccess.get(clazz);
//        List<T> originalEntities = findByIDs(clazz, queryEntity);
//        if (originalEntities == null) {
//            return null;
//        }
//        List<T> findEntities = new ArrayList<>(originalEntities);
//        for (T entity : findEntities) {
//            T copyEntity = createInstance(clazz);
//            for (String fieldName : fieldAccess.getFieldNames()) {
//                fieldAccess.set(copyEntity, fieldName, fieldAccess.get(entity, fieldName));
//            }
//            copyList.add(copyEntity);
//        }
//        return copyList;
//    }
//
//    public <T> List<T> findByIDs(Class<T> clazz, @NotNull Consumer<T> consumer) {
//        T queryEntity = createInstance(clazz);
//        consumer.accept(queryEntity);
//        return findByIDs(clazz, queryEntity);
//    }
//
//    //TODO 有锁
//    public <T> T getEntityFromMySQLByAggregate(String tableName, T queryInstance, String aggregateRootKey) {
//        List<T> ts = this.sqlClient.sqlQuery.directFindByIds(queryInstance);
//        if (ts == null || ts.isEmpty()) {
//            writeNullIfAbsentToCache(tableName, queryInstance, aggregateRootKey);
//            return null;
//        }
//        T cacheEntity = ts.get(0);
//        List<String> discreteKeys = CacheImplUtil.getDiscreteRoots(tableName, cacheEntity);
//        LockUtil.syncLock(aggregateRootKey, () -> {
//            this.sqlClient.caffeineCache.put(aggregateRootKey, cacheEntity);
//            for (String discreteKey : discreteKeys) {
//                cacheDiscreteRootObjects(discreteKey, cacheEntity);
//            }
//        });
//        LockUtil.asyncLock(aggregateRootKey, () -> {
//            this.sqlClient.redisCache.putMap(aggregateRootKey, CacheImplUtil.serializeObject(cacheEntity));
//            for (String discreteKey : discreteKeys) {
//                this.sqlClient.redisCache.pushUnduplicateList(discreteKey, aggregateRootKey);
//            }
//        });
//        return cacheEntity;
//    }
//
//    //TODO 有锁
//    private <T> void writeNullIfAbsentToCache(String tableName, T queryInstance, String aggregateRootKey) {
//        if (aggregateRootKey != null) {
//            LockUtil.syncLock(aggregateRootKey, () -> {
//                this.sqlClient.caffeineCache.put(aggregateRootKey, CacheImplUtil.emptyObject);
//            });
//            LockUtil.asyncLock(aggregateRootKey, () -> {
//                this.sqlClient.redisCache.putMap(aggregateRootKey, CacheImplUtil.map);
//            });
//        }
//        List<String> discreteRoots = CacheImplUtil.getDiscreteRoots(tableName, queryInstance);
//        for (String discreteRoot : discreteRoots) {
//            this.sqlClient.caffeineCache.put(discreteRoot, CacheImplUtil.emptyObject);
//            this.sqlClient.redisCache.setList(discreteRoot, CacheImplUtil.list);
//        }
//    }
//
//    //有锁 TODO
//    private <T> void cacheEntityObjects(String tableName, String aggregateRootKey, T cacheEntity) {
//        List<String> discreteKeys = CacheImplUtil.getDiscreteRoots(tableName, cacheEntity);
//        for (String discreteKey : discreteKeys) {
//            cacheDiscreteRootObjects(discreteKey, cacheEntity);
//        }
//        this.sqlClient.caffeineCache.put(aggregateRootKey, cacheEntity);
//        this.sqlClient.redisCache.putMap(aggregateRootKey, CacheImplUtil.serializeObject(cacheEntity));
//        for (String discreteKey : discreteKeys) {
//            this.sqlClient.redisCache.pushUnduplicateList(discreteKey, aggregateRootKey);
//        }
//    }
//
//    //有锁 TODO
//    @NotNull
//    private <T> List<T> getEntityFromMySQL(T queryInstance, String tableName) {
//        List<T> ts = this.sqlClient.sqlQuery.directFindByIds(queryInstance);
//        for (T t : ts) {
//            String queryAggregateRoot = CacheImplUtil.getQueryAggregateRoot(t);
//            LockUtil.syncLock(queryAggregateRoot, () -> {
//                String aggregateRootKey = CacheImplUtil.getAggregateRootKey(tableName, queryAggregateRoot);
//                cacheEntityObjects(tableName, aggregateRootKey, t);
//            });
//        }
//        //如果查询对象不存在，对缓存写入null
//        if (ts.isEmpty()) {
//            String queryAggregateRoot = CacheImplUtil.getQueryAggregateRoot(queryInstance);
//            if (queryAggregateRoot != null) {
//                LockUtil.syncLock(queryAggregateRoot, () -> {
//                    writeNullIfAbsentToCache(tableName, queryInstance, queryAggregateRoot);
//                });
//            }
//        }
//        return ts;
//    }
//
//    //有锁 TODO
//    public <T> void cacheDiscreteRootObjects(String discreteRoot, T t) {
//        Object o1 = this.sqlClient.caffeineCache.get(discreteRoot);
//        if (o1 != null) {
//            List list = (List) o1;
//            if (!list.contains(t)) {
//                list.add(t);
//            }
//        } else {
//            this.sqlClient.caffeineCache.put(discreteRoot, new ArrayList<>(Collections.singletonList(t)));
//        }
//    }
//
//    //调用前有锁 TODO
//    public <T> Object getEntityByRedis(String tableName, Class<T> clazz, String aggregateRootKey) {
//        Map<String, String> map = this.sqlClient.redisCache.getMap(aggregateRootKey);
//        if (map.isEmpty()) {
//            this.sqlClient.caffeineCache.put(aggregateRootKey, CacheImplUtil.emptyObject);
//            return CacheImplUtil.emptyObject;
//        }
//        T t = CacheImplUtil.injectData(clazz, map);
//        LockUtil.syncLock(aggregateRootKey, () -> {
//            this.sqlClient.caffeineCache.put(aggregateRootKey, t);
//            List<String> discreteKeys = CacheImplUtil.getDiscreteRoots(tableName, t);
//            for (String discreteKey : discreteKeys) {
//                cacheDiscreteRootObjects(discreteKey, t);
//            }
//        });
//        return t;
//    }
//
//
//}
