package com.gplugins.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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
        long timestamp;
        
        PendingPlacement(Location location) {
            this.location = location;
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
        pendingPlacements.put(player.getUniqueId(), new PendingPlacement(placementLoc));
        
        // Créer une commande /data pour récupérer les custom_model_data
        String slot = event.getHand() == EquipmentSlot.HAND ? "mainhand" : "offhand";
        String dataCommand = String.format("data get entity %s SelectedItem.components.minecraft:custom_model_data", player.getName());
        
        // Exécuter la commande et traiter le résultat
        new BukkitRunnable() {
            @Override
            public void run() {
                // Exécuter la commande data
                Bukkit.dispatchCommand(new DataCommandSender(player), dataCommand);
            }
        }.runTask(plugin);
        
        event.setCancelled(true);
    }
    
    // CommandSender personnalisé pour capturer le résultat de /data
    private class DataCommandSender implements CommandSender {
        private final Player originalPlayer;
        
        public DataCommandSender(Player player) {
            this.originalPlayer = player;
        }
        
        @Override
        public void sendMessage(String message) {
            // Traiter le résultat de la commande /data
            processDataResult(originalPlayer, message);
        }
        
        @Override
        public void sendMessage(String[] messages) {
            for (String message : messages) {
                sendMessage(message);
            }
        }
        
        @Override
        public Server getServer() {
            return Bukkit.getServer();
        }
        
        @Override
        public String getName() {
            return "DataCommandSender";
        }
        
        @Override
        public Spigot spigot() {
            return originalPlayer.spigot();
        }
        
        @Override
        public boolean isPermissionSet(String name) {
            return true;
        }
        
        @Override
        public boolean isPermissionSet(org.bukkit.permissions.Permission perm) {
            return true;
        }
        
        @Override
        public boolean hasPermission(String name) {
            return true;
        }
        
        @Override
        public boolean hasPermission(org.bukkit.permissions.Permission perm) {
            return true;
        }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, String name, boolean value) {
            return null;
        }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin) {
            return null;
        }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, String name, boolean value, int ticks) {
            return null;
        }
        
        @Override
        public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, int ticks) {
            return null;
        }
        
        @Override
        public void removeAttachment(org.bukkit.permissions.PermissionAttachment attachment) {}
        
        @Override
        public void recalculatePermissions() {}
        
        @Override
        public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() {
            return new java.util.HashSet<>();
        }
        
        @Override
        public boolean isOp() {
            return true;
        }
        
        @Override
        public void setOp(boolean value) {}
    }
    
    private void processDataResult(Player player, String dataResult) {
        PendingPlacement pending = pendingPlacements.get(player.getUniqueId());
        if (pending == null) return;
        
        // Vérifier si la réponse est trop ancienne (>2 secondes)
        if (System.currentTimeMillis() - pending.timestamp > 2000) {
            pendingPlacements.remove(player.getUniqueId());
            return;
        }
        
        // Parser le résultat de /data
        String customModelData = parseCustomModelData(dataResult);
        
        if (customModelData != null && isValidUraniumData(customModelData)) {
            // Placer le bloc d'uranium
            placeUraniumBlock(pending.location, customModelData);
            
            // Retirer l'item de l'inventaire
            removeItemFromPlayer(player);
            
            player.sendMessage("§aBloc d'uranium placé !");
        }
        
        // Nettoyer
        pendingPlacements.remove(player.getUniqueId());
    }
    
    private String parseCustomModelData(String dataResult) {
        // Parser les différents formats possibles :
        // Format 1: "SelectedItem.components.minecraft:custom_model_data: {strings: [\"uranium\"]}"
        // Format 2: "SelectedItem.components.minecraft:custom_model_data: 1001"
        // Format 3: "No data available"
        
        if (dataResult.contains("No data available") || dataResult.contains("has no item")) {
            return null;
        }
        
        // Chercher le format avec strings
        if (dataResult.contains("strings:")) {
            int startIndex = dataResult.indexOf("[\"") + 2;
            int endIndex = dataResult.indexOf("\"]", startIndex);
            if (startIndex > 1 && endIndex > startIndex) {
                return dataResult.substring(startIndex, endIndex);
            }
        }
        
        // Chercher le format numérique
        if (dataResult.contains("custom_model_data:")) {
            String[] parts = dataResult.split("custom_model_data:");
            if (parts.length > 1) {
                String numberPart = parts[1].trim();
                // Extraire le nombre
                numberPart = numberPart.replaceAll("[^0-9]", "");
                if (!numberPart.isEmpty()) {
                    return numberPart;
                }
            }
        }
        
        return null;
    }
    
    private boolean isValidUraniumData(String customModelData) {
        // Vérifier si c'est de l'uranium
        return || "1001".equals(customModelData);
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
    
    private void removeItemFromPlayer(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (mainHand != null && mainHand.getType() == Material.STICK && mainHand.hasItemMeta()) {
            if (mainHand.getAmount() > 1) {
                mainHand.setAmount(mainHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        } else if (offHand != null && offHand.getType() == Material.STICK && offHand.hasItemMeta()) {
            if (offHand.getAmount() > 1) {
                offHand.setAmount(offHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        }
    }
}