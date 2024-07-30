package io.aoitori043.aoitoriproject.script;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  21:27
 * @Description: ?
 */
public class TemporaryDataManager implements Listener {

    public static HashMap<String,PlayerDataAccessor> playerDataAccessors = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        playerDataAccessors.remove(player.getName());
    }

    public static PlayerDataAccessor getPlayerDataAccessor(String playerName) {
        return playerDataAccessors.computeIfAbsent(playerName,k->{
            PlayerDataAccessor playerDataAccessor = new PlayerDataAccessor(Bukkit.getPlayer(playerName));
            PlayerDataAccessor.VariablesAttribute variablesAttribute = new PlayerDataAccessor.VariablesAttribute();
            variablesAttribute.setValue(playerName);
            variablesAttribute.setType(PlayerDataAccessor.VariableType.TEXT);
            variablesAttribute.setInitValue(playerName);
            variablesAttribute.setVarName("player_name");
            playerDataAccessor.addVariable(variablesAttribute);
            return playerDataAccessor;
        });
    }
}
