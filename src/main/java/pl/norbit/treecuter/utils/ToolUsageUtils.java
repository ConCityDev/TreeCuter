package pl.norbit.treecuter.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.norbit.treecuter.TreeCuter;

import java.util.ArrayList;
import java.util.List;

public class ToolUsageUtils {

    private static final NamespacedKey MAX_USAGE_KEY = new NamespacedKey(TreeCuter.getInstance(), "max_usage");
    private static final NamespacedKey USED_USAGE_KEY = new NamespacedKey(TreeCuter.getInstance(), "used_usage");

    private ToolUsageUtils() {
        throw new IllegalStateException("This class cannot be instantiated");
    }

    /**
     * Set usage rights to an item
     * @param item ItemStack to set usage rights
     * @param maxUsage Maximum usage rights
     * @param usedUsage Used usage rights
     * @return ItemStack with updated usage rights
     */
    public static ItemStack setUsageRights(ItemStack item, int maxUsage, int usedUsage) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(MAX_USAGE_KEY, PersistentDataType.INTEGER, maxUsage);
        container.set(USED_USAGE_KEY, PersistentDataType.INTEGER, usedUsage);

        updateLore(meta, maxUsage, usedUsage);
        updateDurabilityBar(item, meta, maxUsage, usedUsage);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Get maximum usage rights from an item
     * @param item ItemStack to get maximum usage rights
     * @return Maximum usage rights, -1 if not set
     */
    public static int getMaxUsage(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return -1;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.getOrDefault(MAX_USAGE_KEY, PersistentDataType.INTEGER, -1);
    }

    /**
     * Get used usage rights from an item
     * @param item ItemStack to get used usage rights
     * @return Used usage rights, 0 if not set
     */
    public static int getUsedUsage(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.getOrDefault(USED_USAGE_KEY, PersistentDataType.INTEGER, 0);
    }

    /**
     * Check if an item has usage rights
     * @param item ItemStack to check
     * @return true if item has usage rights, false otherwise
     */
    public static boolean hasUsageRights(ItemStack item) {
        return getMaxUsage(item) != -1;
    }

    /**
     * Check if an item has remaining usage rights
     * @param item ItemStack to check
     * @return true if item has remaining usage rights, false otherwise
     */
    public static boolean hasRemainingUsage(ItemStack item) {
        int maxUsage = getMaxUsage(item);
        int usedUsage = getUsedUsage(item);

        if (maxUsage == -1) {
            return true; // No usage limit
        }

        return usedUsage < maxUsage;
    }

    /**
     * Get remaining usage rights from an item
     * @param item ItemStack to get remaining usage rights
     * @return Remaining usage rights, -1 if no limit
     */
    public static int getRemainingUsage(ItemStack item) {
        int maxUsage = getMaxUsage(item);
        int usedUsage = getUsedUsage(item);

        if (maxUsage == -1) {
            return -1; // No usage limit
        }

        return maxUsage - usedUsage;
    }

    /**
     * Increment used usage rights for an item
     * @param item ItemStack to increment usage
     * @param amount Amount to increment
     * @return Updated ItemStack, null if item has no more usage rights
     */
    public static ItemStack incrementUsage(ItemStack item, int amount) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        int maxUsage = getMaxUsage(item);

        // If no usage limit, return item as is
        if (maxUsage == -1) {
            return item;
        }

        int usedUsage = getUsedUsage(item);
        int newUsedUsage = usedUsage + amount;

        // If item has no more usage rights, return null
        if (newUsedUsage > maxUsage) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(USED_USAGE_KEY, PersistentDataType.INTEGER, newUsedUsage);

        updateLore(meta, maxUsage, newUsedUsage);
        updateDurabilityBar(item, meta, maxUsage, newUsedUsage);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Update item's durability bar to reflect usage rights (visual only)
     * @param item ItemStack to update
     * @param meta ItemMeta of the item
     * @param maxUsage Maximum usage rights
     * @param usedUsage Used usage rights
     */
    private static void updateDurabilityBar(ItemStack item, ItemMeta meta, int maxUsage, int usedUsage) {
        // Only update durability if item has a durability bar
        if (!(meta instanceof org.bukkit.inventory.meta.Damageable damageable)) {
            return;
        }

        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability <= 0) {
            return;
        }

        // Calculate durability based on remaining usage rights
        int remainingUsage = maxUsage - usedUsage;
        double usagePercentage = (double) remainingUsage / maxUsage;

        // Set damage so that durability bar shows remaining usage percentage
        // damage = maxDurability - (maxDurability * usagePercentage)
        int damage = (int) (maxDurability * (1.0 - usagePercentage));

        // Ensure damage doesn't exceed max durability
        damage = Math.min(damage, maxDurability - 1);
        damage = Math.max(damage, 0);

        damageable.setDamage(damage);
    }

    /**
     * Update lore with usage information
     * @param meta ItemMeta to update
     * @param maxUsage Maximum usage rights
     * @param usedUsage Used usage rights
     */
    @SuppressWarnings("deprecation")
    private static void updateLore(ItemMeta meta, int maxUsage, int usedUsage) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Get the usage lore pattern from config (without color codes and placeholders)
        String usageLorePattern = pl.norbit.treecuter.config.Settings.getToolUsageLore()
                .replaceAll("&[0-9a-fk-or]", "") // Remove color codes
                .replaceAll("\\{remaining}", "")
                .replaceAll("\\{max}", "")
                .replaceAll("\\{used}", "")
                .trim();

        // Remove old usage lore if exists (check for pattern match or old hardcoded messages)
        lore.removeIf(line -> {
            if (line == null) return false;
            String stripped = line.replaceAll("§[0-9a-fk-or]", ""); // Remove bukkit color codes
            return stripped.contains("Remaining usage:")
                || stripped.contains("Kalan kullanım:")
                || (usageLorePattern.length() > 3 && stripped.contains(usageLorePattern.substring(0, Math.min(usageLorePattern.length(), 10))));
        });

        // Add new usage lore from config
        int remaining = maxUsage - usedUsage;
        String usageLine = pl.norbit.treecuter.config.Settings.getToolUsageLore()
                .replace("{remaining}", String.valueOf(remaining))
                .replace("{max}", String.valueOf(maxUsage))
                .replace("{used}", String.valueOf(usedUsage));

        lore.add(ChatUtils.format(usageLine));

        meta.setLore(lore);
    }
}

