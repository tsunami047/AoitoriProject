package io.aoitori043.aoitoriproject.script;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-06  20:44
 * @Description: ?
 */
public class PlaceholderHook extends PlaceholderExpansion {


    private final JavaPlugin plugin;

    public PlaceholderHook(JavaPlugin tSkills) {
        plugin = tSkills;
    }

    @Override
    public String getIdentifier() {
        return "aoitoriproject";
    }

    @Override
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        try {
            if (identifier.startsWith("var_")) {
                return TemporaryDataManager.getPlayerDataAccessor(player.getName()).getValue(identifier.substring(4)).toString();
            }
        } catch (Exception e) {

        }
        return null;
    }
}
