package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemPickupSetting extends RegionSetting {

    public ItemPickupSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "ITEM_PICKUP";
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (shouldProcess(player, event.getItem().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        PlayerPickupItemEvent pickupEvent = (PlayerPickupItemEvent) event;
        Player player = pickupEvent.getPlayer();
        Region region = getRegionManager().getRegion(((PlayerPickupItemEvent) event)
                .getItem().getLocation());

        triggerAPIEvent(player, event, region);
        pickupEvent.setCancelled(true);
    }
}