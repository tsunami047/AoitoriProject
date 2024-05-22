package io.aoitori043.aoitoriproject;

import com.tuershen.nbtlibraryfix.NBTLibraryMain;
import io.aoitori043.aoitoriproject.command.BasicCommandExecute;
import io.aoitori043.aoitoriproject.database.DatabaseCenter;
import io.aoitori043.aoitoriproject.impl.HandlerInjection;
import io.aoitori043.aoitoriproject.impl.command.IBasicCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Properties;

public final class AoitoriProject extends JavaPlugin implements Listener {

    public static AoitoriProject plugin;

    @Override
    public void onEnable() {
        plugin = this;
        NBTLibraryMain.loadNBTLibrary(this);
        Bukkit.getPluginManager().registerEvents(this,this);
        BasicCommandExecute.registerCommandExecute(new IBasicCommand(this));
        HandlerInjection.load();
        afterLoadConfig();
        DatabaseCenter.init();
    }

    public static HashSet<String> onlinePlayerNames = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void whenPlayerJoinServer(PlayerJoinEvent event){
        onlinePlayerNames.add(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void whenPlayerQuitServer(PlayerQuitEvent event){
        onlinePlayerNames.remove(event.getPlayer().getName());
    }

    public static void afterLoadConfig(){

    }

    @Override
    public void onDisable() {


    }
}
