package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDropSetting extends RegionSetting {

    public ItemDropSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "ITEM_DROP";
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (checkIfInRegion(event.getPlayer().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        PlayerDropItemEvent dropEvent = (PlayerDropItemEvent) event;
        Player player = dropEvent.getPlayer();
        Region region = getRegionManager().getRegion(player.getLocation());

        triggerAPIEvent(player, event, region);
        dropEvent.setCancelled(true);
    }
}