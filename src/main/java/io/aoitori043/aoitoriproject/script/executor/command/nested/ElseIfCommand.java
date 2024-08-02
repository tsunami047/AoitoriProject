package io.aoitori043.aoitoriproject.script.executor.command.nested;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-02  17:16
 * @Description: ?
 */
public class ElseIfCommand extends IfCommand{
    public ElseIfCommand(int depth, String[] parameters) {
        super(depth, parameters);
        this.type = "elseIf";
    }
}
