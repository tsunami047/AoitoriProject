package io.aoitori043.aoitoriproject.thread;

import kilim.Pausable;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-24  21:04
 * @Description: ?
 */
@FunctionalInterface
public interface KilimRunnable {
    public abstract void run() throws Pausable, Exception;
}
