package it.dominick.regionx.api.player;

import it.dominick.regionx.region.Region;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserRegion {
    private static final Map<Player, Region> userRegions = new HashMap<>();
    private static final Map<Player, Boolean> bypassStatus = new HashMap<>();

    public static void setPlayerRegion(Player player, Region region) {
        userRegions.put(player, region);
    }

    public static Region getPlayerRegion(Player player) {
        return userRegions.get(player);
    }

    public static void removePlayerRegion(Player player) {
        userRegions.remove(player);
    }

    public static boolean isPlayerInRegion(Player player, Region region) {
        return userRegions.get(player) == region;
    }

    public static void setBypass(Player player, boolean bypass) {
        bypassStatus.put(player, bypass);
    }

    public static boolean hasBypass(Player player) {
        return bypassStatus.getOrDefault(player, false);
    }

    public static Set<Player> getPlayersInRegion(Region region) {
        return userRegions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(region))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
