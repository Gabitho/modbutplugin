package tonpackage.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class UraniumPlaceListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // On ignore les clics sans bloc ciblé
        if (!event.hasBlock()) return;

        Player player = event.getPlayer();

        // Main utilisée (main ou offhand)
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        ItemStack item = (hand == EquipmentSlot.HAND)
            ? player.getInventory().getItemInMainHand()
            : player.getInventory().getItemInOffHand();

        if (item.getType() != Material.STICK) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return;

        // CustomModelData 1001 = bâton uranium
        if (meta.getCustomModelData() != 1001) return;

        event.setCancelled(true); // empêche les actions par défaut

        // Coordonnées du bloc ciblé
        Vector base = event.getClickedBlock().getLocation().toVector();
        BlockFace face = event.getBlockFace();
        Vector target = base.add(new Vector(face.getModX(), face.getModY(), face.getModZ()));

        // Arrondi des coordonnées pour coller à un bloc (entiers)
        int x = target.getBlockX();
        int y = target.getBlockY();
        int z = target.getBlockZ();

        // Summon l’item_display retexturisé
        String command = String.format(
            "summon item_display %d %d %d {item:{id:\"stick\",count:1,components:{custom_model_data:{strings:[\"1001\"]}}}}",
            x, y, z
        );

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
