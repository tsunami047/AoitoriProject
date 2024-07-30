package io.aoitori043.aoitoriproject.script.executor.command;

import com.tuershen.nbtlibraryfix.NBTLibraryMain;
import com.tuershen.nbtlibraryfix.api.NBTTagCompoundApi;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import io.aoitori043.aoitoriproject.script.parameter.LabelPosition;
import io.aoitori043.aoitoriproject.script.parameter.ValueType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  18:02
 * @Description: ?
 */
public class ChangeHandItemCommand extends AbstractCommand {

    public LabelPosition labelPosition;
    public ValueType valueType;
    public int line;
    public String nbtName;
    public Expression value; //需要进行封装

    public void compile() {
        switch (parameters[0].toLowerCase()) {
            case "displayname":
                labelPosition = LabelPosition.DISPLAY_NAME;
                value = new Expression(parameters[1]);
                break;
            case "lore":
                labelPosition = LabelPosition.LORE;
                line = Integer.parseInt(parameters[1]);
                value = new Expression(parameters[2]);
                break;
            case "nbt":
                labelPosition = LabelPosition.NBT;
                switch (parameters[1].toLowerCase()) {
                    case "string":
                        valueType = ValueType.STRING;
                        break;
                    case "double":
                        valueType = ValueType.DOUBLE;
                        break;
                    case "int":
                        valueType = ValueType.INT;
                        break;
                    case "boolean":
                        valueType = ValueType.BOOLEAN;
                        break;
                    case "float":
                        valueType = ValueType.FLOAT;
                        break;
                    default: {
                        throw new ClassCastException("预编译错误！不支持NBT类型：" + parameters[1]);
                    }
                }
                nbtName = parameters[2];
                value = new Expression(parameters[3]);
                break;
        }
    }

    public ChangeHandItemCommand(int depth, String[] parameters) {
        super(depth, "changeHandItem", parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        Player player = playerDataAccessor.getPlayer();
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) {
            return null;
        }
        switch (labelPosition) {
            case DISPLAY_NAME: {
                if (!itemInHand.hasItemMeta()) {
                    return null;
                }
                ItemMeta itemMeta = itemInHand.getItemMeta();
                itemMeta.setDisplayName(value.interpret(playerDataAccessor, variables).toString());
                itemInHand.setItemMeta(itemMeta);
                break;
            }
            case LORE: {
                if (!itemInHand.hasItemMeta()) {
                    return null;
                }
                ItemMeta itemMeta = itemInHand.getItemMeta();
                if (!itemMeta.hasLore()) {
                    return null;
                }
                List<String> lore = itemMeta.getLore();
                lore.set(line, value.interpret(playerDataAccessor, variables).toString());
                break;
            }
            case NBT: {
                NBTTagCompoundApi compound = NBTLibraryMain.libraryApi.getCompound(itemInHand);
                switch (valueType) {
                    case STRING:
                        compound.setString(nbtName, value.interpret(playerDataAccessor, variables).toString());
                        break;
                    case DOUBLE:
                        compound.setDouble(nbtName, Double.parseDouble(value.interpret(playerDataAccessor, variables).toString()));
                        break;
                    case BOOLEAN:
                        compound.setBoolean(nbtName, Boolean.parseBoolean(value.interpret(playerDataAccessor, variables).toString()));
                        break;
                    case INT:
                        compound.setInt(nbtName, Integer.parseInt(value.interpret(playerDataAccessor, variables).toString()));
                        break;
                    case FLOAT:
                        compound.setFloat(nbtName, Float.parseFloat(value.interpret(playerDataAccessor, variables).toString()));
                        break;
                }
                ItemStack itemStack = NBTLibraryMain.libraryApi.setCompound(itemInHand, compound);
                player.setItemInHand(itemStack);
                break;
            }
        }
        return null;
    }
}