package pl.norbit.treecuter.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import pl.norbit.treecuter.config.Settings;

public class BlockStripListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockStrip(PlayerInteractEvent e) {
        var b = e.getClickedBlock();
        var action = e.getAction();
        var p = e.getPlayer();

        // Only check right-click actions
        if(action != Action.RIGHT_CLICK_BLOCK){
            return;
        }

        if(b == null){
            return;
        }

        ItemStack item = p.getInventory().getItemInMainHand();
        
        // Check if the item is a custom tool
        if(Settings.isCustomTool(item)) {
            // Check if the clicked block is a log that can be stripped
            String blockType = b.getType().toString();
            if(isStrippableLog(blockType)) {
                // Cancel the event to prevent stripping
                e.setCancelled(true);
            }
        }
    }

    private boolean isStrippableLog(String blockType) {
        return blockType.endsWith("_LOG") || 
               blockType.endsWith("_WOOD") || 
               blockType.endsWith("_STEM") || 
               blockType.endsWith("_HYPHAE");
    }
}

