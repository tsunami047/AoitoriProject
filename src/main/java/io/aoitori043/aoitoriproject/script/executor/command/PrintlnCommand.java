package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-09  02:46
 * @Description: ?
 */
public class PrintlnCommand extends AbstractCommand {

    Expression text;

    public PrintlnCommand(int depth, String[] parameters) {
        super(depth, "println", parameters);
    }

    public void compile() {
        text = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        System.out.println(text.interpret(playerDataAccessor,variables));
        return null;
    }
}