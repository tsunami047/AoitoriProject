package io.aoitori043.aoitoriproject.database.orm.cache;

import io.aoitori043.aoitoriproject.CanaryClientImpl;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.database.orm.cache.impl.CacheImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
        if (super.containsKey(k)) {
            V v1 = superMapCache.get(k);
            CanaryClientImpl.sqlClient.update(v,v1, CacheImpl.UpdateType.NOT_COPY_NULL);
            List<V> query = CanaryClientImpl.sqlClient.query(v1);
            directPut(k,query.get(0));
            return query.get(0);
        }
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
    public void clear(){
        Iterator<Entry<K, V>> iterator = super.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<K, V> next = iterator.next();
            V v = next.getValue();
            if(v == null){
                continue;
            }
            K k = next.getKey();
            superMapCache.remove(k);
            CanaryClientImpl.sqlClient.delete(v);
            iterator.remove();
        }
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
