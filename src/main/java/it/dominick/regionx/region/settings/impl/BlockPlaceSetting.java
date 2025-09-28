package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockPlaceSetting extends RegionSetting {

    public BlockPlaceSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "BLOCK_PLACE";
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (shouldProcess(player, event.getBlock().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        BlockPlaceEvent blockPlaceEvent = (BlockPlaceEvent) event;
        Player player = blockPlaceEvent.getPlayer();
        Region region = getRegionManager().getRegion(blockPlaceEvent.getBlock().getLocation());

        triggerAPIEvent(player, event, region);
        blockPlaceEvent.setCancelled(true);
    }
}
