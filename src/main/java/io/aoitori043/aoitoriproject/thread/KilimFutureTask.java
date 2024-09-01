package io.aoitori043.aoitoriproject.thread;

import io.netty.util.concurrent.Promise;
import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-30  03:33
 * @Description: ?
 */
public class KilimFutureTask extends Task {

    private KilimFutureRunnable runnable;
    public KilimFutureTask(Mailbox resultMailbox,KilimFutureRunnable runnable) {
        this.runnable = runnable;
        this.resultMailbox = resultMailbox;
    }

    private Mailbox<Object> resultMailbox;

    @Override
    public void execute() throws Pausable {
        Object run = runnable.run();
        resultMailbox.put(run);
    }

}
