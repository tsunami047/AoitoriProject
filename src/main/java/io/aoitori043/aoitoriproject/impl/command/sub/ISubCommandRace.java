package io.aoitori043.aoitoriproject.impl.command.sub;


import io.aoitori043.aoitoriproject.command.*;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-13  01:20
 * @Description: ?
 */
@SubArgument(
        argument = "race"
)
public class ISubCommandRace extends SubCommand {


    @TabCompletion(argument = "set")
    public List<String> getTabCompletion(int index) {
        return null;
    }


    @Parameter(
            argument = "set",
            help = "设置玩家种族"
    )
    @ParameterSpecification(
            index = 0,
            tip = "player",
            type = ParameterSpecification.Type.Player
    )
    @ParameterSpecification(
            index = 1,
            tip = "race"
    )
    public void execute_set(CommandSender sender,List<ArgumentHelper> arguments) {
        System.out.println(1);
    }

    @Parameter(
            argument = "del",
            help = "清空玩家种族记录"
    )
    @ParameterSpecification(
            index = 1,
            tip = "player",
            type = ParameterSpecification.Type.Player
    )
    public void execute_del(CommandSender sender,List<ArgumentHelper> arguments) {
        System.out.println(2);
    }
}
