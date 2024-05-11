package io.aoitori043.aoitoriproject.database.orm.impl;

import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.CaffeineCacheImpl;

import java.util.Collections;
import java.util.List;

public class HighPerformanceImpl extends CacheImpl{

    public static class HighPerformanceCaffeineCacheImpl extends CaffeineCacheImpl {
        public HighPerformanceImpl highPerformanceImpl;

        public HighPerformanceCaffeineCacheImpl(SQLClient sqlClient,HighPerformanceImpl highPerformanceImpl) {
            super(sqlClient);
            this.highPerformanceImpl = highPerformanceImpl;
        }
    }


    public final HighPerformanceImpl.HighPerformanceCaffeineCacheImpl caffeineCache;

    public HighPerformanceImpl(SQLClient sqlClient) {
        super(sqlClient);
        this.caffeineCache = new HighPerformanceImpl.HighPerformanceCaffeineCacheImpl(sqlClient,this);
    }

    @Override
    public <T> boolean update(T updateEntity, T anchorEntity, UpdateType updateType) {
        return false;
    }

    @Override
    public <T> boolean delete(T whereEntity) {
        return false;
    }

    @Override
    public <T> boolean insert(T entity) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entity.getClass());
        sqlClient.sqlInsert.directInsertObject(entity);
        String aggregateRoot = entityAttribute.getAggregateRoot(entity);
        return false;
    }

    @Override
    public <T> List<T> find(T whereEntity) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasApplyOverride() {
        return false;
    }
}
