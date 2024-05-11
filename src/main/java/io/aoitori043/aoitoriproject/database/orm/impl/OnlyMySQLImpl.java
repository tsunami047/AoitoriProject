package io.aoitori043.aoitoriproject.database.orm.impl;

import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.semaphore.LockUtil;

import java.util.List;

public class OnlyMySQLImpl extends CacheImpl {

    public OnlyMySQLImpl(SQLClient sqlClient) {
        super(sqlClient);
    }


    @Override
    public <T> boolean update(T updateEntity, T anchorEntity, CacheImpl.UpdateType updateType) {
        sqlClient.sqlUpdate.updateNotCache(updateEntity,anchorEntity);
        return true;
    }

    @Override
    public <T> boolean delete(T whereEntity) {
        SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(whereEntity.getClass());
        String aggregateRoot = entityAttribute.getAggregateRoot(whereEntity);
        if(aggregateRoot == null) {
            for (T tEntity : this.sqlClient.query(whereEntity)) {
                aggregateRoot = entityAttribute.getAggregateRoot(tEntity);
                LockUtil.asyncLock(aggregateRoot,()->{
                    cascadingDelete(entityAttribute, tEntity);
                    sqlClient.sqlDelete.deleteNotCache((Class<T>) whereEntity.getClass(), tEntity);
                });
            }
        }else {
            LockUtil.asyncLock(aggregateRoot, () -> {
                cascadingDelete(entityAttribute, whereEntity);
                sqlClient.sqlDelete.deleteNotCache((Class<T>) whereEntity.getClass(), whereEntity);
            });
        }
        return true;
    }

    @Override
    public <T> boolean insert(T entity) {
        long id = sqlClient.sqlInsert.directInsertObject(entity);
        SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(entity.getClass());
        initialEmbeddedObject(entityAttribute, entity, id);
        return true;
    }

    @Override
    public <T> List<T> find(T whereEntity) {
        List<T> tEntities = sqlClient.sqlQuery.directFindByIds(whereEntity);
        for (T tEntity : tEntities) {
            this.injectForeignEntities(tEntity);
        }
        return tEntities;
    }

    @Override
    public boolean hasApplyOverride() {
        return false;
    }
}
