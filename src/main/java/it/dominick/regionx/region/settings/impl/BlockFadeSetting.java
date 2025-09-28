package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockFadeSetting extends RegionSetting {

    public BlockFadeSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "BLOCK_FADE";
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (checkIfInRegion(event.getBlock().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        BlockFadeEvent blockFadeEvent = (BlockFadeEvent) event;
        Region region = getRegionManager().getRegion(blockFadeEvent.getBlock().getLocation());

        triggerAPIEvent(null, event, region);
        blockFadeEvent.setCancelled(true);
    }
}
