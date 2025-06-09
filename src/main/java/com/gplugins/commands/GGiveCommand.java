package com.gplugins.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GGiveCommand implements CommandExecutor, TabCompleter {
    
    // Classe pour définir un item custom
    private static class CustomItem {
        private final String name;
        private final String displayName;
        private final Material material;
        private final String customModelData;
        private final int fallbackModelData;
        
        public CustomItem(String name, String displayName, Material material, String customModelData, int fallbackModelData) {
            this.name = name;
            this.displayName = displayName;
            this.material = material;
            this.customModelData = customModelData;
            this.fallbackModelData = fallbackModelData;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public String getCustomModelData() { return customModelData; }
        public int getFallbackModelData() { return fallbackModelData; }
    }
    
    // Map contenant tous les items custom
    private static final Map<String, CustomItem> CUSTOM_ITEMS = new HashMap<>();
    
    static {
        // Ajouter tous vos items custom ici
        CUSTOM_ITEMS.put("uranium", new CustomItem(
            "uranium", 
            "§aMinerai d'uranium", 
            Material.STICK, 
            "1001", 
            1001
        ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUsage: /ggive <item>");
        }
        
        String itemName = args[0].toLowerCase();
        CustomItem customItem = CUSTOM_ITEMS.get(itemName);
        
        if (customItem == null) {
            player.sendMessage("§cItem introuvable: " + itemName);
            player.sendMessage("§7Items disponibles: " + String.join(", ", CUSTOM_ITEMS.keySet()));
            return true;
        }

        // Créer l'item avec la commande vanilla
        try {
            String giveItemCommand = String.format(
                "give %s minecraft:%s[custom_name='{\"text\":\"%s\"}',custom_model_data={strings:[\"%s\"]}] 1",
                player.getName(),
                customItem.getMaterial().name().toLowerCase(),
                customItem.getDisplayName().replace("§", "\\u00a7"),
                customItem.getCustomModelData()
            );
            
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), giveItemCommand);
            
        } catch (Exception e) {
            // Fallback vers l'ancienne méthode si la commande échoue
            player.sendMessage("§cErreur avec la commande vanilla, utilisation du fallback...");
            
            ItemStack item = new ItemStack(customItem.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customItem.getFallbackModelData());
                meta.setDisplayName(customItem.getDisplayName());
                item.setItemMeta(meta);
            }
            player.getInventory().addItem(item);
        }

        player.sendMessage("§aTu as reçu: " + customItem.getDisplayName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (String itemName : CUSTOM_ITEMS.keySet()) {
                if (itemName.startsWith(input)) {
                    completions.add(itemName);
                }
            }
            
            return completions;
        }
        return Collections.emptyList();
    }
}