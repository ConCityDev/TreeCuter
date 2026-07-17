package pl.norbit.treecuter.utils.item;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.inventory.ItemStack;

public class CraftEngineUtils {

    private CraftEngineUtils() {}

    public static boolean isEqualItem(ItemStack stack1, String id) {
        BukkitItemDefinition bukkitItemDefinition = CraftEngineItems.byId(id);

        if(bukkitItemDefinition == null) return false;

        BukkitItemDefinition bukkitItemDefinition1 = CraftEngineItems.byItemStack(stack1);

        if(bukkitItemDefinition1 == null) return false;

        return bukkitItemDefinition.equals(bukkitItemDefinition1);
    }
}
