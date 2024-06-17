package io.aoitori043.aoitoriproject.database.orm.impl;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.CaffeineCacheImpl;
import lombok.Data;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.temporal.ChronoUnit.HOURS;

//玩家退出服务器给几秒钟进行保存数据，在此之前不能进入新的服务器
@ToString
public class ExclusiveCacheImpl extends CacheImpl {


    public final ConcurrentHashMap<String, Set<CacheWrapper>> playerNameCache = new ConcurrentHashMap<>();
    public final CaffeineCacheImpl<CacheWrapper> aggregateRootCacheImpl = new CaffeineCacheImpl<CacheWrapper>(1000, Duration.of(5, HOURS));
    public final CaffeineCacheImpl<Set<String>> discreteRootCacheImpl = new CaffeineCacheImpl<Set<String>>(10000, Duration.of(5, HOURS));
    public ExclusiveCacheImpl(SQLClient sqlClient) {
        super(sqlClient);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), AoitoriProject.plugin);
    }

    //删除了游离缓存，直接用内建缓存
    @Override
    public <T> boolean apply(T entity){
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
        List<T> tEntities = find(entity);
        if (tEntities == null || tEntities.isEmpty() || entityAttribute.getAggregateRoot(entity)==null) {
            insert(entity);
        } else {
            deleteCache(entity, entityAttribute);
            interceptIllegalModify(entity, entityAttribute);
            sqlClient.sqlUpdate.absoluteUpdate(entity, entity);
            cache(entity, entityAttribute);
        }
        return true;
    }

    //删除了游离缓存，但是tns一直用着原来的,没有更新内建缓存
    @Override
    public <T> boolean update(T updateEntity, T anchorEntity, UpdateType updateType) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(anchorEntity.getClass());
        interceptIllegalModify(anchorEntity, entityAttribute);
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
        if (aggregateRoot != null) {
            CacheWrapper cacheWrapper = aggregateRootCacheImpl.get(aggregateRoot);
            deleteCacheByWrapper(cacheWrapper);
            Set<CacheWrapper> cacheWrappers = playerNameCache.get(cacheWrapper.getPlayerName());
            cacheWrappers.remove(cacheWrapper);
            return true;
        }
        return false;
    }

    /*
    清除聚合和游离根缓存
     */
    private void deleteCacheByWrapper(CacheWrapper cacheWrapper) {
        String aggregateRoot = cacheWrapper.getAggregateRoot();
        aggregateRootCacheImpl.del(aggregateRoot);
        for (String discreteRoot : cacheWrapper.getDiscreteRootCache()) {
            Set<String> aggregateRoots = discreteRootCacheImpl.get(discreteRoot);
            if (aggregateRoots != null) {
                aggregateRoots.remove(aggregateRoot);
                if (aggregateRoots.isEmpty()) {
                    discreteRootCacheImpl.del(discreteRoot);
                }
            }else{
                System.out.println("缓存不正确映射： "+discreteRoot);
            }
        }
    }

    @Override
    public <T> boolean delete(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        interceptIllegalModify(whereEntity, entityAttribute);
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
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
        interceptIllegalModify(entity, entityAttribute);
        long id = sqlClient.sqlInsert.directInsertObject(entity);
        initialEmbeddedObject(entityAttribute, entity, id);
        entityAttribute.setId(entity, id);
        //缓存
        cache(entity, entityAttribute);
        return true;
    }

    private <T> void interceptIllegalModify(T entity, SQLClient.EntityAttributes entityAttribute) {
        String playerName = entityAttribute.getPlayerName(entity);
        if(playerName != null && !playerNameCache.containsKey(playerName)){
            throw new NullPointerException("访问拒绝，试图在 "+playerName+" 玩家不在线时操作其独有型数据："+ entity);
        }
    }

    @Override
    public String toString() {
        return "ExclusiveCacheImpl{" + "playerNameCache=" + playerNameCache + ", aggregateRootCache=" + aggregateRootCacheImpl.getCache().asMap() + ", discreteRootCache=" + discreteRootCacheImpl.getCache().asMap() + "} " + super.toString();
    }

    private <T> boolean cache(T entity, SQLClient.EntityAttributes entityAttribute) {
        String aggregateRoot = entityAttribute.getAggregateRoot(entity);
        List<String> insertDiscreteRoots = entityAttribute.getInsertDiscreteRoots(entity);
        try {
            CacheWrapper cacheWrapper = new CacheWrapper(entity, aggregateRoot, insertDiscreteRoots);
            //如果抛出错误的话是无法进入缓存的
            aggregateRootCacheImpl.put(aggregateRoot, cacheWrapper);
            for (String insertDiscreteRoot : insertDiscreteRoots) {
                Set<String> discreteRootContainAggregateRoot = null;
                synchronized (ExclusiveCacheImpl.class) {
                    discreteRootContainAggregateRoot = discreteRootCacheImpl.get(insertDiscreteRoot);
                    if (discreteRootContainAggregateRoot == null) {
                        discreteRootContainAggregateRoot = new HashSet<>();
                        discreteRootCacheImpl.put(insertDiscreteRoot, discreteRootContainAggregateRoot);
                    }
                }
                discreteRootContainAggregateRoot.add(aggregateRoot);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> List<T> find(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if (aggregateRoot != null) {
            CacheWrapper cacheWrapper = aggregateRootCacheImpl.get(aggregateRoot);
            if (cacheWrapper != null) {
                return Collections.singletonList((T) cacheWrapper.getEntity());
            }
        } else {
            String discreteRoot = entityAttribute.getDiscreteRoot(whereEntity);
            Set<String> aggregateRoots = discreteRootCacheImpl.get(discreteRoot);
            if (aggregateRoots != null) {
                List<T> result = new ArrayList<>();
                Iterator<String> iterator = aggregateRoots.iterator();
                boolean notfound = false;
                while (iterator.hasNext()) {
                    String root = iterator.next();
                    CacheWrapper cacheWrapper = aggregateRootCacheImpl.get(root);
                    if (cacheWrapper == null) {
                        notfound = true;
                        iterator.remove();
                    } else {
                        result.add((T) cacheWrapper.getEntity());
                    }
                }
                //如果遇到用离散根也查询不到聚合根，就重新查询，存在缓存删不干净的情况！（？）这是为什么
                if (notfound) {
                    if (aggregateRoots.isEmpty()) {
                        discreteRootCacheImpl.del(discreteRoot);
                        System.out.println("游离根查询的聚合根未缓存： "+discreteRoot);
                    }
                    return find(whereEntity);
                }
                return result;
            }
        }
        //在缓存中查询不到对象
        List<T> tEntities = sqlClient.sqlQuery.directFindByIds(whereEntity);
        for (T tEntity : tEntities) {
            //查询结果无法入缓，玩家不在线，查询结果返回空
            if (!cache(tEntity, entityAttribute)) {
                return null;
            }
            injectForeignEntities(tEntity);
        }
        return tEntities;
    }

    @Override
    public boolean hasApplyOverride() {
        return true;
    }

    //如果是共享的数据，则使用redis（高频查询）/mysql（低频查询），玩家独占型数据，使用mysql+caffeine缓存淘汰机制同步数据
    //线程安全的办法：在一个单线程中，阻塞的执行每个任务
    //离散根的生成条件：1 作为Key 2外聚合根 3作为索引
    //每次修改都直接删除所有缓存
    @Data
    public class CacheWrapper {
        Object entity;
        String aggregateRoot;
        List<String> discreteRootCache;
        String playerName;

        public CacheWrapper(Object entity, String aggregateRoot, List<String> discreteRootCache) throws IllegalAccessException {
            this.entity = entity;
            this.aggregateRoot = aggregateRoot;
            this.discreteRootCache = discreteRootCache;
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
            playerName = entityAttribute.getPlayerName(entity);
            try {
                playerNameCache.get(playerName).add(this);
                //巧妙的设计，防止在玩家不在线的时候进行缓存
            }catch (Exception e){
                    throw new IllegalAccessException("非法访问: " + playerName + " 玩家不在此服务器，但是却试图修改其独有型数据。");
            }

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
        @EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
        public void whenPlayerJoinServer(PlayerJoinEvent e) {
            playerNameCache.put(e.getPlayer().getName(), new HashSet<>());
        }

        @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
        public void whenPlayerQuitServer(PlayerQuitEvent e) {
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

}
