package io.aoitori043.aoitoriproject;

import com.tuershen.nbtlibraryfix.NBTLibraryMain;
import io.aoitori043.aoitoriproject.command.BasicCommandExecute;
import io.aoitori043.aoitoriproject.config.impl.BasicDatabaseMapper;
import io.aoitori043.aoitoriproject.database.DatabaseCenter;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import io.aoitori043.aoitoriproject.database.point.PointManager;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.aoitoriproject.impl.command.IBasicCommand;
import io.aoitori043.aoitoriproject.op.BukkitReflectionUtils;
import io.aoitori043.aoitoriproject.script.PlaceholderHook;
import io.aoitori043.aoitoriproject.script.TemporaryDataManager;
import io.aoitori043.aoitoriproject.thread.KilimScheduler;
import io.aoitori043.aoitoriproject.utils.lock.DistributedLock;
import io.aoitori043.aoitoriproject.utils.lock.PlayerResourceLock;
import io.aoitori043.syncdistribute.rmi.MessageChannelListener;
import io.aoitori043.syncdistribute.rmi.RMIClient;
import io.aoitori043.syncdistribute.rmi.heartbeat.NodeServer;
import io.aoitori043.syncdistribute.rmi.service.MessageService;
import io.aoitori043.syncdistribute.rmi.service.OnlineService;
import io.aoitori043.syncdistribute.rmi.service.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashSet;

public final class AoitoriProject extends JavaPlugin implements Listener {

    public static AoitoriProject plugin;
    public static int port;
    public static PointManager pointManager = new PointManager();
    public static KilimScheduler kilimScheduler = new KilimScheduler();
    public static MessageService messageService;
    public static OnlineService onlineService;
    public static PlayerDataService playerDataService;
    public static DistributedLock distributedLock;
    public static PlayerResourceLock playerResourceLock;

    @Override
    public void onEnable() {
        plugin = this;
        new PlaceholderHook(this).register();
        NBTLibraryMain.loadNBTLibrary(this);
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getPluginManager().registerEvents(new TemporaryDataManager(),this);
        BasicCommandExecute.registerCommandExecute(new IBasicCommand(this));
        ConfigHandler.load();
        afterLoadConfig();
        try {
            BukkitReflectionUtils.init(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        DatabaseCenter.init();
        NodeServer.start();
        RMIClient.start();
        port = plugin.getServer().getPort();
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
        File serverFolder = AoitoriProject.plugin.getDataFolder().getParentFile().getParentFile();
        String serverName = serverFolder.getName();
        serverId = DatabaseProperties.bc$serverId == null || DatabaseProperties.bc$serverId.equals("auto") ? serverName : DatabaseProperties.bc$serverId;
    }

    @Override
    public void onDisable() {


    }
}
