package pl.norbit.treecuter.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import pl.norbit.treecuter.config.Settings;

public class DurabilityUtils {
    private DurabilityUtils() {
        throw new IllegalStateException("This class cannot be instantiated");
    }

    public static int checkRemainingUses(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        
        if(meta.isUnbreakable()){
            return Settings.getMaxBlocks();
        }

        // Check if item has usage rights
        // Usage rights count USES not BLOCKS, so return max blocks per use
        if(ToolUsageUtils.hasUsageRights(item)){
            int remaining = ToolUsageUtils.getRemainingUsage(item);
            if(remaining > 0){
                // Return max blocks that can be cut per use, not remaining uses
                return Settings.getMaxBlocks();
            } else {
                // No remaining uses, return 0
                return 0;
            }
        }

        if (meta instanceof Damageable damageable) {
            int maxDurability = item.getType().getMaxDurability();
            int currentDamage = damageable.getDamage();
            int remainingDurability = maxDurability - currentDamage;

            //Check for durability enchantment
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (unbreakingLevel > 0) return remainingDurability * (unbreakingLevel + 1);
            
            return remainingDurability;
        }
        return 0;
    }

    public static ItemStack updateDurability(ItemStack item, int dmg){
        ItemMeta meta = item.getItemMeta();
        
        if(meta.isUnbreakable()){
            return item;
        }

        // Check and update usage rights if item has them
        if(ToolUsageUtils.hasUsageRights(item)){
            // Item has no more usage rights or updated item
            // Always increment by 1 for usage rights (counts uses, not blocks)
            return ToolUsageUtils.incrementUsage(item, 1);
        }

        if (meta instanceof Damageable damageable){
            int maxDurability = item.getType().getMaxDurability();

            //Check for durability enchantment
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            int actualDamage = dmg;
            if (unbreakingLevel > 0) actualDamage = dmg / (unbreakingLevel + 1);

            if(damageable.getDamage() + actualDamage >= maxDurability){
                return null;
            }
            damageable.setDamage((damageable.getDamage() + actualDamage));
        }
        item.setItemMeta(meta);
        return item;
    }
}
