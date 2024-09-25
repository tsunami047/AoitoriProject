package io.aoitori043.aoitoriproject;

import com.tuershen.nbtlibraryfix.NBTLibraryMain;
import io.aoitori043.aoitoriproject.command.BasicCommandExecute;
import io.aoitori043.aoitoriproject.database.DatabaseCenter;
import io.aoitori043.aoitoriproject.database.point.PointManager;
import io.aoitori043.aoitoriproject.database.point.redis.RedisDataCache;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.aoitoriproject.impl.command.IBasicCommand;
import io.aoitori043.aoitoriproject.op.BukkitReflectionUtils;
import io.aoitori043.aoitoriproject.script.PlaceholderHook;
import io.aoitori043.aoitoriproject.script.TemporaryDataManager;
import io.aoitori043.aoitoriproject.thread.KilimScheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public final class AoitoriProject extends JavaPlugin implements Listener {

    public static AoitoriProject plugin;
    public static PointManager pointManager = new PointManager();
    public static KilimScheduler kilimScheduler = new KilimScheduler();

    @Override
    public void onEnable() {
        plugin = this;
        new PlaceholderHook(this).register();
        NBTLibraryMain.loadNBTLibrary(this);
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getPluginManager().registerEvents(new TemporaryDataManager(),this);
        Bukkit.getPluginManager().registerEvents(new RedisDataCache(),this);
        BasicCommandExecute.registerCommandExecute(new IBasicCommand(this));
        ConfigHandler.load();
        afterLoadConfig();
        try {
            BukkitReflectionUtils.init(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        DatabaseCenter.init();
//        RMIClient.start();
    }

    public static boolean isPlayerOnline(String playerName){
        return onlinePlayerNames.contains(playerName);
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
