package io.aoitori043.aoitoriproject.thread;

import kilim.AffineScheduler;
import kilim.Mailbox;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-30  03:46
 * @Description: ?
 */
public class KilimScheduler {

    public void singleExecute(String symbol, KilimRunnable runnable) {
        AoitoriScheduler.singleExecute(symbol, runnable);
    }

    public <T> T singleFutureExecute(String symbol,KilimFutureRunnable<T> runnable) {
        return (T)AoitoriScheduler.singleFutureExecute(symbol, runnable);
    }

    public void forkJoinExecute(KilimRunnable runnable) {
        AoitoriScheduler.forkJoinExecute(runnable);
    }

}
