package io.aoitori043.aoitoriproject.impl.command.sub;



import io.aoitori043.aoitoriproject.command.*;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import org.bukkit.command.CommandSender;

import java.util.List;

import static io.aoitori043.aoitoriproject.AoitoriProject.afterLoadConfig;

@SubArgument(
        argument = "reload"
)
public class ISubCommandReload extends SubCommand {


    @TabCompletion()
    public List<String> getTabCompletion(int index) {
        return null;
    }

    @NotArgument(help = "重载插件")
    @ExecutionStartMessage(message = "开始重载...")
    @ExecutionEndMessage(message = "重载完成，耗时 %time%s")
    public void execute(CommandSender sender,List<ArgumentHelper> arguments) {
        ConfigHandler.load();
        afterLoadConfig();
    }

}
