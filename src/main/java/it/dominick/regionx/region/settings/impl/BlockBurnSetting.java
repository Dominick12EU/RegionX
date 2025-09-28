package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockBurnSetting extends RegionSetting {

    public BlockBurnSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "BLOCK_BURN";
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (checkIfInRegion(event.getBlock().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        BlockBurnEvent blockBurnEvent = (BlockBurnEvent) event;
        Region region = getRegionManager().getRegion(blockBurnEvent.getBlock().getLocation());

        triggerAPIEvent(null, event, region);
        blockBurnEvent.setCancelled(true);
    }
}
