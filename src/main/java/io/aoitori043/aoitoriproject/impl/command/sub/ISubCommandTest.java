package io.aoitori043.aoitoriproject.impl.command.sub;


import io.aoitori043.aoitoriproject.CanaryClientImpl;
import io.aoitori043.aoitoriproject.command.*;
import io.aoitori043.aoitoriproject.database.orm.impl.CacheImpl;
import io.aoitori043.aoitoriproject.database.orm.impl.ExclusiveCacheImpl;
import io.aoitori043.aoitoriproject.database.orm.sign.Cache;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-13  01:20
 * @Description: ?
 */
@SubArgument(
        argument = "test"
)
public class ISubCommandTest extends SubCommand {


    @TabCompletion(argument = "set")
    public List<String> getTabCompletion(int index) {
        return null;
    }


    @NotArgument(help = "测试")
    public void execute_test(CommandSender sender,List<ArgumentHelper> arguments) {
        CacheImpl cache = CanaryClientImpl.sqlClient.getCacheHashMap().get(Cache.CacheType.PLAYER_EXCLUSIVE_DATA);
        System.out.println(cache);
//        com.github.benmanes.caffeine.cache.Cache<String, Object> cache1 = ((ExclusiveCacheImpl) cache).getCaffeineCache().cache;
//        for (Map.Entry<String, Object> stringObjectEntry : cache1.asMap().entrySet()) {
//            System.out.println(stringObjectEntry.getKey() +" "+ stringObjectEntry.getValue());
//        }
    }

}
