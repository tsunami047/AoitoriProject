package io.aoitori043.aoitoriproject.thread;

import kilim.*;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-24  21:05
 * @Description: ?
 */
public class AoitoriScheduler {

    public static ConcurrentHashMap<String, Scheduler> map = new ConcurrentHashMap<>();

    public static void singleExecute(String symbol,KilimRunnable runnable) {
        KilimTask kilimTask = new KilimTask(runnable);
        kilimTask.setScheduler(map.computeIfAbsent(symbol, k -> new AffineScheduler(1, 0)));
        kilimTask.start();
    }

    public static Object singleFutureExecute(String symbol,KilimFutureRunnable runnable) {
        Mailbox<Object> mailbox = new Mailbox<>(1);
        KilimFutureTask kilimTask = new KilimFutureTask(mailbox,runnable);
        kilimTask.setScheduler(map.computeIfAbsent(symbol, k -> new AffineScheduler(1, 0)));
        kilimTask.start();
        return mailbox.getb();
    }

    public static void forkJoinExecute(KilimRunnable runnable) {
        new KilimTask(runnable).start();
    }

}
