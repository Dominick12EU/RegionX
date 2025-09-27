package it.dominick.regionx.region;

import it.dominick.regionx.api.events.RegionEnterEvent;
import it.dominick.regionx.api.events.RegionQuitEvent;
import it.dominick.regionx.api.player.UserRegion;
import it.dominick.regionx.region.settings.RegionSetting;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class Region extends Cuboid {
    private final Map<String, RegionSetting> settings = new HashMap<>();
    private final Set<Player> playersInside = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Region(String name, Location pos1, Location pos2) {
        super(name, pos1, pos2);
    }

    @Override
    public void onEnter(Player player) {
        if (!UserRegion.hasBypass(player)) {
            UserRegion.setPlayerRegion(player, this);
            Bukkit.getPluginManager().callEvent(new RegionEnterEvent(player, this));
        }
    }

    @Override
    public void onExit(Player player) {
        UserRegion.removePlayerRegion(player);
        Bukkit.getPluginManager().callEvent(new RegionQuitEvent(player, this));
    }
}
