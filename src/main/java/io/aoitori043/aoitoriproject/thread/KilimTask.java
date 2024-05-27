package io.aoitori043.aoitoriproject.thread;

import kilim.Pausable;
import kilim.Task;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-24  21:04
 * @Description: ?
 */
public class KilimTask extends Task {
    private KilimRunnable runnable;
    public KilimTask(KilimRunnable runnable) {
        this.runnable = runnable;
    }
    @Override
    public void execute() throws Pausable {
        try {
            runnable.run();
        }catch (Exception e){
            if(e instanceof Pausable){
                throw (Pausable)e;
            }else{
                e.printStackTrace();
            }
        }
    }
}
