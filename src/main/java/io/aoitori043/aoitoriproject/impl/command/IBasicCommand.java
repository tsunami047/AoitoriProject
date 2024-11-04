package io.aoitori043.aoitoriproject.impl.command;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.command.*;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.aoitoriproject.impl.command.sub.ISubCommandReload;
import io.aoitori043.aoitoriproject.op.BukkitReflectionUtils;
import io.aoitori043.syncdistribute.rmi.RMIClient;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:29
 * @Description: ?
 */
@BasicCommandParameter()
public class IBasicCommand extends BasicCommand {

    public IBasicCommand(JavaPlugin plugin) {
        super(plugin.getName(),plugin);
    }

    public String getPrefix(){
        return ConfigHandler.instance.pluginPrefix;
    }

    @Override
    public Class<? extends SubCommand>[] getSubCommands() {
        return new Class[]{
                ISubCommandReload.class,
        };
    }

    @Override
    public List<String> getAllAlias() {
        return Arrays.asList("ap","apj");
    }

    @Override
    public void sendMessage(CommandSender sender,String msg) {
        if (sender instanceof Player) {
            ((Player)sender).sendMessage(msg.replaceAll("&","§"));
        } else {
            plugin.getLogger().info(msg.replaceAll("§.", ""));
        }
    }

    @Parameter(argument = "bclink",help = "连接bc")
    public void test(CommandSender sender,List<SubCommand.ArgumentHelper> arguments){
        RMIClient.start();
    }

    @Parameter(argument = "test",help = "连接bc")
    public void test2(CommandSender sender,List<SubCommand.ArgumentHelper> arguments) {
        try {
            AoitoriProject.messageService.sendMessage("Aoitori", "t1newbee");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
