package io.aoitori043.aoitoriproject.thread;

import kilim.Pausable;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-30  03:37
 * @Description: ?
 */
@FunctionalInterface
public interface KilimFutureRunnable<T>  {
     Object run() throws Pausable;
}
