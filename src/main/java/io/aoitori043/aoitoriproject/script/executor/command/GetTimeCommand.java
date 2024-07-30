package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:59
 * @Description: ?
 */
public class GetTimeCommand extends AbstractCommand {

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        performReturnContent.setResult(System.currentTimeMillis());
        return nestedCommandWrapper;
    }

    public GetTimeCommand(int depth, String[] parameters) {
        super(depth, "getTime", parameters);
    }

    public void compile() {
    }
}