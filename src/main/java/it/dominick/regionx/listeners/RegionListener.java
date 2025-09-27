package it.dominick.regionx.listeners;

import it.dominick.regionx.api.player.UserRegion;
import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public class RegionListener implements Listener {
    private final RegionManager regionManager;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        updatePlayerRegion(player, to);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to != null) {
            updatePlayerRegion(player, to);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        if (location != null) {
            updatePlayerRegion(player, location);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Region userRegion = UserRegion.getPlayerRegion(player);

        if (userRegion != null) userRegion.onExit(player);
    }

    private void updatePlayerRegion(Player player, Location to) {
        Region currentRegion = regionManager.getRegionByLocation(to);
        Region userRegion = UserRegion.getPlayerRegion(player);

        if (currentRegion != null && (userRegion == null || !userRegion.equals(currentRegion))) {
            if (userRegion != null) {
                userRegion.onExit(player);
            }
            currentRegion.onEnter(player);
            UserRegion.setPlayerRegion(player, currentRegion);
        } else if (currentRegion == null && userRegion != null) {
            userRegion.onExit(player);
            UserRegion.removePlayerRegion(player);
        }
    }
}
