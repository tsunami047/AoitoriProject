package io.aoitori043.aoitoriproject.command;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  14:49
 * @Description:
 * 如果要在这个类定义方法必须实现克隆方法
 */
public abstract class BasicCommand {
    public JavaPlugin plugin;
    public String parentName;

    public BasicCommand(String parentName,JavaPlugin plugin) {
        this.plugin = plugin;
        this.parentName = parentName;
    }


    public abstract Class<? extends SubCommand>[] getSubCommands();

    public abstract List<String> getAllAlias();


    public BasicCommand clone() throws CloneNotSupportedException {
        return (BasicCommand) super.clone();
    }

    public String getAlias(){
        if(getAllAlias() == null || getAllAlias().isEmpty()){
            return plugin.getName();
        }
        return getAllAlias().get(0);
    }
    public abstract void sendMessage(CommandSender sender,String msg);
    public abstract String getPrefix();

    public void sendMessageWithPrefix(CommandSender sender,String msg){
        sendMessage(sender,getPrefix()+msg);
    }

}
