package com.gplugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatColor;

import com.gplugins.managers.CustomItemManager;

import java.util.*;

public class GGiveCommand implements CommandExecutor, TabCompleter {
    
    private final CustomItemManager customItemManager;
    
    public GGiveCommand(CustomItemManager customItemManager) {
        this.customItemManager = customItemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est réservée aux joueurs.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /ggive <item> [quantité]");
            player.sendMessage(ChatColor.GRAY + "Items disponibles: " + 
                String.join(", ", customItemManager.getRegisteredItemIds()));
            return true;
        }
        
        String itemId = args[0].toLowerCase();
        int quantity = 1;
        
        // Vérifier la quantité si spécifiée
        if (args.length >= 2) {
            try {
                quantity = Integer.parseInt(args[1]);
                if (quantity <= 0 || quantity > 64) {
                    player.sendMessage(ChatColor.RED + "La quantité doit être entre 1 et 64.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Quantité invalide: " + args[1]);
                return true;
            }
        }
        
        // Créer l'item custom
        ItemStack item = customItemManager.createCustomItem(itemId);
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Item introuvable: " + itemId);
            player.sendMessage(ChatColor.GRAY + "Items disponibles: " + 
                String.join(", ", customItemManager.getRegisteredItemIds()));
            return true;
        }
        
        // Définir la quantité
        item.setAmount(quantity);
        
        // Donner l'item au joueur
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.YELLOW + "Inventaire plein! L'item a été jeté au sol.");
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
        
        CustomItemManager.CustomItemData itemData = customItemManager.getCustomItemData(itemId);
        player.sendMessage(ChatColor.GREEN + "Vous avez reçu " + quantity + "x " + itemData.getDisplayName());
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Auto-complétion pour les noms d'items
            String input = args[0].toLowerCase();
            
            for (String itemId : customItemManager.getRegisteredItemIds()) {
                if (itemId.startsWith(input)) {
                    completions.add(itemId);
                }
            }
        } else if (args.length == 2) {
            // Auto-complétion pour les quantités
            completions.addAll(Arrays.asList("1", "16", "32", "64"));
        }
        
        return completions;
    }
}