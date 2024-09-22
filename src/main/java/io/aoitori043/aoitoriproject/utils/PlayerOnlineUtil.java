package io.aoitori043.aoitoriproject.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.aoitori043.aoitoriproject.AoitoriProject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Collection;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-21  00:56
 * @Description: ?
 */
public class PlayerOnlineUtil implements PluginMessageListener{


    public boolean checkPlayerOnBungeeServer(String playerName, String targetServer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetPlayerServer");
        out.writeUTF(playerName);
        Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
        if (onlinePlayers == null || onlinePlayers.isEmpty()){
            return false;
        }
        onlinePlayers.iterator().next().sendPluginMessage(AoitoriProject.plugin, "BungeeCord", out.toByteArray());
        return true;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetPlayerServer")) {
            String userName = in.readUTF();
            int serverName = in.readInt();
        }
    }

    public void register(Plugin plugin){
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
    }
}
