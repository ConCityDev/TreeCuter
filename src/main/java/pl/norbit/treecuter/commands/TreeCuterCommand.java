package pl.norbit.treecuter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.norbit.treecuter.config.Settings;
import pl.norbit.treecuter.service.ToggleService;
import pl.norbit.treecuter.utils.ChatUtils;
import pl.norbit.treecuter.utils.PermissionsUtils;

import java.util.ArrayList;
import java.util.List;

public class TreeCuterCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0) {
            sendInfo(sender);
            return true;
        }
        String arg = args[0].toUpperCase();

        switch (arg) {
            case "RELOAD" -> reload(sender);
            case "GET" -> {
                if(args.length < 2){
                    sendInfo(sender);
                    return true;
                }

                String key = args[1];

                // Default values if not provided
                int maxUsage = -1; // -1 means unlimited
                int usedUsage = 0;
                
                // Parse maxUsage if provided
                if(args.length >= 3) {
                    try {
                        maxUsage = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtils.format(Settings.getGiveInvalidMax()));
                        return true;
                    }
                }
                
                // Parse usedUsage if provided
                if(args.length >= 4) {
                    try {
                        usedUsage = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtils.format(Settings.getGiveInvalidUsed()));
                        return true;
                    }
                }

                get(sender, key, maxUsage, usedUsage);
            }
            case "GIVE" -> {
                if(args.length < 3){
                    sender.sendMessage(ChatUtils.format(Settings.getGiveUsage()));
                    return true;
                }

                String targetName = args[1];
                String key = args[2];

                // Default values if not provided
                int maxUsage = -1; // -1 means unlimited
                int usedUsage = 0;

                // Parse maxUsage if provided
                if(args.length >= 4) {
                    try {
                        maxUsage = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtils.format(Settings.getGiveInvalidMax()));
                        return true;
                    }
                }

                // Parse usedUsage if provided
                if(args.length >= 5) {
                    try {
                        usedUsage = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtils.format(Settings.getGiveInvalidUsed()));
                        return true;
                    }
                }

                give(sender, targetName, key, maxUsage, usedUsage);
            }
            case "TOGGLE" -> toggle(sender);
            default -> sendInfo(sender);
        }
        return true;
    }

    private void reload(CommandSender sender){
        if(hasNotPermission(sender, "reload")) return;

        sender.sendMessage(ChatUtils.format(Settings.getReloadStart()));
        Settings.loadConfig(true);
        sender.sendMessage(ChatUtils.format(Settings.getReloadEnd()));
    }

    private void get(CommandSender sender, String key, int maxUsage, int usedUsage){
        if(hasNotPermission(sender, "get")) return;

        var p = getPlayer(sender);

        if(p == null){
            return;
        }

        // Debug: Show available tools
        var availableTools = Settings.getCustomToolKeys();
        if(availableTools.isEmpty()) {
            sender.sendMessage(ChatUtils.format("&c&l» &7Debug: No custom tools configured!"));
            sender.sendMessage(ChatUtils.format("&7Please check your config.yml and ensure custom-tool sections are properly configured."));
            return;
        }

        Settings.getCustomToolForKey(key, maxUsage, usedUsage)
                .ifPresentOrElse(customTool -> {
                    p.getInventory().addItem(customTool);
                    p.sendMessage(ChatUtils.format(Settings.getToolGet()));

                    // Send usage info if usage rights are set
                    if(maxUsage > 0) {
                        String usageMsg = Settings.getToolUsageInfo()
                                .replace("{used}", String.valueOf(usedUsage))
                                .replace("{max}", String.valueOf(maxUsage));
                        p.sendMessage(ChatUtils.format(usageMsg));
                    }
                }, () -> {
                    p.sendMessage(ChatUtils.format(Settings.getToolNotFound()));
                    // Debug: Show available tool IDs
                    p.sendMessage(ChatUtils.format("&7Available tool IDs: &e" + String.join(", ", availableTools)));
                });
    }

    private void give(CommandSender sender, String targetName, String key, int maxUsage, int usedUsage){
        if(hasNotPermission(sender, "give")) return;

        // Find target player
        Player target = Bukkit.getPlayerExact(targetName);
        if(target == null || !target.isOnline()) {
            sender.sendMessage(ChatUtils.format(Settings.getGivePlayerNotFound()));
            return;
        }

        // Debug: Show available tools
        var availableTools = Settings.getCustomToolKeys();
        if(availableTools.isEmpty()) {
            sender.sendMessage(ChatUtils.format("&c&l» &7Debug: No custom tools configured!"));
            sender.sendMessage(ChatUtils.format("&7Please check your config.yml and ensure custom-tool sections are properly configured."));
            return;
        }

        Settings.getCustomToolForKey(key, maxUsage, usedUsage)
                .ifPresentOrElse(customTool -> {
                    target.getInventory().addItem(customTool);
                    target.sendMessage(ChatUtils.format(Settings.getToolGet()));

                    // Send usage info if usage rights are set
                    if(maxUsage > 0) {
                        String usageMsg = Settings.getToolUsageInfo()
                                .replace("{used}", String.valueOf(usedUsage))
                                .replace("{max}", String.valueOf(maxUsage));
                        target.sendMessage(ChatUtils.format(usageMsg));
                    }

                    // Notify sender
                    String successMsg = Settings.getGiveSuccess()
                            .replace("{player}", target.getName());
                    sender.sendMessage(ChatUtils.format(successMsg));
                }, () -> {
                    sender.sendMessage(ChatUtils.format(Settings.getToolNotFound()));
                    // Debug: Show available tool IDs
                    sender.sendMessage(ChatUtils.format("&7Available tool IDs: &e" + String.join(", ", availableTools)));
                });
    }

    private void toggle(CommandSender sender){
        if(hasNotPermission(sender, "toggle")){
            return;
        }

        var p = getPlayer(sender);

        if(p == null){
            return;
        }

        boolean status = ToggleService.changeToggle(p.getUniqueId());

        String message = status ? Settings.getToggleMessageOn() : Settings.getToggleMessageOff();

        p.sendMessage(ChatUtils.format(message));
    }

    private boolean hasNotPermission(CommandSender sender, String permission){
        String perm = "treecuter." + permission;

        if(!PermissionsUtils.hasPermission(sender, perm)){
            sender.sendMessage(ChatUtils.format(Settings.getPermissionMessage()));
            return true;
        }
        return false;
    }

    private Player getPlayer(CommandSender sender){
        if((sender instanceof Player player)) {
            return player;
        }
        sender.sendMessage(ChatUtils.format(Settings.getConsoleMessage()));
        return null;
    }

    private static void sendInfo(CommandSender sender){
        if(!sender.hasPermission("treecuter.help")){
            sender.sendMessage(ChatUtils.format(Settings.getPermissionMessage()));
            return;
        }

        Settings.getHelpMessage()
                .stream()
                .map(ChatUtils::format)
                .forEach(sender::sendMessage);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1) {
            return List.of("reload", "get", "give", "toggle");
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("get")) {
                return Settings.getCustomToolKeys();
            }
            if(args[0].equalsIgnoreCase("give")) {
                // Return online player names
                List<String> playerNames = new ArrayList<>();
                for(Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        }

        if(args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Settings.getCustomToolKeys();
        }

        return List.of();
    }
}
