package io.aoitori043.aoitoriproject.script;

import io.aoitori043.aoitoriproject.script.event.PlayerJoinServerEvent;
import io.aoitori043.aoitoriproject.script.event.PlayerQuitServerEvent;
import io.aoitori043.aoitoriproject.thread.AoitoriScheduler;
import io.aoitori043.syncdistribute.rmi.PlayerSyndAccess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  21:27
 * @Description: ?
 */
public class TemporaryDataManager implements Listener {

    public static ConcurrentHashMap<String,PlayerDataAccessor> playerDataAccessors = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerDataAccessor playerDataAccessor = createPlayerDataAccessor(event.getPlayer());
        playerDataAccessor.setPlayer(event.getPlayer());
        playerDataAccessors.put(event.getPlayer().getName(),playerDataAccessor);
        Bukkit.getPluginManager().callEvent(new AoitoriPlayerJoinEvent(event.getPlayer()));
//        PlayerJoinServerEvent.call(playerDataAccessor,new ConcurrentHashMap<>());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerDataAccessor playerDataAccessor = getPlayerDataAccessor(event.getPlayer());
        Bukkit.getPluginManager().callEvent(new AoitoriPlayerQuitEvent(event.getPlayer()));
        PlayerQuitServerEvent.call(playerDataAccessor, new ConcurrentHashMap<>());
        playerDataAccessors.remove(event.getPlayer().getName());
        PlayerSyndAccess.remove(event.getPlayer().getName());
    }


    public static PlayerDataAccessor getPlayerDataAccessor(Player player) {
        String playerName = player.getName();
        return getPlayerDataAccessor(playerName);
    }

    public static PlayerDataAccessor getPlayerDataAccessor(String playerName) {
        return playerDataAccessors.get(playerName);
//        return playerDataAccessors.computeIfAbsent(playerName,k->{
//            PlayerDataAccessor playerDataAccessor = createPlayerDataAccessor(playerName);
//            return playerDataAccessor;
//        });
    }

    private static @NotNull PlayerDataAccessor createPlayerDataAccessor(Player player) {
        String playerName = player.getName();
        PlayerDataAccessor playerDataAccessor = new PlayerDataAccessor(player);
        PlayerDataAccessor.VariablesAttribute variablesAttribute = new PlayerDataAccessor.VariablesAttribute();
        variablesAttribute.setValue(playerName);
        variablesAttribute.setType(PlayerDataAccessor.VariableType.STRING);
        variablesAttribute.setInitValue(playerName);
        variablesAttribute.setVarName("player_name");
        playerDataAccessor.addVariable(variablesAttribute);
        return playerDataAccessor;
    }
}
