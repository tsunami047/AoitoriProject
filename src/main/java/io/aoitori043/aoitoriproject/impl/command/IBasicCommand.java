package io.aoitori043.aoitoriproject.impl.command;

import io.aoitori043.aoitoriproject.command.*;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.aoitoriproject.impl.command.sub.ISubCommandReload;
import io.aoitori043.aoitoriproject.op.BukkitReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

    @ExecutePermission(isOp = false)
    @Parameter(argument = "test1",help = "op指令")
    public void test1(CommandSender sender,List<SubCommand.ArgumentHelper> arguments){

        System.out.println(sender.isOp());
    }

    @Parameter(argument = "test",help = "测试权限")
    public void test(CommandSender sender,List<SubCommand.ArgumentHelper> arguments){
        for (int i = 0; i < 1000; i++) {
            BukkitReflectionUtils.syncSafeOpRunCommand((Player) arguments.get(0).getAsPlayer(), Arrays.asList("aoitoriproject test1"));
        }
    }
}
