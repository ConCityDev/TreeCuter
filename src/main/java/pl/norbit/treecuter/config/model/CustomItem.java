package pl.norbit.treecuter.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import pl.norbit.treecuter.utils.item.MaterialMatcherUtils;

@Data
@AllArgsConstructor
public class CustomItem {
    private String materialId;

    public boolean isEqual(ItemStack itemStack) {
        return MaterialMatcherUtils.isEqual(itemStack, materialId);
    }
}
