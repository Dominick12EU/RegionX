package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FallDamageSetting extends RegionSetting {

    public FallDamageSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "FALL_DAMAGE";
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (shouldProcess(player, player.getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        EntityDamageEvent fallDamageEvent = (EntityDamageEvent) event;
        if (fallDamageEvent.getEntity() instanceof Player player &&
                fallDamageEvent.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Region region = getRegionManager().getRegion(player.getLocation());

            triggerAPIEvent(player, event, region);
            fallDamageEvent.setCancelled(true);
        }
    }
}
