package io.aoitori043.aoitoriproject.script.executor.command;

import com.tuershen.nbtlibraryfix.api.NBTTagCompoundApi;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import io.aoitori043.aoitoriproject.script.parameter.LabelPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.tuershen.nbtlibraryfix.NBTLibraryMain.libraryApi;
import static io.aoitori043.aoitoriproject.script.parameter.LabelPosition.NBT;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-28  04:03
 * @Description: ?
 */
public class GetHandItemCommand extends AbstractCommand {

    public LabelPosition position;
    public int line;
    public Expression expressionCompiler;

    public GetHandItemCommand(int depth, String[] parameters) {
        super(depth, "getHandItem", parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        Player player = playerDataAccessor.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            return null;
        }
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        switch (position) {
            case NBT:
                NBTTagCompoundApi compound = libraryApi.getCompound(itemInHand);
                String interpret = expressionCompiler.interpret(playerDataAccessor, variables).toString();
                try {
                    performReturnContent.setResult(compound.getString(interpret));
                    return nestedCommandWrapper;
                } catch (Exception e) {
                    return null;
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
                try {
                    if(lore.size() <= line){
                        return null;
                    }
                    performReturnContent.setResult(lore.get(line));
                    return nestedCommandWrapper;
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Could not interpret lore: " + expressionCompiler.interpret(playerDataAccessor, variables));
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }
            case DISPLAY_NAME: {
                if (!itemInHand.hasItemMeta()) {
                    return null;
                }
                ItemMeta itemMeta = itemInHand.getItemMeta();
                if (!itemMeta.hasDisplayName()) {
                    return null;
                }
                performReturnContent.setResult(itemMeta.getDisplayName());
                return nestedCommandWrapper;
            }
        }
        return null;
    }

    @Override
    public void compile() {
        switch (parameters[0]) {
            case "displayname":
                position = LabelPosition.DISPLAY_NAME;
                break;
            case "lore":
                position = LabelPosition.LORE;
                line = Integer.parseInt(parameters[1]);
                break;
            case "nbt":
                position = NBT;
                expressionCompiler = new Expression(parameters[1]);
                break;
        }
    }
}
