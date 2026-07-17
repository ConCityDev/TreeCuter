package pl.norbit.treecuter.utils.item;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import pl.norbit.treecuter.config.Settings;
import pl.norbit.treecuter.config.model.ItemType;

import java.util.Set;

public class MaterialMatcherUtils {
    private MaterialMatcherUtils (){}

    public static boolean isEqual(ItemStack itemStack, String materialId) {
        if (itemStack == null) return false;
        return match(materialId, itemStack.getType(), itemStack, null);
    }

    public static boolean isEqual(Block block, Set<String> materials) {
        if (block == null) return false;

        for (String material : materials) {
            if(match(material, block.getType(), null, block)){
                return true;
            }
        }
        return false;
    }

    public static boolean isEqual(Block block, String materialId) {
        if (block == null) return false;
        return match(materialId, block.getType(), null, block);
    }

    private static boolean match(String materialId, Material material, ItemStack itemStack, Block block) {
        String[] split = materialId.split(":");

        // VANILLA
        if (split.length < 2) {
            Material mat = Material.getMaterial(materialId.toUpperCase());
            return mat != null && material == mat;
        }

        String namespace = split[0];
        String id = split[1];

        ItemType type = getType(namespace);

        return switch (type) {
            case ITEMSADDER -> {
                if(!Settings.isItemsAdderEnabled()){
                    yield false;
                }
                if (itemStack != null) {
                    yield ItemsAdderUtils.isEqualItem(itemStack, id);
                }
                if (block != null) {
                    yield ItemsAdderUtils.isEqualBlock(block, id);
                }
                yield false;
            }
            case NEXO -> {
                if(!Settings.isNexoAdderEnabled()){
                    yield false;
                }
                if (itemStack != null) {
                    yield NexoUtils.isEqualItem(itemStack, id);
                }
                if (block != null) {
                    yield NexoUtils.isEqualBlock(block, id);
                }
                yield false;
            }

            case EXECUTABLEITEMS -> {
                if(!Settings.isExecutableItemsEnabled()){
                    yield false;
                }
                if (itemStack != null) {
                    yield ExecutableItemsUtils.isEqualItem(itemStack, id);
                }
                yield false;
            }
            case ORAXEN -> {
                if(!Settings.isOraxenEnabled()){
                    yield false;
                }
                if (itemStack != null) {
                    yield OraxenUtils.isEqualItem(itemStack, id);
                }
                yield false;
            }

            case CRAFTENGINE -> {
                if(!Settings.isCraftEngineEnabled()){
                    yield false;
                }
                if (itemStack != null) {
                    yield CraftEngineUtils.isEqualItem(itemStack, id);
                }
                yield false;
            }

            case MINECRAFT -> {
                Material mat = Material.getMaterial(id.toUpperCase());
                yield mat != null && material == mat;
            }
        };
    }

    private static ItemType getType(String namespace) {
        if (namespace.equalsIgnoreCase("ia") || namespace.equalsIgnoreCase("itemsadder") ) {
            return ItemType.ITEMSADDER;
        } else if (namespace.equalsIgnoreCase("nexo")) {
            return ItemType.NEXO;
        } else if (namespace.equalsIgnoreCase("oraxen")) {
            return ItemType.ORAXEN;
        } else if (namespace.equalsIgnoreCase("ei") || (namespace.equalsIgnoreCase("executableitems"))) {
            return ItemType.EXECUTABLEITEMS;
        } else if (namespace.equalsIgnoreCase("ce") || (namespace.equalsIgnoreCase("craftengine"))) {
            return ItemType.CRAFTENGINE;
        }
        return ItemType.MINECRAFT;
    }
}