package io.aoitori043.aoitoriproject.utils;

import io.aoitori043.aoitoriproject.AoitoriProject;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @Author: natsumi
 * @CreateTime: 2023-08-19  21:32
 * @Description: ?
 */
public class CMDUtils {


    public static void performCmd(String cmd) {
        try {
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else {
                Bukkit.getScheduler().runTask(AoitoriProject.plugin, () -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void performCmd(String playerName,List<String> cmds) {
        try {
            List<String> myCommands = new ArrayList<>(cmds);
            myCommands.replaceAll(k -> k.replace("%player_name%", playerName));
            performCmd(myCommands);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void performCmd(String playerName, List<String> cmds, UnaryOperator<String> operator) {
        try {
            List<String> myCommands = new ArrayList<>(cmds);
            myCommands.replaceAll(k -> k.replace("%player_name%", playerName));
            myCommands.replaceAll(operator);
            performCmd(myCommands);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void performCmd(List<String> cmds) {
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
