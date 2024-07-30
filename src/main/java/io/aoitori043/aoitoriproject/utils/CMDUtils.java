package io.aoitori043.aoitoriproject.utils;

import io.aoitori043.aoitoriproject.AoitoriProject;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2023-08-19  21:32
 * @Description: ?
 */
public class CMDUtils {


    public static void performCmd(String cmd) {
        if (Bukkit.isPrimaryThread()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else {
            Bukkit.getScheduler().runTask(AoitoriProject.plugin, () -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            });
        }
    }

    public static void performCmd(List<String> cmds) {
        if (Bukkit.isPrimaryThread()) {
            for (String cmd : cmds) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        } else {
            Bukkit.getScheduler().runTask(AoitoriProject.plugin, () -> {
                for (String cmd : cmds) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            });
        }
    }
}
