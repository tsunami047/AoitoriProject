package io.aoitori043.aoitoriproject.impl.command.sub;


import io.aoitori043.aoitoriproject.command.*;
import io.aoitori043.syncdistribute.rmi.RMIClient;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-13  01:20
 * @Description: ?
 */
@SubArgument(
        argument = "test"
)
public class ISubCommandTest extends SubCommand {


    @Parameter(argument = "t1", help = "测试")
    @ExecutionEndMessage(message = "耗时 %time%s")
    @ParameterSpecification(index = 0, tip = "player", type = ParameterSpecification.Type.Text)
    public void execute_test(CommandSender sender,List<ArgumentHelper> arguments) {
        for (int i = 0; i < 100; i++) {
            RMIClient.isOnline(arguments.get(0).getOriginalArg());
        }

    }

    @Parameter(argument = "t2", help = "测试2")
    @ParameterSpecification(index = 0, tip = "player", type = ParameterSpecification.Type.Text)
    public void execute_test2(CommandSender sender,List<ArgumentHelper> arguments) {
        RMIClient.start();
    }


}
