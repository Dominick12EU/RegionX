package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemFrameDestroySetting extends RegionSetting {

    public ItemFrameDestroySetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "ITEMFRAME_DESTROY";
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        Player player = event.getEntity() instanceof Player ? (Player)
                event.getEntity() : null;
        if (shouldProcess(player, event.getEntity().getLocation())) {
            handleEvent(event, player);
        }
    }

    @Override
    protected void handleEvent(Event event) {}

    protected void handleEvent(HangingBreakEvent event, Player player) {
        Region region = getRegionManager().getRegion(event.getEntity().getLocation());
        triggerAPIEvent(player, event, region);
        event.setCancelled(true);
    }
}
