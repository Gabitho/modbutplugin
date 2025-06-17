package com.gplugins.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public class CustomItemManager {
    
    private final JavaPlugin plugin;
    private final NamespacedKey customItemKey;
    private final Map<String, CustomItemData> registeredItems;
    
    public CustomItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customItemKey = new NamespacedKey(plugin, "custom_item");
        this.registeredItems = new HashMap<>();
        
        // Enregistrer tous les items custom
        registerDefaultItems();
    }
    
    /**
     * Classe interne pour stocker les données d'un item custom
     */
    public static class CustomItemData {
        private final String id;
        private final String displayName;
        private final Material baseMaterial;
        private final int customModelData;
        private final List<String> lore;
        private final ItemType itemType;
        
        public CustomItemData(String id, String displayName, Material baseMaterial, 
                            int customModelData, List<String> lore, ItemType itemType) {
            this.id = id;
            this.displayName = displayName;
            this.baseMaterial = baseMaterial;
            this.customModelData = customModelData;
            this.lore = lore != null ? lore : new ArrayList<>();
            this.itemType = itemType;
        }
        
        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Material getBaseMaterial() { return baseMaterial; }
        public int getCustomModelData() { return customModelData; }
        public List<String> getLore() { return lore; }
        public ItemType getItemType() { return itemType; }
    }
    
    /**
     * Enum pour les types d'items
     */
    public enum ItemType {
        BLOCK,      // Bloc placable
        TOOL,       // Outil
        WEAPON,     // Arme
        ARMOR,      // Armure
        CONSUMABLE, // Consommable
        MISC        // Divers
    }
    
    /**
     * Enregistre les items par défaut
     */
    private void registerDefaultItems() {
        // Uranium (bloc)
        registerItem(new CustomItemData(
            "uranium_ore",
            ChatColor.GREEN + "Minerai d'Uranium",
            Material.STICK,
            1001,
            Arrays.asList(
                ChatColor.GRAY + "Un minerai radioactif rare",
                ChatColor.YELLOW + "Utilisé pour l'énergie nucléaire"
            ),
            ItemType.BLOCK
        ));
        
        // Épée en uranium (exemple d'arme)
        registerItem(new CustomItemData(
            "uranium_sword",
            ChatColor.GREEN + "Épée d'Uranium",
            Material.IRON_SWORD,
            1002,
            Arrays.asList(
                ChatColor.GRAY + "Une épée forgée avec de l'uranium",
                ChatColor.RED + "Dégâts: +2 par rapport au fer",
                ChatColor.YELLOW + "Effet: Poison sur les ennemis"
            ),
            ItemType.WEAPON
        ));
        
        // Lingot d'uranium
        registerItem(new CustomItemData(
            "uranium_ingot",
            ChatColor.GREEN + "Lingot d'Uranium",
            Material.IRON_INGOT,
            1003,
            Arrays.asList(
                ChatColor.GRAY + "Uranium raffiné",
                ChatColor.YELLOW + "Matériau de craft avancé"
            ),
            ItemType.MISC
        ));
    }
    
    /**
     * Enregistre un nouvel item custom
     */
    public void registerItem(CustomItemData itemData) {
        registeredItems.put(itemData.getId(), itemData);
        plugin.getLogger().info("Item custom enregistré: " + itemData.getId());
    }
    
    /**
     * Crée un ItemStack à partir d'un ID d'item custom
     */
    public ItemStack createCustomItem(String itemId) {
        CustomItemData itemData = registeredItems.get(itemId);
        if (itemData == null) {
            plugin.getLogger().warning("Item custom introuvable: " + itemId);
            return null;
        }
        
        ItemStack item = new ItemStack(itemData.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Définir le nom d'affichage
            meta.setDisplayName(itemData.getDisplayName());
            
            // Définir la lore
            if (!itemData.getLore().isEmpty()) {
                meta.setLore(itemData.getLore());
            }
            
            // Définir le custom model data
            meta.setCustomModelData(itemData.getCustomModelData());
            
            // IMPORTANT: Stocker l'ID dans les persistent data
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, itemId);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Vérifie si un ItemStack est un item custom
     */
    public boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(customItemKey, PersistentDataType.STRING);
    }
    
    /**
     * Récupère l'ID d'un item custom
     */
    public String getCustomItemId(ItemStack item) {
        if (!isCustomItem(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(customItemKey, PersistentDataType.STRING);
    }
    
    /**
     * Récupère les données d'un item custom
     */
    public CustomItemData getCustomItemData(String itemId) {
        return registeredItems.get(itemId);
    }
    
    /**
     * Récupère les données d'un ItemStack custom
     */
    public CustomItemData getCustomItemData(ItemStack item) {
        String itemId = getCustomItemId(item);
        return itemId != null ? getCustomItemData(itemId) : null;
    }
    
    /**
     * Récupère tous les IDs des items enregistrés
     */
    public Set<String> getRegisteredItemIds() {
        return registeredItems.keySet();
    }
    
    /**
     * Récupère tous les items enregistrés
     */
    public Map<String, CustomItemData> getRegisteredItems() {
        return new HashMap<>(registeredItems);
    }
}