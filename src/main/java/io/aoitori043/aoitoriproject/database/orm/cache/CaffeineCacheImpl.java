package io.aoitori043.aoitoriproject.database.orm.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import lombok.Getter;

import java.time.Duration;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-29  19:43
 * @Description: ?
 */
@Getter
public class CaffeineCacheImpl {

    public Cache<String, Object> cache;
    public SQLClient sqlClient;

    public CaffeineCacheImpl(SQLClient sqlClient)
    {
        this.sqlClient = sqlClient;
        cache = Caffeine.newBuilder().build();
    }

    public CaffeineCacheImpl(int maximumSize, Duration duration) {
        cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(duration)
                .recordStats()
                .build();
    }

    public void put(String key,Object o){
        cache.put(key,o);
    }

    public Object get(String key){
        return cache.getIfPresent(key);
    }

    public void del(String key){
        cache.invalidate(key);
    }

}
