package io.aoitori043.aoitoriproject.script.executor.command.sign;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-07  22:12
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DelayCommand extends AbstractCommand {

    public Expression delay;

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        return null;
    }

    public DelayCommand(int depth, String[] parameters) {
        super(depth, "delay", parameters);
    }

    public void compile() {
        delay = new Expression(parameters[0]);
    }
}
