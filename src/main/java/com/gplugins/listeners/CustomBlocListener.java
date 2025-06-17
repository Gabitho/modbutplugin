package com.gplugins.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.gplugins.managers.CustomItemManager;
import com.gplugins.managers.CustomBlockManager;

public class CustomBlockListener implements Listener {
    
    private final JavaPlugin plugin;
    private final CustomItemManager customItemManager;
    private final CustomBlockManager customBlockManager;
    
    public CustomBlockListener(JavaPlugin plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
        this.customBlockManager = new CustomBlockManager(plugin);
    }
    
    /**
     * Gère le placement des blocs personnalisés
     */
    @EventHandler
    public void onBlockPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // Éviter les doubles événements
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Vérifier si c'est un item custom
        if (!customItemManager.isCustomItem(item)) return;
        
        String itemId = customItemManager.getCustomItemId(item);
        CustomItemManager.CustomItemData itemData = customItemManager.getCustomItemData(itemId);
        
        // Vérifier si c'est un bloc placable
        if (itemData.getItemType() != CustomItemManager.ItemType.BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        // Calculer la position où placer le bloc
        Location placeLocation = clickedBlock.getLocation().add(
            event.getBlockFace().getModX(),
            event.getBlockFace().getModY(),
            event.getBlockFace().getModZ()
        );
        
        // Vérifier si on peut placer le bloc
        if (!placeLocation.getBlock().getType().isAir()) return;
        
        // Placer le bloc custom
        customBlockManager.placeCustomBlock(placeLocation, itemId, itemData);
        
        // Retirer l'item de l'inventaire (mode survie)
        if (player.getGameMode() != GameMode.CREATIVE) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
        
        // Annuler l'événement pour éviter les interactions normales
        event.setCancelled(true);
        
        player.sendMessage(ChatColor.GREEN + "Bloc " + itemData.getDisplayName() + " placé!");
    }
    
    /**
     * Gère la destruction des blocs personnalisés
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Vérifier si c'est un bloc custom
        if (!customBlockManager.isCustomBlock(block.getLocation())) return;
        
        String itemId = customBlockManager.getCustomBlockId(block.getLocation());
        if (itemId == null) return;
        
        CustomItemManager.CustomItemData itemData = customItemManager.getCustomItemData(itemId);
        if (itemData == null) return;
        
        // Supprimer le bloc custom
        customBlockManager.removeCustomBlock(block.getLocation());
        
        // Empêcher le drop normal du bloc
        event.setCancelled(true);
        
        // Détruire le bloc manuellement
        block.setType(Material.AIR);
        
        // Faire dropper l'item custom si on n'est pas en créatif
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack customItem = customItemManager.createCustomItem(itemId);
            if (customItem != null) {
                block.getWorld().dropItemNaturally(block.getLocation(), customItem);
            }
        }
        
        player.sendMessage(ChatColor.YELLOW + "Bloc " + itemData.getDisplayName() + " détruit!");
    }
}