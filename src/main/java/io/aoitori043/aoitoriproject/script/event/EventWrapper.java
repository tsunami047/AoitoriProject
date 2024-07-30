package io.aoitori043.aoitoriproject.script.event;

import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-29  05:17
 * @Description: ?
 */
@Data
public class EventWrapper {

    String eventName;
    String checkSign;
    String[] parameters;

    List<AbstractCommand> commands;
}
