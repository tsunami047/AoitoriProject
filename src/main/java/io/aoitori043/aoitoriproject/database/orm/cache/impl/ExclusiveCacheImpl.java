package io.aoitori043.aoitoriproject.database.orm.cache.impl;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.PluginProvider;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.CaffeineCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.cache.semaphore.LockUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.aoitori043.aoitoriproject.database.orm.cache.impl.CacheImpl.UpdateType.NOT_COPY_NULL;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;
import static org.bukkit.event.player.PlayerPreLoginEvent.Result.ALLOWED;
@Getter
public class ExclusiveCacheImpl extends CacheImpl {

    public static final String EXCLUSIVE_DATABASE = "exclusive_map";
    public final ExclusiveCaffeineCacheImpl caffeineCache;

    public ExclusiveCacheImpl(SQLClient sqlClient) {
        super(sqlClient);
        JavaPlugin javaPlugin = PluginProvider.getJavaPlugin();
        Bukkit.getPluginManager().registerEvents(new ExclusiveCacheBukkitListener(this), javaPlugin);
        caffeineCache = new ExclusiveCaffeineCacheImpl(this);
    }

    @Override
    public <T> T getEntityFromRedis(SQLClient.EntityAttributes entityAttribute, @NotNull String aggregateRoot) {
        return LockUtil.syncLockSubmit(aggregateRoot, () -> {
            Map<String, String> map = this.sqlClient.redisCache.getMap(aggregateRoot);
            if (map.isEmpty()) {
                return null;
            }
            T tEntity = injectData(entityAttribute, map);
            cachingCaffeine(entityAttribute, aggregateRoot, tEntity);
            return tEntity;
        });
    }

    @Override
    public void cachingCaffeine(SQLClient.EntityAttributes entityAttribute, String aggregateRoot, Object entity) {
        List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
        for (String insertDiscreteRoot : insertDiscreteRoots) {
            caffeineAddDiscreteRoot(insertDiscreteRoot, entity);
        }
        this.caffeineCache.put(aggregateRoot, entity);
        this.injectForeignEntities(entity);
        //关联实体对查询外玩家名会导致递归循环
    }

    public <T> List<T> find(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if (aggregateRoot == null) {
            String discreteRoot = entityAttribute.getDiscreteRoot(whereEntity);
            Object o = this.caffeineCache.get(discreteRoot);
            if (o != null) {
                //可以在这里增加锁算法，但是会降低效率
                return o == CacheImplUtil.emptyObject ? null : (List<T>) o;
            }
            List<String> aggregateRoots = this.sqlClient.redisCache.getList(discreteRoot);
            if (aggregateRoots.isEmpty()) {
                List<T> tlist = this.sqlClient.sqlQuery.directFindByIds(whereEntity);
                for (T tEntity : tlist) {
                    String loopAggregateRoot = entityAttribute.getAggregateRoot(tEntity);
                    LockUtil.syncLock(loopAggregateRoot, () -> cachingCaffeine(entityAttribute, loopAggregateRoot, tEntity));
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
            Object entityFromCaffeine = this.caffeineCache.get(aggregateRoot);
            if (entityFromCaffeine != null) {
                return entityFromCaffeine == CacheImplUtil.emptyObject ? null : Collections.singletonList((T) entityFromCaffeine);
            }
            Object entityFromRedis = getEntityFromRedis(entityAttribute, aggregateRoot);
            if (entityFromRedis != null) {
                return Collections.singletonList((T) entityFromRedis);
            }
            List<Object> entityFromMySQLByAggregateRoot = getEntityFromMySQLByAggregateRoot(entityAttribute, aggregateRoot);
            if (entityFromMySQLByAggregateRoot != null) {
                return (List<T>) entityFromMySQLByAggregateRoot;
            }
        }
        return null;
    }

    @Override
    public boolean hasApplyOverride() {
        return true;
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
                LockUtil.syncLock(aggregateRoot, () -> this.caffeineCache.put(aggregateRoot, CacheImplUtil.emptyObject));
                LockUtil.asyncLock(aggregateRoot, () -> this.sqlClient.redisCache.putMap(aggregateRoot, CacheImplUtil.map));
            }
            List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(whereEntity);
            for (String discreteRoot : insertDiscreteRoots) {
                this.caffeineCache.put(discreteRoot, CacheImplUtil.emptyObject);
                this.sqlClient.redisCache.setList(discreteRoot, CacheImplUtil.list);
            }
            return null;
        }
        for (T tEntity : tEntities) {
            String loopAggregateRoot = entityAttribute.getAggregateRoot(tEntity);
            LockUtil.syncLock(loopAggregateRoot, () -> cachingCaffeine(entityAttribute, loopAggregateRoot, tEntity));
            LockUtil.asyncLock(loopAggregateRoot, () -> cachingRedis(entityAttribute, loopAggregateRoot, tEntity));
        }
        return tEntities;
    }

