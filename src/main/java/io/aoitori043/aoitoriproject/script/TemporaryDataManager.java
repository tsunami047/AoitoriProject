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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  21:27
 * @Description: ?
 */
public class TemporaryDataManager implements Listener {

    public static ConcurrentHashMap<String,PlayerDataAccessor> playerDataAccessors = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        AoitoriScheduler.singleExecute("joinandquit",()->{
            PlayerDataAccessor playerDataAccessor = getPlayerDataAccessor(event.getPlayer());
            playerDataAccessor.setPlayer(event.getPlayer());
            Bukkit.getPluginManager().callEvent(new AoitoriPlayerJoinEvent(event.getPlayer()));
            PlayerJoinServerEvent.call(playerDataAccessor,new ConcurrentHashMap<>());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        AoitoriScheduler.singleExecute("joinandquit",()-> {
            PlayerDataAccessor playerDataAccessor = getPlayerDataAccessor(event.getPlayer());
            Bukkit.getPluginManager().callEvent(new AoitoriPlayerQuitEvent(event.getPlayer()));
            PlayerQuitServerEvent.call(playerDataAccessor, new ConcurrentHashMap<>());
            playerDataAccessors.remove(event.getPlayer().getName());
            PlayerSyndAccess.persistentMap.remove(event.getPlayer().getName());
        });
    }


    public static PlayerDataAccessor getPlayerDataAccessor(Player player) {
        String playerName = player.getName();
        return getPlayerDataAccessor(playerName);
    }

    public static PlayerDataAccessor getPlayerDataAccessor(String playerName) {
        return playerDataAccessors.computeIfAbsent(playerName,k->{
            PlayerDataAccessor playerDataAccessor = new PlayerDataAccessor(Bukkit.getPlayer(playerName));
            PlayerDataAccessor.VariablesAttribute variablesAttribute = new PlayerDataAccessor.VariablesAttribute();
            variablesAttribute.setValue(playerName);
            variablesAttribute.setType(PlayerDataAccessor.VariableType.STRING);
            variablesAttribute.setInitValue(playerName);
            variablesAttribute.setVarName("player_name");
            playerDataAccessor.addVariable(variablesAttribute);
            return playerDataAccessor;
        });
    }
}
