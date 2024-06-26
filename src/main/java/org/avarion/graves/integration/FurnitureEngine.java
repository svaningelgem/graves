package org.avarion.graves.integration;

import com.mira.furnitureengine.api.FurnitureAPI;
import org.avarion.graves.Graves;
import org.avarion.graves.data.EntityData;
import org.avarion.graves.listener.integration.furnitureengine.FurnitureBreakListener;
import org.avarion.graves.listener.integration.furnitureengine.FurnitureInteractListener;
import org.avarion.graves.manager.EntityDataManager;
import org.avarion.graves.type.Grave;
import org.avarion.graves.util.BlockFaceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class FurnitureEngine extends EntityDataManager {

    private final Graves plugin;
    private final FurnitureAPI furnitureAPI;
    private final FurnitureInteractListener furnitureInteractListener;
    private final FurnitureBreakListener furnitureBreakListener;

    public FurnitureEngine(Graves plugin) {
        super(plugin);

        this.plugin = plugin;
        this.furnitureAPI = new FurnitureAPI();
        this.furnitureInteractListener = new FurnitureInteractListener(plugin, this);
        this.furnitureBreakListener = new FurnitureBreakListener(this);

        registerListeners();
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(furnitureInteractListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(furnitureBreakListener, plugin);
    }

    public void unregisterListeners() {
        if (furnitureInteractListener != null) {
            HandlerList.unregisterAll(furnitureInteractListener);
        }

        if (furnitureBreakListener != null) {
            HandlerList.unregisterAll(furnitureBreakListener);
        }
    }

    public void createFurniture(Location location, Grave grave) {
        if (plugin.getConfigBool("furnitureengine.enabled", grave)) {
            String name = plugin.getConfigString("furnitureengine.name", grave, "");

            location.getBlock().setType(Material.AIR);

            if (placeFurniture(name, location, BlockFaceUtil.getBlockFaceRotation(BlockFaceUtil.getYawBlockFace(location.getYaw())))) {
                ItemFrame itemFrame = getItemFrame(location);

                if (itemFrame != null && location.getWorld() != null) {
                    createEntityData(location, itemFrame.getUniqueId(), grave.getUUID(), EntityData.Type.FURNITUREENGINE);
                    plugin.debugMessage("Placing FurnitureEngine furniture for "
                                        + grave.getUUID()
                                        + " at "
                                        + location.getWorld().getName()
                                        + ", "
                                        + (location.getBlockX() + 0.5)
                                        + "x, "
                                        + (location.getBlockY() + 0.5)
                                        + "y, "
                                        + (location.getBlockZ() + 0.5)
                                        + "z", 1);
                }
            }
        }
    }

    public void removeFurniture(Grave grave) {
        removeFurniture(getEntityDataMap(getLoadedEntityDataList(grave)));
    }

    public void removeFurniture(EntityData entityData) {
        removeFurniture(getEntityDataMap(Collections.singletonList(entityData)));
    }

    public void removeFurniture(@NotNull Map<EntityData, Entity> entityDataMap) {
        List<EntityData> entityDataList = new ArrayList<>();

        for (Map.Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
            breakFurniture(entry.getValue().getLocation());
            entry.getValue().remove();
            entityDataList.add(entry.getKey());
        }

        plugin.getDataManager().removeEntityData(entityDataList);
    }

    public @Nullable ItemFrame getItemFrame(Location location) {
        location = location.clone().add(0.0D, 1.0D, 0.0D);

        if (location.getWorld() != null) {
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.13D, 0.2D, 0.13D)) {
                if (entity instanceof ItemFrame) {
                    return (ItemFrame) entity;
                }
            }
        }

        return null;
    }

    private boolean placeFurniture(String name, Location location, Rotation rotation) {
        try {
            furnitureAPI.PlaceFurniture(name, location, rotation);

            return true;
        }
        catch (NoSuchMethodError ignored) {
            plugin.warningMessage("FurnitureAPI.PlaceFurniture() not found.");

            return false;
        }
    }

    private void breakFurniture(Location location) {
        try {
            furnitureAPI.BreakFurniture(location);
        }
        catch (NoSuchMethodError ignored) {
            plugin.warningMessage("FurnitureAPI.BreakFurniture() not found.");
        }
    }

}
