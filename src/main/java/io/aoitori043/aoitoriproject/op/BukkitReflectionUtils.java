package io.aoitori043.aoitoriproject.op;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import io.aoitori043.aoitoriproject.impl.HandlerInjection;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.OpListEntry;
import net.minecraft.server.v1_12_R1.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-04  13:09
 * @Description: 安全的提权op执行指令
 */
public class BukkitReflectionUtils {

    public volatile static net.minecraft.server.v1_12_R1.OpList ops;
    public static int i;
    //uuid+GameProfile，放进去就是OP
    public volatile static Map<String, Object> opMap;
    public volatile static ConcurrentHashMap<Player, CopyOnWriteArrayList<String>> opcmds = new ConcurrentHashMap<>();
    public static List<String> oplist; //配置中的op列表

    public static List<String[]> safeCommand;


    /**
     * @date 2023/6/4 13:15
     * @description 安全的以op权限执行命令
     */
    public static void safeOpRunCommand(Player player, List<String> commands) {
        UnauthCommandHandler.addTask(player,commands);
    }

    /**
     * @param file
     * @return String
     * @date 2023/6/8 15:21
     * @description 提取string文本
     */
    public static String readFileToString(File file) throws IOException {
        Path filePath = file.toPath();
        byte[] fileBytes = Files.readAllBytes(filePath);
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    public static void init(JavaPlugin javaPlugin) throws NoSuchFieldException, IllegalAccessException, IOException {
        Bukkit.getPluginManager().registerEvents(new UnauthCommandHandler(),javaPlugin);
        CraftServer cs = (CraftServer) Bukkit.getServer();
        MinecraftServer minecraftServer = (MinecraftServer) io.aoitori043.aoitoriproject.ReflectionUtil.getPrivateAndSuperField(cs, "console");
        PlayerList playerList = minecraftServer.getPlayerList();
        i = minecraftServer.q();
        ops = playerList.getOPs();
        File c = PlayerList.c;
//        Bukkit.getOperators() 重载时有玩家为OP时可能会逃逸监控
        opMap = (Map<String, Object>) io.aoitori043.aoitoriproject.ReflectionUtil.getPrivateAndSuperField(ops, "d");
        PlayerOPList[] playerOPLists = new Gson().fromJson(readFileToString(c), PlayerOPList[].class);
        oplist = Arrays.stream(playerOPLists).map(PlayerOPList::getName).collect(Collectors.toList());
        YamlConfiguration basicConfig = HandlerInjection.instance.basicConfig;
        safeCommand = new ArrayList<>();
        List<String> allow_other_plugin_execute_cmd = basicConfig.getStringList("safeOpCommands");
        for (String s : allow_other_plugin_execute_cmd) {
            safeCommand.add(s.split(" "));
        }
    }

    public static void setOp(Player player, boolean value) {
        GameProfile profile = ((CraftPlayer) player).getProfile();
        if (value && !player.isOp()) {
            OpListEntry opListEntry = new OpListEntry(profile, i, ops.b(profile));
            opMap.put(player.getUniqueId().toString(), opListEntry);
        } else {
            opMap.remove(player.getUniqueId().toString());
        }
    }


    public static class PlayerOPList {
        private String uuid;
        private String name;
        private int level;
        private boolean bypassesPlayerLimit;

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "PlayerOPList{" + "name='" + name + '\'' + '}';
        }

        // getters and setters
    }

}
