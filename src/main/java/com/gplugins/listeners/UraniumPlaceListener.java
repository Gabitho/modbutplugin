package com.gplugins.listeners; // ← Remplace ça par le vrai package si besoin

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

public class UraniumPlaceListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        // Supporte les deux mains comme un bloc
        ItemStack item = event.getHand() == EquipmentSlot.HAND
            ? player.getInventory().getItemInMainHand()
            : player.getInventory().getItemInOffHand();

        if (item == null || item.getType() != Material.STICK) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return;

        if (Integer.toString(meta.getCustomModelData()) != "1001") return; // modèle uranium

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Location loc = clickedBlock.getLocation().add(0, 1, 0);

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        // 1. Placer diamond_ore (on le retexturera via ressource pack comme uranium)
        loc.getWorld().getBlockAt(x, y, z).setType(Material.DIAMOND_ORE);

        // 2. Summon item_display par-dessus avec la bonne échelle
        String summonCommand = String.format(
            "summon item_display %d %d %d {item:{id:\"stick\",count:1,components:{custom_model_data:{strings:[\"1001\"]}}},{transformation:{scale:[1.02f,1.02f,1.02f]}}",
            x-0.51, y, z-0.51
        );

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), summonCommand);
    }
}
