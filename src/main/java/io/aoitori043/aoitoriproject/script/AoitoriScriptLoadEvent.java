package io.aoitori043.aoitoriproject.script;

import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.CommandCompiler;
import lombok.Data;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-09  14:02
 * @Description: ?
 */
@Data
public class AoitoriScriptLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    CommandCompiler commandCompiler;
    String originalCommand;
    int depth;
    int startLine;
    int endLine;
    List<String> originalList;
    List<AbstractCommand> compiledCommands;

    AbstractCommand currentCommand;



    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
