package com.gplugins;

import org.bukkit.plugin.java.JavaPlugin;
import com.gplugins.commands.GGiveCommand;
import com.gplugins.listeners.UraniumPlaceListener;


public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("GPlugins démarré !");
        getCommand("ggive").setExecutor(new GGiveCommand());
        getServer().getPluginManager().registerEvents(new UraniumPlaceListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("GPlugins arrêté.");
    }
}

