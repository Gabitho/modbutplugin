package com.gplugins.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class GGiveCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("uranium")) {
            player.sendMessage("Usage: /ggive uranium");
            return true;
        }

        // Création de l'item custom (bâton uranium)
        ItemStack uraniumStick = new ItemStack(Material.STICK);
        ItemMeta meta = uraniumStick.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData("1001"); // À associer dans le resource pack
            meta.setDisplayName("§aMinerai d'uranium");
            uraniumStick.setItemMeta(meta);
        }

        player.getInventory().addItem(uraniumStick);
        player.sendMessage("§aTu as reçu un bâton d'uranium !");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("uranium");
        }
        return Collections.emptyList();
    }
}
