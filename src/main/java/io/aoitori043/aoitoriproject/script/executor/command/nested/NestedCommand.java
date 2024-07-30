package io.aoitori043.aoitoriproject.script.executor.command.nested;

import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:17
 * @Description: ?
 */
@Data
public abstract class NestedCommand extends AbstractCommand {
    public NestedCommand(int depth, String type, String[] parameters) {
        super(depth, type, parameters);
    }

    public List<AbstractCommand> nestedCommands;

    public NestedCommand() {
    }
}
