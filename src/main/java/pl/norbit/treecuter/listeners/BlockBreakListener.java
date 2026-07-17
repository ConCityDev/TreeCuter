package pl.norbit.treecuter.listeners;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import pl.norbit.treecuter.config.Settings;
import pl.norbit.treecuter.config.model.CutShape;
import pl.norbit.treecuter.service.EffectService;
import pl.norbit.treecuter.service.TreeCutService;
import pl.norbit.treecuter.utils.ChatUtils;
import pl.norbit.treecuter.utils.ToolUsageUtils;

public class BlockBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Player p = e.getPlayer();
        Material type = b.getType();

        if (e.isCancelled()){
            return;
        }

        //block custom blocks
        if(type == Material.NOTE_BLOCK){
            return;
        }

        if (Settings.isShiftMining() && (!p.isSneaking())){
            return;
        }

        if (!EffectService.isEffectPlayer(p)){
            return;
        }

        ItemStack item = p.getInventory().getItemInMainHand();

        // Check if tool has usage rights and if they are depleted
        if(ToolUsageUtils.hasUsageRights(item) && !ToolUsageUtils.hasRemainingUsage(item)){
            p.sendMessage(ChatUtils.format(Settings.getToolNoUsage()));
            return;
        }

        CutShape shape = Settings.getCutShape(b, item);

        if (shape == null) {
            return;
        }

        TreeCutService.cutTree(p, shape);
    }
}
