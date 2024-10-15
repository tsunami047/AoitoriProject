package io.aoitori043.aoitoriproject.op;

import com.google.gson.Gson;
import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.PluginProvider;
import net.minecraft.server.v1_12_R1.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-10  23:45
 * @Description: 通过反射方法高效进行越权执行指令
 */
public class UnauthCommandHandler implements Listener {

//    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    public volatile static ConcurrentHashMap<Player, OPCommandExecutor> executor = new ConcurrentHashMap<>();

    public synchronized static void addTask(Player player, List<String> commands) {
        AoitoriProject.plugin.getLogger().warning("废弃");
//        executorService.execute(() -> {
//            OPCommandExecutor opCommandExecutor;
//            if (executor.containsKey(player)) {
//                opCommandExecutor = executor.get(player);
//            } else {
//                opCommandExecutor = new OPCommandExecutor(player);
//                executor.put(player, opCommandExecutor);
//            }
//            try {
//                opCommandExecutor.addTask(commands);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        //插件执行的方法是不会触发这个方法的
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player.isOp() && !BukkitReflectionUtils.oplist.contains(player.getName())) {
            message = message.replace("/","");
            String[] a = message.split(" ");
            for (String[] b : BukkitReflectionUtils.safeCommand) {
                if(a.length==b.length){
                    boolean safe = true;
                    for (int i = 0; i < a.length; i++) {
                        if (!b[i].equalsIgnoreCase("*") && !a[i].equalsIgnoreCase(b[i])) {
                            safe = false;
                            break;
                        }
                    }
                    if(safe){
                        return;
                    }
                }
            }
            System.out.printf(player.getName() + "的指令" + message + "被拦截，试图以OP权限执行没有允许的指令");
            event.setCancelled(true);
        }
        if (message.contains("op")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(PluginProvider.getJavaPlugin(),() -> {
                try {
                    File c = PlayerList.c;
                    BukkitReflectionUtils.PlayerOPList[] playerOPLists = new Gson().fromJson(BukkitReflectionUtils.readFileToString(c), BukkitReflectionUtils.PlayerOPList[].class);
                    BukkitReflectionUtils.oplist = Arrays.stream(playerOPLists).map(BukkitReflectionUtils.PlayerOPList::getName).collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },20L*2);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        String[] s = event.getCommand().split(" ");
        if (s[0].equalsIgnoreCase("stop")) {
            return;
        }
        if (s[0].contains("op")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(PluginProvider.getJavaPlugin(),() -> {
                try {
                    File c = PlayerList.c;
                    BukkitReflectionUtils.PlayerOPList[] playerOPLists = new Gson().fromJson(BukkitReflectionUtils.readFileToString(c), BukkitReflectionUtils.PlayerOPList[].class);
                    BukkitReflectionUtils.oplist = Arrays.stream(playerOPLists).map(BukkitReflectionUtils.PlayerOPList::getName).collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },20L*2);
        }
    }

}
