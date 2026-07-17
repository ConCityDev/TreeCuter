package pl.norbit.treecuter.utils.item;

import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OraxenUtils {
    private OraxenUtils() {}

    public static void oraxenBreak(Block b, Player p){
        BlockMechanic blockMechanic = OraxenBlocks.getBlockMechanic(b);

        if(blockMechanic != null){
            OraxenBlocks.remove(b.getLocation(), p);
        }else {
            b.breakNaturally();
        }
    }

    public static void oraxenBreak(Block b){
        BlockMechanic blockMechanic = OraxenBlocks.getBlockMechanic(b);

        if(blockMechanic != null){
            OraxenBlocks.remove(b.getLocation(), null);
        }else {
            b.breakNaturally();
        }
    }

    public static boolean isEqualBlock(Block b, String id) {
        BlockMechanic blockMechanic = OraxenBlocks.getBlockMechanic(b);

        if(blockMechanic == null){
            return false;
        }

        return id.equals(blockMechanic.getItemID());
    }

    /**
     * Check if ItemStack is equal to ItemsAdder item
     * @param item ItemStack
     * @param id Item id
     * @return True if ItemStack is equal to ItemsAdder item
     */
    public static boolean isEqualItem(ItemStack item, String id) {
        String itemId = OraxenItems.getIdByItem(item);

        if(itemId == null){
            return false;
        }

        return id.equals(itemId);
    }
}
