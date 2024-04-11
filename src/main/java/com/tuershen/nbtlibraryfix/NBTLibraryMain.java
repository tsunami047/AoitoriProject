package com.tuershen.nbtlibraryfix;

import com.tuershen.nbtlibraryfix.api.CompoundLibraryApi;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * @auther Tuershen Create Date on 2020/12/28
 */
public class NBTLibraryMain extends JavaPlugin {


    public static CompoundLibraryApi libraryApi;

    public static void loadNBTLibrary(JavaPlugin plugin) {
        libraryApi = CompoundLibraryManager.getPluginManager(plugin);
    }

}
