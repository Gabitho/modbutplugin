package com.gplugins.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.*;

public class CustomBlockManager {
    
    private final JavaPlugin plugin;
    private final Map<String, UUID> placedBlocks; // location -> item_display UUID
    
    public CustomBlockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.placedBlocks = new HashMap<>();
    }
    
    /**
     * Place un bloc personnalisé dans le monde
     */
    public void placeCustomBlock(Location location, String itemId, CustomItemManager.CustomItemData itemData) {
        World world = location.getWorld();
        if (world == null) return;
        
        // 1. Placer un bloc de base (invisible ou semi-transparent)
        Block block = location.getBlock();
        
        // Utiliser barrier comme bloc de base (invisible mais solide)
        // Alternative: utiliser glass avec un resource pack pour le rendre invisible
        block.setType(Material.BARRIER);
        
        // 2. Créer un item_display pour l'apparence visuelle
        Location displayLocation = location.clone().add(0.5, 0.5, 0.5); // Centrer dans le bloc
        
        ItemDisplay itemDisplay = world.spawn(displayLocation, ItemDisplay.class, display -> {
            // Créer l'item à afficher
            ItemStack displayItem = new ItemStack(itemData.getBaseMaterial());
            displayItem.getItemMeta().setCustomModelData(itemData.getCustomModelData());
            display.setItemStack(displayItem);
            
            // Configurer la transformation (taille, rotation)
            Transformation transformation = new Transformation(
                new Vector3f(0, 0, 0),           // Translation
                new org.joml.AxisAngle4f(0, 0, 0, 1), // Rotation gauche
                new Vector3f(1.02f, 1.02f, 1.02f),    // Échelle (légèrement plus grand)
                new org.joml.AxisAngle4f(0, 0, 0, 1)  // Rotation droite
            );
            display.setTransformation(transformation);
            
            // Autres paramètres
            display.setBillboard(ItemDisplay.Billboard.FIXED); // Ne pas tourner vers le joueur
            display.setGlowing(false);
            display.setInvulnerable(true); // Empêcher la destruction directe
        });
        
        // 3. Enregistrer le bloc placé
        String locationKey = locationToString(location);
        placedBlocks.put(locationKey, itemDisplay.getUniqueId());
        
        plugin.getLogger().info("Bloc custom placé: " + itemId + " à " + locationKey);
    }
    
    /**
     * Supprime un bloc personnalisé
     */
    public void removeCustomBlock(Location location) {
        String locationKey = locationToString(location);
        UUID displayId = placedBlocks.get(locationKey);
        
        if (displayId != null) {
            // Supprimer l'item_display
            World world = location.getWorld();
            if (world != null) {
                ItemDisplay display = (ItemDisplay) Bukkit.getEntity(displayId);
                if (display != null) {
                    display.remove();
                }
            }
            
            // Retirer de notre registre
            placedBlocks.remove(locationKey);
            
            plugin.getLogger().info("Bloc custom supprimé à " + locationKey);
        }
    }
    
    /**
     * Vérifie si un bloc est un bloc personnalisé
     */
    public boolean isCustomBlock(Location location) {
        String locationKey = locationToString(location);
        return placedBlocks.containsKey(locationKey);
    }
    
    /**
     * Récupère l'ID d'un bloc personnalisé
     * Note: Cette méthode nécessiterait un système de stockage plus complexe
     * pour associer les locations aux IDs d'items. Pour l'instant, on peut 
     * examiner l'item_display pour retrouver l'ID.
     */
    public String getCustomBlockId(Location location) {
        String locationKey = locationToString(location);
        UUID displayId = placedBlocks.get(locationKey);
        
        if (displayId != null) {
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(displayId);
            if (display != null) {
                ItemStack item = display.getItemStack();
                if (item != null && item.hasItemMeta()) {
                    // Ici, tu pourrais vérifier le CustomModelData ou d'autres propriétés
                    // pour déterminer quel item c'est
                    int customModelData = item.getItemMeta().getCustomModelData();
                    
                    // Exemple simple: mapper le CustomModelData vers l'ID
                    switch (customModelData) {
                        case 1001: return "uranium_ore";
                        // Ajouter d'autres mappings ici
                        default: return null;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Convertit une location en string unique
     */
    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + 
               location.getBlockX() + "," + 
               location.getBlockY() + "," + 
               location.getBlockZ();
    }
    
    /**
     * Convertit une string en location
     */
    private Location stringToLocation(String locationString) {
        String[] parts = locationString.split(",");
        if (parts.length != 4) return null;
        
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Nettoie les blocs orphelins (item_display sans bloc correspondant)
     */
    public void cleanupOrphanedBlocks() {
        Iterator<Map.Entry<String, UUID>> iterator = placedBlocks.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, UUID> entry = iterator.next();
            UUID displayId = entry.getValue();
            
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(displayId);
            if (display == null || display.isDead()) {
                iterator.remove();
                plugin.getLogger().info("Bloc orphelin nettoyé: " + entry.getKey());
            }
        }
    }
    
    /**
     * Récupère tous les blocs personnalisés placés
     */
    public Map<String, UUID> getPlacedBlocks() {
        return new HashMap<>(placedBlocks);
    }
}