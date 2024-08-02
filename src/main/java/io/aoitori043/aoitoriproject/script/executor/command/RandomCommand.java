package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-31  21:25
 * @Description: ?
 */
public class RandomCommand extends AbstractCommand {


    public void compile() {
    }

    public RandomCommand(int depth, String[] parameters) {
        super(depth, "random", parameters);
    }

    @Override
    public AbstractCommand.NestedCommandWrapper execute(PlayerDataAccessor
    playerDataAccessor, AbstractCommand.PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setResult(new SecureRandom().nextDouble());
        return null;
    }
}
