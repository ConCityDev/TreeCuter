package pl.norbit.treecuter.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import pl.norbit.treecuter.utils.ToolUsageUtils;

/**
 * Prevents items with usage rights from taking normal durability damage
 * Durability is only used for visual representation of remaining uses
 */
public class ItemDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        
        // If item has usage rights system, cancel normal damage
        if (ToolUsageUtils.hasUsageRights(item)) {
            event.setCancelled(true);
        }
    }
}