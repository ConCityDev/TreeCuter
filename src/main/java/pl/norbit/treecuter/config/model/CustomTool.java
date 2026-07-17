package pl.norbit.treecuter.config.model;

import dev.lone.itemsadder.api.CustomStack;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.treecuter.config.Settings;
import pl.norbit.treecuter.utils.ChatUtils;
import pl.norbit.treecuter.utils.ToolUsageUtils;
import pl.norbit.treecuter.utils.item.ItemsAdderUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomTool {
    private String name;
    private String material;
    private List<String> lore;
    private Integer customModelData; // For custom model data support

    public ItemStack getItemStack() {
        // Check if it's an ItemsAdder item (format: ia:item_id or itemsadder:item_id)
        if (material != null && (material.startsWith("ia:") || material.startsWith("itemsadder:"))) {
            if (Settings.isItemsAdderEnabled()) {
                String itemId = material.contains(":") ? material.split(":", 2)[1] : material;
                CustomStack stack = CustomStack.getInstance(itemId);
                ItemStack iaItem = stack != null ? stack.getItemStack().clone() : null;

                if (iaItem != null) {
                    // Apply custom name and lore if specified
                    if (name != null || (lore != null && !lore.isEmpty())) {
                        ItemMeta meta = iaItem.getItemMeta();
                        if (name != null) {
                            meta.setDisplayName(ChatUtils.format(name));
                        }
                        if (lore != null && !lore.isEmpty()) {
                            List<String> formatLore = lore.stream()
                                    .map(ChatUtils::format)
                                    .toList();
                            meta.setLore(formatLore);
                        }
                        iaItem.setItemMeta(meta);
                    }
                    return iaItem;
                }
            }
            throw new IllegalArgumentException("ItemsAdder is not enabled or item not found: " + material);
        }

        // Regular Minecraft material
        Material mat = Material.getMaterial(material.toUpperCase());

        if (mat == null) {
            throw new IllegalArgumentException("Invalid material: " + material);
        }

        ItemStack itemStack = new ItemStack(mat);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> formatLore = lore != null ? lore.stream()
                .map(ChatUtils::format)
                .toList() : new ArrayList<>();

        itemMeta.setDisplayName(ChatUtils.format(name));
        itemMeta.setLore(formatLore);

        // Apply custom model data if specified
        if (customModelData != null) {
            itemMeta.setCustomModelData(customModelData);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack getItemStackWithUsage(int maxUsage, int usedUsage) {
        ItemStack itemStack = getItemStack();
        return ToolUsageUtils.setUsageRights(itemStack, maxUsage, usedUsage);
    }

    public boolean isSimilar(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        // Check if it's an ItemsAdder item
        if (material != null && (material.startsWith("ia:") || material.startsWith("itemsadder:"))) {
            if (Settings.isItemsAdderEnabled()) {
                String itemId = material.contains(":") ? material.split(":", 2)[1] : material;
                return ItemsAdderUtils.isEqualItem(item, itemId);
            }
            return false;
        }

        // Check regular Minecraft material
        Material mat = Material.getMaterial(material.toUpperCase());
        if (mat == null) {
            return false;
        }

        // Check if the material matches
        if (item.getType() != mat) {
            return false;
        }

        // Check if item has meta
        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        // Check display name if specified
        if (name != null) {
            if (!meta.hasDisplayName()) {
                return false;
            }
            if (!meta.getDisplayName().equals(ChatUtils.format(name))) {
                return false;
            }
        }

        // Check custom model data if specified
        if (customModelData != null) {
            if (!meta.hasCustomModelData()) {
                return false;
            }
            return meta.getCustomModelData() == customModelData;
        }

        return true;
    }
}
