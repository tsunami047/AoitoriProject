package io.aoitori043.aoitoriproject.database.orm.impl;

import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.util.Collections;
import java.util.List;

public class HighPerformanceImpl extends CacheImpl{
    public HighPerformanceImpl(SQLClient sqlClient) {
        super(sqlClient);
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
