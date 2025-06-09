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

        // Exécuter la commande vanilla avec custom_model_data en string
        
        String uraniumCommand = String.format(
            "give %s minecraft:stick[custom_name='{\"text\":\"Minerai d uranium\"}',custom_model_data={strings:[\"1001\"]}] 1",
            player.getName()
        );
        
        // Exécuter la commande via le serveur
        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), uraniumCommand);
        
        // Exécuter la commande via le serveur
        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
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