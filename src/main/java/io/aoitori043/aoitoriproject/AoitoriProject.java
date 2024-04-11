package io.aoitori043.aoitoriproject;

import com.tuershen.nbtlibraryfix.NBTLibraryMain;
import io.aoitori043.aoitoriproject.command.BasicCommandExecute;
import io.aoitori043.aoitoriproject.database.DatabaseCenter;
import io.aoitori043.aoitoriproject.database.orm.cache.JVMCacheSync;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.aoitoriproject.impl.command.IBasicCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AoitoriProject extends JavaPlugin {

    public static AoitoriProject plugin;

    @Override
    public void onEnable() {
        plugin = this;
        NBTLibraryMain.loadNBTLibrary(this);
        BasicCommandExecute.registerCommandExecute(new IBasicCommand(this));
        ConfigHandler.load();
        afterLoadConfig();
        DatabaseCenter.init();
    }

    public static void afterLoadConfig(){

    }

    @Override
    public void onDisable() {
        JVMCacheSync.stopZeroMQService();
    }
}
