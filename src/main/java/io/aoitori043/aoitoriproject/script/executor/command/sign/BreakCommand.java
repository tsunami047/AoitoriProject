package io.aoitori043.aoitoriproject.script.executor.command.sign;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:41
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class BreakCommand extends AbstractCommand {


    public BreakCommand(int depth, String[] parameters) {
        super(depth, "break", parameters);
    }

    public void compile() {

    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setBreak(true);
        return null;
    }
}
