package org.avarion.graves.listener;

import org.avarion.graves.Graves;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.jetbrains.annotations.NotNull;

public class HangingBreakListener implements Listener {

    private final Graves plugin;

    public HangingBreakListener(Graves plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHangingBreak(@NotNull HangingBreakEvent event) {
        event.setCancelled(event.getEntity() instanceof ItemFrame
                           && plugin.getEntityDataManager().getGrave(event.getEntity()) != null);
    }

}
