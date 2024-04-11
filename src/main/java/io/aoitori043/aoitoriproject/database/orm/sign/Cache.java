package io.aoitori043.aoitoriproject.database.orm.sign;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.aoitori043.aoitoriproject.database.orm.sign.Cache.CacheType.PLAYER_EXCLUSIVE_DATA;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-08  00:12
 * @Description: ?
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    enum CacheType{
        PLAYER_EXCLUSIVE_DATA, //只有玩家自己可以消费的数据，使用全部缓存功能，
        // 给别的服务器上锁，在退出的时候以及定时保存数据，只在一个服务器上操作，不使用redis分布式锁
        // 使用场景：很多，只要是存储玩家专属数据都可以用这个
        HIGH_VALUE_DATA, //高价值数据，不存caffeine，只存取redis和mysql，提供redis分布式锁用于提交事务,
        // 增删改中使用redis分布锁可以保证100%一致性，使用场景：公会、全球市场，会被多个节点同时访问读写场景
        FAST, //只使用redis和caffeine存储数据，适合存储点数类型的数值，蓝量，血量之类的
        ONLY_MYSQL, //只使用mysql存储数据
        ONLY_REDIS //只使用redis存储数据，可以持久化存储
    }
    CacheType cacheType() default PLAYER_EXCLUSIVE_DATA;


}
