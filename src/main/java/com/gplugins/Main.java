package com.monserveur;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class Main extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        this.getCommand("givehead").setExecutor(this);
        getLogger().info("CustomHeadsPlugin activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomHeadsPlugin désactivé !");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("Utilisation : /givehead <custom_model_data>");
            return true;
        }

        int modelData;
        try {
            modelData = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("L'ID doit être un nombre.");
            return true;
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelData);
            meta.setDisplayName("Tête Custom #" + modelData);
            head.setItemMeta(meta);
        }

        player.getInventory().addItem(head);
        player.sendMessage("Tu as reçu une tête avec CustomModelData = " + modelData);
        return true;
    }
}
