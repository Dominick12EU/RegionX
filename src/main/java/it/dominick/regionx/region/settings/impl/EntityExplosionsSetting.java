package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityExplosionsSetting extends RegionSetting {

    public EntityExplosionsSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "ENTITY_EXPLOSIONS";
    }

    @EventHandler
    public void onExplodeEntityEvent(EntityExplodeEvent event) {
        if (checkIfInRegion(event.getEntity().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        EntityExplodeEvent explodeEvent = (EntityExplodeEvent) event;
        Region region = getRegionManager().getRegion(((EntityExplodeEvent) event).getEntity().getLocation());

        triggerAPIEvent(null, event, region);
        explodeEvent.setCancelled(true);
    }
}
