package pl.norbit.treecuter.config.model;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.treecuter.utils.ToolUsageUtils;

import java.util.List;
import java.util.Objects;

@Data
public class CutShape {
    private String id;
    private ChatColor glowingColor;
    private List<CustomItem> acceptTools;

    private CustomTool customTool;
    private List<Material> acceptBlocks;

    public ItemStack getCustomToolItem() {
        if (customTool != null) {
            return customTool.getItemStack();
        }
        return null;
    }

    public boolean isAcceptTool(ItemStack item) {
        if(customTool != null){
            ItemStack itemStack = customTool.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            ItemMeta playerItemMeta = item.getItemMeta();

            if(itemStack.getType() != item.getType()){
                return false;
            }

            // Check if tool matches by name
            boolean nameMatches = Objects.equals(itemMeta.getDisplayName(), playerItemMeta.getDisplayName());

            // Check custom model data if present
            if (itemMeta.hasCustomModelData() && playerItemMeta.hasCustomModelData()) {
                if (itemMeta.getCustomModelData() != playerItemMeta.getCustomModelData()) {
                    return false;
                }
            }

            // If tool has usage rights, check if it has remaining usage
            if(nameMatches && ToolUsageUtils.hasUsageRights(item)) {
                return ToolUsageUtils.hasRemainingUsage(item);
            }

            return nameMatches;
        }

        return acceptTools.stream()
                .anyMatch(acceptTool -> acceptTool.isEqual(item));
    }

    public boolean isAcceptBlock(Material mat) {
        return acceptBlocks.stream()
                .anyMatch(acceptBlock -> acceptBlock == mat);
    }
}
