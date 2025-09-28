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
        if (checkIfInRegion(event.getEntity().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        HangingBreakEvent hangingBreakEvent = (HangingBreakEvent) event;
        Region region = getRegionManager().getRegion(hangingBreakEvent.getEntity().getLocation());

        triggerAPIEvent(null, event, region);
        hangingBreakEvent.setCancelled(true);
    }
}
