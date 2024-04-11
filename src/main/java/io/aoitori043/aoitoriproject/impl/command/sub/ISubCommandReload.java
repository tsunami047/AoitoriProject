package io.aoitori043.aoitoriproject.impl.command.sub;



import io.aoitori043.aoitoriproject.command.*;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;

import java.util.List;

import static io.aoitori043.aoitoriproject.AoitoriProject.afterLoadConfig;

@SubArgument(
        argument = "reload",
        help = "重新加载配置文件"
)
public class ISubCommandReload extends SubCommand {

    @Override
    public List<String> getTabCompletion(int index) {
        return null;
    }

    @Override
    @ExecutePermission(isOp = true)
    @ExecutionStartMessage(message = "开始重载...")
    @ExecutionEndMessage(message = "重载完成，耗时 %time%s")
    public void execute(List<ArgumentHelper> arguments) {
        ConfigHandler.load();
        afterLoadConfig();
    }

}
