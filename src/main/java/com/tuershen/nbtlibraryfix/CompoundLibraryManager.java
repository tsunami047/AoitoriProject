package com.tuershen.nbtlibraryfix;


import com.tuershen.nbtlibraryfix.api.CompoundLibraryApi;
import com.tuershen.nbtlibraryfix.minecraft.CraftItemStack;
import com.tuershen.nbtlibraryfix.minecraft.MinecraftItemStack;
import com.tuershen.nbtlibraryfix.minecraft.PluginManager;
import com.tuershen.nbtlibraryfix.common.AbstractNBTTagCompound;
import com.tuershen.nbtlibraryfix.minecraft.block.AbstractMinecraftEntityTile;
import com.tuershen.nbtlibraryfix.minecraft.entity.AbstractMinecraftEntity;
import com.tuershen.nbtlibraryfix.minecraft.item.AbstractMinecraftItem;
import com.tuershen.nbtlibraryfix.minecraft.nbt.AbstractMinecraftNBTTag;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class CompoundLibraryManager {

    public static Server server;

    public static CompoundLibraryApi getPluginManager(Plugin plugin){
        server = plugin.getServer();
        initPluginManager(plugin.getServer());
        return new PluginManager();
    }

    protected static void initPluginManager(Server server){
        String version = paraphrase(server);
        AbstractMinecraftNBTTag.init(version);
        AbstractMinecraftEntity.init(version);
        AbstractMinecraftEntityTile.init(version);
        AbstractMinecraftItem.init(version);
        AbstractNBTTagCompound.init(AbstractMinecraftNBTTag.getInstance(), version);
        CraftItemStack.initCraftItemStackClass(version);
        MinecraftItemStack.initMinecraftItemStackClass();
    }


    protected static String paraphrase(Server server){ return server.getClass().getPackage().getName().replace(".", ",").split(",")[3]; }

}
