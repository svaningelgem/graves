package org.avarion.graves.listener.integration.oraxen;

import org.avarion.graves.Graves;
import org.avarion.graves.integration.Oraxen;
import org.avarion.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerInteractEntityListener implements Listener {

    private final Graves plugin;
    private final Oraxen oraxen;

    public PlayerInteractEntityListener(Graves plugin, Oraxen oraxen) {
        this.plugin = plugin;
        this.oraxen = oraxen;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnitureInteract(@NotNull PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity instanceof ItemFrame) {
            Grave grave = oraxen.getGrave(entity);

            event.setCancelled(grave != null && plugin.getGraveManager()
                                                      .openGrave(event.getPlayer(), entity.getLocation(), grave));
        }
    }

}
