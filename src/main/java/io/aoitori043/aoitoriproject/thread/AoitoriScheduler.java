package io.aoitori043.aoitoriproject.thread;

import kilim.AffineScheduler;
import kilim.Scheduler;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-24  21:05
 * @Description: ?
 */
public class AoitoriScheduler {

    public static HashMap<String, Scheduler> map = new HashMap<>();

    public static void singleExecute(String symbol,KilimRunnable runnable) {
        KilimTask kilimTask = new KilimTask(runnable);
        kilimTask.setScheduler(map.computeIfAbsent(symbol, k -> new AffineScheduler(1, 0)));
        kilimTask.start();
    }

    public static void forkJoinExecute(KilimRunnable runnable) {
        new KilimTask(runnable).start();
    }
}
