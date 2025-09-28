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
        if (checkIfInRegion(event.getEntity().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        EntityDamageEvent fallDamageEvent = (EntityDamageEvent) event;
        System.out.println("Fall damage event: " + fallDamageEvent.getCause());
        if (fallDamageEvent.getEntity() instanceof Player &&
                fallDamageEvent.getCause() == EntityDamageEvent.DamageCause.FALL) {
            System.out.println("DOMINICK GAY");
            Player player = (Player) fallDamageEvent.getEntity();
            Region region = getRegionManager().getRegion(player.getLocation());

            triggerAPIEvent(player, event, region);
            fallDamageEvent.setCancelled(true);
        }
    }
}
