package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerInteractSetting extends RegionSetting {

    public PlayerInteractSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "INTERACTION";
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (checkIfInRegion(event.getPlayer().getLocation())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
        Player player = interactEvent.getPlayer();
        Region region = getRegionManager().getRegion(player.getLocation());

        if (((PlayerInteractEvent) event).getClickedBlock() != null) {
            triggerAPIEvent(player, event, region);
            interactEvent.setCancelled(true);
        }
    }
}