package io.aoitori043.aoitoriproject.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * @Author: natsumi
 * @CreateTime: 2023-09-18  16:25
 * @Description: ?
 */
public class PlaceholderAPIUtil {

    public static String throughPAPI(Player player, String arg) {
        if (player == null) {
            return arg;
        }
        return PlaceholderAPI.setPlaceholders(player, arg);
    }
}
