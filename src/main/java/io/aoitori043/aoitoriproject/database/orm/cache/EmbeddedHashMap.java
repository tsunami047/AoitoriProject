package io.aoitori043.aoitoriproject.database.orm.cache;

import io.aoitori043.aoitoriproject.CanaryClientImpl;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.util.HashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-10  23:21
 * @Description: ?
 */
public class EmbeddedHashMap<K,V> extends HashMap<K,V> {

    public Object superEntity;
    public int superEntityAggregateRoot;
    public HashMap<K,V> superMapCache = new HashMap();

    public EmbeddedHashMap(Object o) {
        this.superEntity = o;
        SQLClient.EntityAttributes entityAttribute = CanaryClientImpl.sqlClient.getEntityAttribute(o.getClass());
        superEntityAggregateRoot = entityAttribute.getDatabaseId(o);
    }

    public void directPut(K key, V value) {
        super.put(key, value);
    }

    public void completeSuperClassInsert(int superId){
        this.superEntityAggregateRoot = superId;
        if (superMapCache.isEmpty()) return;
        for (Entry<K, V> kvEntry : superMapCache.entrySet()) {
            V v = kvEntry.getValue();
            SQLClient.EntityAttributes entityAttribute = CanaryClientImpl.sqlClient.getEntityAttribute(v.getClass());
            entityAttribute.setForeignRootFieldValue(v,superEntity.getClass(),superEntityAggregateRoot);
            CanaryClientImpl.sqlClient.apply(v);
        }
        superMapCache.clear();
    }

    @Override
    public V put(K k, V v){
        if(superEntityAggregateRoot == -1){
            superMapCache.put(k,v);
            super.put(k,v);
            return v;
        }
        SQLClient.EntityAttributes entityAttribute = CanaryClientImpl.sqlClient.getEntityAttribute(v.getClass());
        entityAttribute.setForeignRootFieldValue(v,superEntity.getClass(),superEntityAggregateRoot);
        CanaryClientImpl.sqlClient.apply(v);
        super.put(k,v);
        //执行对应的方法
        return v;
    }

    @Override
    public V remove(Object k){
        V v = super.get(k);
        if(v == null){
            return null;
        }
        superMapCache.remove(k);
        CanaryClientImpl.sqlClient.delete(v);
        super.remove(k);
        return v;
    }
}
