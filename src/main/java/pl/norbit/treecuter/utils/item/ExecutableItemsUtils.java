package pl.norbit.treecuter.utils.item;

import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ExecutableItemsUtils {
    private ExecutableItemsUtils() {}

    /**
     * Check if ItemStack is equal to ItemsAdder item
     * @param item ItemStack
     * @param id Item id
     * @return True if ItemStack is equal to ItemsAdder item
     */
    public static boolean isEqualItem(ItemStack item, String id) {
        Optional<ExecutableItemInterface> executableItemOptional =
                ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id);

        if(executableItemOptional.isEmpty()) return false;

        Optional<ExecutableItemInterface> executableItemOptional1 =
                ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(item);

        return executableItemOptional1.filter(executableItemInterface -> executableItemOptional
                .get()
                .buildItem(1, Optional.empty())
                .equals(
                        executableItemInterface
                                .buildItem(1, Optional.empty())
                )).isPresent();
    }
}
