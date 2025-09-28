package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockExplosionsSetting extends RegionSetting {

    public BlockExplosionsSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "BLOCK_EXPLOSIONS";
    }

    @EventHandler
    public void onExplodeBlockEvent(BlockExplodeEvent event) {
        if (shouldProcess(null, event.getBlock().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        BlockExplodeEvent explodeEvent = (BlockExplodeEvent) event;
        Region region = getRegionManager().getRegion(((BlockExplodeEvent) event).getBlock().getLocation());

        triggerAPIEvent(null, event, region);
        explodeEvent.setCancelled(true);
    }
}
