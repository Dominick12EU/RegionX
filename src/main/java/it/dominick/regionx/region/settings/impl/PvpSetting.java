package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpSetting extends RegionSetting {

    public PvpSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "PVP";
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (checkIfInRegion(event.getEntity().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        EntityDamageByEntityEvent pvpEvent = (EntityDamageByEntityEvent) event;
        if (pvpEvent.getDamager() instanceof Player && pvpEvent.getEntity() instanceof Player) {
            Player player = (Player) pvpEvent.getEntity();
            Region region = getRegionManager().getRegion(player.getLocation());

            triggerAPIEvent(player, event, region);
            pvpEvent.setCancelled(true);
        }

    }
}
