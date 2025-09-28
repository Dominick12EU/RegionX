package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockSpreadSetting extends RegionSetting {

    public BlockSpreadSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "BLOCK_SPREAD";
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (shouldProcess(null, event.getBlock().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        BlockSpreadEvent blockSpreadEvent = (BlockSpreadEvent) event;
        Region region = getRegionManager().getRegion(blockSpreadEvent.getBlock().getLocation());

        triggerAPIEvent(null, event, region);
        blockSpreadEvent.setCancelled(true);
    }
}
