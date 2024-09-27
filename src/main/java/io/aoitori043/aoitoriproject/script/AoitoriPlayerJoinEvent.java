package io.aoitori043.aoitoriproject.script;

import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.CommandCompiler;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-01  02:41
 * @Description: 当数据暂时没有被存储进数据库时阻止玩家连接任何一个子服务器，但是玩家已经离开
 */
@Getter
@ToString
public class AoitoriPlayerJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    public Player player;

    public AoitoriPlayerJoinEvent(Player player) {
        this.player = player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}