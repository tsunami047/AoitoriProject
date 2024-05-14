package io.aoitori043.aoitoriproject.database.orm.impl;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.CaffeineCacheImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.temporal.ChronoUnit.HOURS;

@ToString
public class ExclusiveCacheImpl extends CacheImpl {


    //如果是共享的数据，则使用redis（高频查询）/mysql（低频查询），玩家独占型数据，使用mysql+caffeine缓存淘汰机制同步数据
    //线程安全的办法：在一个单线程中，阻塞的执行每个任务
    //离散根的生成条件：1 作为Key 2外聚合根 3作为索引
    //每次修改都直接删除所有缓存
    @Data
    public class CacheWrapper{
        Object entity;
        String aggregateRoot;
        List<String> discreteRootCache;
        String playerName;

        public CacheWrapper(Object entity, String aggregateRoot, List<String> discreteRootCache){
            this.entity = entity;
            this.aggregateRoot = aggregateRoot;
            this.discreteRootCache = discreteRootCache;
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
            playerName = entityAttribute.getPlayerName(entity);
            playerNameCache.get(playerName).add(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheWrapper)) return false;
            CacheWrapper that = (CacheWrapper) o;
            return Objects.equals(aggregateRoot, that.aggregateRoot);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(aggregateRoot);
        }
    }

    public class PlayerJoinQuitListener implements Listener {
        @EventHandler
        public void whenPlayerJoinServer(PlayerJoinEvent e){
            playerNameCache.put(e.getPlayer().getName(),new HashSet<>());
        }
        @EventHandler
        public void whenPlayerQuitServer(PlayerQuitEvent e){
            String name = e.getPlayer().getName();
            Set<CacheWrapper> cacheWrappers = playerNameCache.get(name);
            Iterator<CacheWrapper> iterator = cacheWrappers.iterator();
            while (iterator.hasNext()) {
                CacheWrapper cacheWrapper = iterator.next();
                deleteCacheByWrapper(cacheWrapper);
                iterator.remove();
            }
            playerNameCache.remove(name);
        }
    }

    public final ConcurrentHashMap<String,Set<CacheWrapper>> playerNameCache = new ConcurrentHashMap<>();
    public final CaffeineCacheImpl<CacheWrapper> aggregateRootCache = new CaffeineCacheImpl<CacheWrapper>(1000, Duration.of(5, HOURS));
    public final CaffeineCacheImpl<Set<String>> discreteRootCache = new CaffeineCacheImpl<Set<String>>(10000, Duration.of(5, HOURS));
    public ExclusiveCacheImpl(SQLClient sqlClient) {
        super(sqlClient);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), AoitoriProject.plugin);
    }

    @Override
    public <T> boolean update(T updateEntity, T anchorEntity, UpdateType updateType) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(anchorEntity.getClass());
        //删除离散缓存，并且重新建立缓存
        deleteCache(anchorEntity, entityAttribute);
        sqlClient.sqlUpdate.absoluteUpdate(updateEntity, anchorEntity);
        find(anchorEntity);
        return true;
    }

    private <T> void deleteCache(T tEntity, SQLClient.EntityAttributes entityAttribute) {
        if (deleteCacheIfAggregateRoot(tEntity, entityAttribute)) return;
        //无聚合根，则查询后删除
        List<T> tEntities = sqlClient.sqlQuery.directFindByIds(tEntity);
        for (T loopTEntity : tEntities) {
            deleteCacheIfAggregateRoot(loopTEntity, entityAttribute);
        }
    }

    private <T> boolean deleteCacheIfAggregateRoot(T tEntity, SQLClient.EntityAttributes entityAttribute) {
        String aggregateRoot = entityAttribute.getAggregateRoot(tEntity);
        if(aggregateRoot != null){
            CacheWrapper cacheWrapper = aggregateRootCache.get(aggregateRoot);
            deleteCacheByWrapper(cacheWrapper);
            Set<CacheWrapper> cacheWrappers = playerNameCache.get(cacheWrapper.getPlayerName());
            cacheWrappers.remove(cacheWrapper);
//            if (!cacheWrappers.remove(cacheWrapper)) {
//                throw new RuntimeException("无法级联删除玩家缓存记录");
//            }
            return true;
        }
        return false;
    }

    private void deleteCacheByWrapper(CacheWrapper cacheWrapper) {
        aggregateRootCache.del(cacheWrapper.getAggregateRoot());
        for (String discreteRoot : cacheWrapper.getDiscreteRootCache()) {
            Set<String> aggregateRoots = discreteRootCache.get(discreteRoot);
            if(aggregateRoots!=null){
                aggregateRoots.remove(discreteRoot);
                if(aggregateRoots.isEmpty()){
                    discreteRootCache.del(discreteRoot);
                }
            }
        }
    }

    @Override
    public <T> boolean delete(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        List<T> tEntities = sqlClient.sqlQuery.directFindByIds(whereEntity);
        for (T tEntity : tEntities) {
            cascadingDelete(entityAttribute, tEntity);
            deleteCache(tEntity, entityAttribute);
            sqlClient.sqlDelete.deleteNotCache((Class<T>) whereEntity.getClass(), tEntity);
        }
        return true;
    }


    @Override
    public <T> boolean insert(T entity) {
        long id = sqlClient.sqlInsert.directInsertObject(entity);
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
        initialEmbeddedObject(entityAttribute, entity, id);
        entityAttribute.setId(entity, id);
        //缓存
        cache(entity, entityAttribute);
        return true;
    }

    private <T> void cache(T entity, SQLClient.EntityAttributes entityAttribute) {
        String aggregateRoot = entityAttribute.getAggregateRoot(entity);
        List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
        CacheWrapper cacheWrapper = new CacheWrapper(entity, aggregateRoot, insertDiscreteRoots);
        aggregateRootCache.put(aggregateRoot, cacheWrapper);
        for (String insertDiscreteRoot : insertDiscreteRoots) {
            Set<String> discreteRoots = discreteRootCache.get(insertDiscreteRoot);
            if (discreteRoots == null) {
                discreteRoots = new HashSet<>();
                discreteRootCache.put(insertDiscreteRoot, discreteRoots);
            }
            discreteRoots.add(aggregateRoot);
        }
    }

    @Override
    public <T> List<T> find(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if (aggregateRoot != null) {
            CacheWrapper cacheWrapper = aggregateRootCache.get(aggregateRoot);
            if (cacheWrapper != null) {
                return Collections.singletonList((T) cacheWrapper.getEntity());
            }
        } else {
            String discreteRoot = entityAttribute.getDiscreteRoot(whereEntity);
            Set<String> aggregateRoots = discreteRootCache.get(discreteRoot);
            if (aggregateRoots != null) {
                List<T> result = new ArrayList<>();
                Iterator<String> iterator = aggregateRoots.iterator();
                boolean notfound = false;
                while (iterator.hasNext()) {
                    String root = iterator.next();
                    CacheWrapper cacheWrapper = aggregateRootCache.get(root);
                    if (cacheWrapper == null) {
                        notfound = true;
                        iterator.remove();
                    } else {
                        result.add((T) cacheWrapper.getEntity());
                    }
                }
                //如果遇到用离散根也查询不到聚合根，就重新查询
                if(notfound){
                    if(aggregateRoots.isEmpty()){
                        discreteRootCache.del(discreteRoot);
                    }
                    find(whereEntity);
                }
                return result;
            }
        }
        //在缓存中查询不到对象
        List<T> tEntities = sqlClient.sqlQuery.directFindByIds(whereEntity);
        for (T tEntity : tEntities) {
            cache(tEntity, entityAttribute);
            injectForeignEntities(tEntity);
        }
        return tEntities;
    }

    @Override
    public boolean hasApplyOverride() {
        return false;
    }

}
