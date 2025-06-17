package com.gplugins;

import org.bukkit.plugin.java.JavaPlugin;
import com.gplugins.commands.GGiveCommand;
import com.gplugins.listeners.CustomBlockListener;
import com.gplugins.managers.CustomItemManager;

public class Main extends JavaPlugin {
    
    private static Main instance;
    private CustomItemManager customItemManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialiser le gestionnaire des items custom
        customItemManager = new CustomItemManager(this);
        
        getLogger().info("GPlugins démarré !");
        
        // Enregistrer les commandes
        getCommand("ggive").setExecutor(new GGiveCommand(customItemManager));
        
        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new CustomBlockListener(this, customItemManager), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("GPlugins arrêté.");
    }
    
    public static Main getInstance() {
        return instance;
    }
    
    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }
}