package io.aoitori043.aoitoriproject.op;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-10  23:46
 * @Description: ?
 */
public class OPCommandExecutor {

    public OPCommandExecutor(Player player) {
        this.player = player;
    }

    private Player player;
    private ConcurrentLinkedQueue<List<String>> uchqueue = new ConcurrentLinkedQueue<>();


    public synchronized void addTask(List<String> poc) throws InterruptedException {
        uchqueue.offer(poc);
        notify(); // 通知等待的线程有新消息可用
        dequeue();
    }

    public volatile List<String> commands;

    public synchronized void dequeue() throws InterruptedException {
        while (uchqueue.isEmpty()) {
            wait(); // 等待新消息的到来
        }
        commands = uchqueue.poll();
            if(BukkitReflectionUtils.oplist.contains(player.getName())){
                for (String command : commands) player.performCommand(command);
                return;
            }
            BukkitReflectionUtils.setOp(player, true);
            try {
                for (String command : commands) player.performCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                BukkitReflectionUtils.setOp(player, false);
            }
    }


}