    @Override
    public <T> void caffeineAddDiscreteRoot(String discreteRoot, T t) {
        Object o1 = this.caffeineCache.get(discreteRoot);
        if (o1 != null) {
            List list = (List) o1;
            if (!list.contains(t)) {
                list.add(t);
            }
        } else {
            this.caffeineCache.put(discreteRoot, new ArrayList<>(Collections.singletonList(t)));
        }
    }

    //将变更应用到缓存区
    public <T> boolean apply(T updateEntity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(updateEntity.getClass());
            String aggregateRoot = entityAttribute.getAggregateRoot(updateEntity);
            if(aggregateRoot != null) {
                LockUtil.syncLock(aggregateRoot, () -> {
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(updateEntity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        caffeineAddDiscreteRoot(insertDiscreteRoot, updateEntity);
                    }
                    this.caffeineCache.put(aggregateRoot, updateEntity);
                });
            }else{
                this.insert(updateEntity);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //第一个实体用来传递需要改变的参数，第二个用来表示查询参数
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
                });
            }
//            for (T entity : entities) {
//                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
//                LockUtil.asyncLock(aggregateRoot, () -> {
//                    this.sqlClient.redisCache.putMap(aggregateRoot, FastCacheUtil.serializeObject(entity));
//                    this.sqlClient.sqlUpdate.updateNotCache(updateEntity, anchorEntity);
//                });
//            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> boolean delete(T whereEntity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
            List<T> entities = find(whereEntity);
            for (T entity : entities) {
                String aggregateRoot = entityAttribute.getAggregateRoot(entity);
                LockUtil.syncLock(aggregateRoot, () -> {
                    cascadingDelete(entityAttribute, entity);
                    this.caffeineCache.del(aggregateRoot);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.caffeineCache.del(insertDiscreteRoot);
                    }
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    this.sqlClient.redisCache.del(aggregateRoot);
                    List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.delListElement(insertDiscreteRoot, aggregateRoot);
                    }
                    this.sqlClient.sqlDelete.deleteNotCache(entityAttribute.getClazz(), entity);
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> boolean insert(T entity) {
        try {
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
            String aggregateRoot = entityAttribute.getAggregateRoot(entity);
            if (aggregateRoot == null) {
                List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
                long id = sqlClient.sqlInsert.directInsertObject(entity);
                String myAggregateRoot = entityAttribute.getAggregateRootById(id);
                LockUtil.syncLock(myAggregateRoot, () -> {
                    initialEmbeddedObject(entityAttribute, entity, id);
                    entityAttribute.getFieldAccess().set(entity, entityAttribute.getIdFieldName(), id);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        caffeineAddDiscreteRoot(insertDiscreteRoot, entity);
                    }
                    this.caffeineCache.put(myAggregateRoot, entity);
                });
                LockUtil.asyncLock(myAggregateRoot, () -> {
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
                    this.caffeineCache.put(aggregateRoot, entity);
                });
                LockUtil.asyncLock(aggregateRoot, () -> {
                    HashMap<String, String> serializedObject = serializeObject(entity);
                    this.sqlClient.redisCache.putMap(aggregateRoot, serializedObject);
                    for (String insertDiscreteRoot : insertDiscreteRoots) {
                        this.sqlClient.redisCache.pushUnduplicateList(insertDiscreteRoot, aggregateRoot);
                    }
                    sqlClient.sqlInsert.directInsertObject(entity);
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //独有数据缓存：
    //有更新后标记玩家，存入缓存后每隔一段时间就把数据写入到数据库
    //玩家退出后写入一次数据库，写入完毕后删除掉缓存，删除标记
    public static class ExclusiveCaffeineCacheImpl extends CaffeineCacheImpl {
        public ExclusiveCacheImpl exclusiveCache;
        //用来标记更新
        public ConcurrentHashMap<String, Set<String>> playerWaitUpdateAggregateMap = new ConcurrentHashMap<>();
        //用来级联删除缓存
        public ConcurrentHashMap<String,Map<String,Object>> playerRootMap = new ConcurrentHashMap<>();

        public ExclusiveCaffeineCacheImpl(ExclusiveCacheImpl exclusiveCache) {
            super(exclusiveCache.sqlClient);
            this.exclusiveCache = exclusiveCache;
            this.startCopyLoop();
        }

        public void startCopyLoop() {
            Bukkit.getScheduler().runTaskTimer(AoitoriProject.plugin, () -> {
                Iterator<Map.Entry<String, Set<String>>> iterator = playerWaitUpdateAggregateMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Set<String>> next = iterator.next();
                    String playerName = next.getKey();
                    updatePlayerCache(playerName,true);
                    iterator.remove();
                }
            }, 20 * 60 * 5, 20 * 60 * 5);
        }

        private void updatePlayerCache(String playerName,boolean removeCache) {
            try (Jedis connection = sqlClient.redis.getConnection()) {
                String serverId = connection.hget(EXCLUSIVE_DATABASE, playerName);
                if (serverId != null && !serverId.equals(DatabaseProperties.cache.zeromq$serverId)) {
                    Bukkit.getPlayer(playerName).kickPlayer("你的独有数据被其它服务器持有，请等待释放后重新进入，至多需要五分钟。");
                    return;
                }
                connection.hset(EXCLUSIVE_DATABASE, playerName, DatabaseProperties.cache.zeromq$serverId);
                connection.expire(playerName, 5*60+10);
            }catch (Exception e){
                e.printStackTrace();
            }
            Set<String> strings = playerWaitUpdateAggregateMap.get(playerName);
            if(strings == null || strings.isEmpty()){
                return;
            }
            strings = new LinkedHashSet<>(strings);
            Set<String> finalStrings = strings;
            Bukkit.getScheduler().runTaskAsynchronously(AoitoriProject.plugin, () -> {
                Iterator<String> iteratorList = finalStrings.iterator();
                while (iteratorList.hasNext()) {
                    try {
                        String aggregateRoot = iteratorList.next();
                        Object o = super.get(aggregateRoot);
                        if (o == null) {
                            iteratorList.remove();
                            continue;
                        }
                        if (o != CacheImplUtil.emptyObject) {
                            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(o.getClass());
                            LockUtil.syncLock(aggregateRoot, () -> {
                                exclusiveCache.cachingRedis(entityAttribute, aggregateRoot, o);
                                List<Object> objects = this.sqlClient.sqlQuery.directFindByIds(o);
                                if (objects.isEmpty()) {
                                    sqlClient.sqlInsert.directInsertObject(o);
                                } else {
                                    sqlClient.sqlUpdate.updateNotCache(o, o);
                                }
                                exclusiveCache.updateForeignObject(o);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void removeCache(String playerName){
            playerWaitUpdateAggregateMap.remove(playerName);
            Map<String, Object> stringObjectMap = playerRootMap.get(playerName);
            for (Map.Entry<String, Object> discreteMap : stringObjectMap.entrySet()) {
                String key = discreteMap.getKey();
                if (key.charAt(0) == '$') {
                    directDel(key);
                }else if(key.charAt(0) == '%'){
                    Object value = discreteMap.getValue();
                    List list = (List) super.get(key);
                    list.remove(value);
                    if(list.isEmpty()){
                        directDel(key);
                    }
                }
            }

        }

        public synchronized void savePlayerAllData(String playerName) {
            LockUtil.syncLock(playerName, () -> {
                updatePlayerCache(playerName,true);
                removeCache(playerName);
            });
        }

        @Override
        public void put(@NotNull String key,@NotNull Object o) {
            if(o instanceof List){
                List list = (List) (List) o;
                for (Object object : list) {
                    SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(object.getClass());
                    String playerName = entityAttribute.getPlayerName(object);
                    if (playerName == null) {
                        throw new RuntimeException("使用 PLAYER_EXCLUSIVE_DATA 实体类型数据时，插入时必须保证有玩家名");
                    }
                    playerRootMap.computeIfAbsent(playerName, k -> new HashMap<>()).put(key,o);
                }
            }else {
                SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(o.getClass());
                String playerName = entityAttribute.getPlayerName(o);
                if (playerName == null) {
                    throw new RuntimeException("使用 PLAYER_EXCLUSIVE_DATA 实体类型数据时，插入时必须保证有玩家名");
                }
                if (key.charAt(0) == '$') {
                    playerWaitUpdateAggregateMap.computeIfAbsent(playerName, k -> new LinkedHashSet<>()).add(key);
                }
                playerRootMap.computeIfAbsent(playerName, k -> new HashMap<>()).put(key,o);
            }
            super.put(key, o);
        }

        public void directDel(String key){
            super.del(key);
        }

        @Override
        public void del(@NotNull String key) {
            Object o = super.get(key);
            if (o == null) {
                super.del(key);
                return;
            }
            //级联删除玩家键名映射表，可能会导致内存泄漏
            if(o instanceof List){
                List list = (List) (List) o;
                for (Object object : list) {
                    SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(object.getClass());
                    String playerName = entityAttribute.getPlayerName(object);
                    playerRootMap.computeIfAbsent(playerName, k -> new HashMap<>()).remove(key);
                }
            }else{
                SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(o.getClass());
                String playerName = entityAttribute.getPlayerName(o);
                playerWaitUpdateAggregateMap.computeIfAbsent(playerName, k -> new LinkedHashSet<>()).remove(key);
                playerRootMap.computeIfAbsent(playerName, k -> new HashMap<>()).remove(key);
            }
            super.del(key);
        }


    }

    public class ExclusiveCacheBukkitListener implements Listener {
        public ExclusiveCacheImpl exclusiveCache;
        //可以查，但是不能增删改
        public HashSet<String> playerLocks = new HashSet<>();

        public ExclusiveCacheBukkitListener(ExclusiveCacheImpl exclusiveCache) {
            this.exclusiveCache = exclusiveCache;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void whenPlayerJoinServer(AsyncPlayerPreLoginEvent playerPreLoginEvent) {
            PlayerPreLoginEvent.Result result = playerPreLoginEvent.getResult();
            if (result == ALLOWED) {
                String name = playerPreLoginEvent.getName();
                try (Jedis connection = sqlClient.redis.getConnection()) {
                    String serverId = connection.hget(EXCLUSIVE_DATABASE, name);
                    if (serverId != null && !serverId.equals(DatabaseProperties.cache.zeromq$serverId)) {
                        playerPreLoginEvent.disallow(KICK_OTHER, "你的独有数据被其它服务器持有，请等待释放后重新进入，至多需要五分钟。");
                        return;
                    }
                    connection.hset(EXCLUSIVE_DATABASE, name, DatabaseProperties.cache.zeromq$serverId);
                    connection.expire(name, 5*60+10);
                }
                playerLocks.add(name);
                //推送锁信息，同时在redis写入锁信息
            }
        }

        @EventHandler
        public void whenPlayerQuitSever(PlayerQuitEvent event) {
            String name = event.getPlayer().getName();
            exclusiveCache.caffeineCache.savePlayerAllData(name);
            playerLocks.remove(name);
            try (Jedis connection = sqlClient.redis.getConnection()) {
                String serverId = connection.hget(EXCLUSIVE_DATABASE, name);
                if (serverId.equals(DatabaseProperties.cache.zeromq$serverId)) {
                    connection.hdel(EXCLUSIVE_DATABASE, name);
                }
            }
            PluginProvider.getJavaPlugin().getLogger().info("保存玩家 "+name+" 数据成功");
        }
    }


}
