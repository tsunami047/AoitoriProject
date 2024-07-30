package io.aoitori043.aoitoriproject.script.executor.command.sign;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-30  03:46
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CancelCommand extends AbstractCommand {
    public CancelCommand(int depth, String[] parameters) {
        super(depth, "cancel", parameters);
    }

    public void compile() {

    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setCancel(true);
        return null;
    }
}
