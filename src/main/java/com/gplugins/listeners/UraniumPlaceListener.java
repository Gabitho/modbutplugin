package com.gplugins.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UraniumPlaceListener implements Listener {
    
    private final JavaPlugin plugin;
    private final Map<UUID, PendingPlacement> pendingPlacements = new HashMap<>();
    
    public UraniumPlaceListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    // Classe pour stocker les infos en attente
    private static class PendingPlacement {
        Location location;
        EquipmentSlot hand;
        long timestamp;
        
        PendingPlacement(Location location, EquipmentSlot hand) {
            this.location = location;
            this.hand = hand;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        // Vérifier l'item dans la main
        ItemStack item = event.getHand() == EquipmentSlot.HAND
            ? player.getInventory().getItemInMainHand()
            : player.getInventory().getItemInOffHand();

        if (item == null || item.getType() != Material.STICK) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Location placementLoc = clickedBlock.getLocation().add(0, 1, 0);
        
        // Stocker l'emplacement de placement en attente
        pendingPlacements.put(player.getUniqueId(), new PendingPlacement(placementLoc, event.getHand()));
        
        // Utiliser une approche avec fichier temporaire pour récupérer les données
        executeDataCommand(player);
        
        event.setCancelled(true);
    }
    
    private void executeDataCommand(Player player) {
        // Créer un fichier temporaire unique
        String fileName = "data_" + player.getUniqueId().toString().replace("-", "") + "_" + System.currentTimeMillis() + ".txt";
        File dataFile = new File(plugin.getDataFolder(), fileName);
        
        // Créer le dossier si nécessaire
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Commande pour écrire le résultat dans un fichier
        String dataCommand = String.format(
            "execute as %s run data get entity @s SelectedItem.components.minecraft:custom_model_data > %s",
            player.getName(),
            dataFile.getAbsolutePath()
        );
        
        // Exécuter la commande
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), dataCommand);
        
        // Lire le fichier après un court délai
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (dataFile.exists()) {
                        String content = Files.readString(dataFile.toPath());
                        processDataResult(player, content);
                        dataFile.delete(); // Nettoyer
                    } else {
                        // Fallback : utiliser le CustomModelData standard
                        processDataFallback(player);
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Erreur lors de la lecture du fichier data: " + e.getMessage());
                    processDataFallback(player);
                }
            }
        }.runTaskLater(plugin, 5L); // Attendre 5 ticks (0.25 secondes)
    }
    
    private void processDataResult(Player player, String dataResult) {
        PendingPlacement pending = pendingPlacements.get(player.getUniqueId());
        if (pending == null) return;
        
        // Vérifier si la réponse est trop ancienne (>5 secondes)
        if (System.currentTimeMillis() - pending.timestamp > 5000) {
            pendingPlacements.remove(player.getUniqueId());
            return;
        }
        
        // Parser le résultat
        String customModelData = parseCustomModelData(dataResult);
        
        if (customModelData != null && isValidUraniumData(customModelData)) {
            // Placer le bloc d'uranium
            placeUraniumBlock(pending.location, customModelData);
            
            // Retirer l'item de l'inventaire
            removeItemFromPlayer(player, pending.hand);
            
            player.sendMessage("§aBloc d'uranium placé !");
        } else {
            player.sendMessage("§cCet item ne peut pas être placé !");
        }
        
        // Nettoyer
        pendingPlacements.remove(player.getUniqueId());
    }
    
    private void processDataFallback(Player player) {
        PendingPlacement pending = pendingPlacements.get(player.getUniqueId());
        if (pending == null) return;
        
        // Utiliser l'ancienne méthode avec getCustomModelData()
        ItemStack item = pending.hand == EquipmentSlot.HAND
            ? player.getInventory().getItemInMainHand()
            : player.getInventory().getItemInOffHand();
            
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelData()) {
                String customModelData = String.valueOf(meta.getCustomModelData());
                
                if (isValidUraniumData(customModelData)) {
                    placeUraniumBlock(pending.location, customModelData);
                    removeItemFromPlayer(player, pending.hand);
                    player.sendMessage("§aBloc d'uranium placé !");
                } else {
                    player.sendMessage("§cCet item ne peut pas être placé !");
                }
            }
        }
        
        pendingPlacements.remove(player.getUniqueId());
    }
    
    private String parseCustomModelData(String dataResult) {
        if (dataResult == null || dataResult.trim().isEmpty()) return null;
        if (dataResult.contains("No data available") || dataResult.contains("has no item")) {
            return null;
        }
        
        // Chercher le format avec strings
        if (dataResult.contains("\"")) {
            int startIndex = dataResult.indexOf("\"") + 1;
            int endIndex = dataResult.indexOf("\"", startIndex);
            if (startIndex > 0 && endIndex > startIndex) {
                return dataResult.substring(startIndex, endIndex);
            }
        }
        
        // Chercher les nombres
        String[] parts = dataResult.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return part;
            }
        }
        
        return null;
    }
    
    private boolean isValidUraniumData(String customModelData) {
        // Ajouter tous vos minerais ici
        return "uranium".equals(customModelData) || 
               "1001".equals(customModelData) ||
               "plutonium".equals(customModelData) ||
               "thorium".equals(customModelData) ||
               "2001".equals(customModelData);
    }
    
    private void placeUraniumBlock(Location location, String customModelData) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // 1. Placer le bloc de base
        location.getWorld().getBlockAt(x, y, z).setType(Material.DIAMOND_ORE);
        
        // 2. Summon item_display avec le bon custom_model_data
        String displayData = customModelData.matches("\\d+") ? customModelData : "\"" + customModelData + "\"";
        String summonCommand = String.format(
            "summon item_display %d.49 %d %d.49 {item:{id:\"stick\",count:1,components:{custom_model_data:%s}},transformation:{scale:[1.02f,1.02f,1.02f]}}",
            x, y, z, displayData
        );
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), summonCommand);
    }
    
    private void removeItemFromPlayer(Player player, EquipmentSlot hand) {
        ItemStack item = hand == EquipmentSlot.HAND
            ? player.getInventory().getItemInMainHand()
            : player.getInventory().getItemInOffHand();
            
        if (item != null && item.getType() == Material.STICK) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                if (hand == EquipmentSlot.HAND) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }
        }
    }
}