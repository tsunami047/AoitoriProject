package io.aoitori043.aoitoriproject.script;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-01  02:43
 * @Description: ?
 */
@Getter
public class AoitoriPlayerQuitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    public Player player;

    public AoitoriPlayerQuitEvent(Player player) {
        this.player = player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}