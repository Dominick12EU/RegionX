package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LeavesDecaySetting extends RegionSetting {

    public LeavesDecaySetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "LEAVES_DECAY";
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (shouldProcess(null, event.getBlock().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        LeavesDecayEvent leavesDecayEvent = (LeavesDecayEvent) event;
        Region region = getRegionManager().getRegion(leavesDecayEvent.getBlock().getLocation());

        triggerAPIEvent(null, event, region);
        leavesDecayEvent.setCancelled(true);
    }
}
