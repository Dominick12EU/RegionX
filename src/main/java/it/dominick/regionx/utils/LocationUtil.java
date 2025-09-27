package it.dominick.regionx.utils;

import it.dominick.regionx.region.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class LocationUtil {

    public static Location findSafeLocationNearby(Location center) {
        World world = center.getWorld();
        int radius = 50;
        int maxY = world.getMaxHeight();

        for (int r = 0; r <= radius; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    Location checkLocation = center.clone().add(x, 0, z);
                    for (int y = maxY; y >= 0; y--) {
                        checkLocation.setY(y);
                        Material blockType = checkLocation.getBlock().getType();

                        if (!blockType.isSolid()) {
                            return checkLocation.add(0.5, 0, 0.5);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Location getSafeCenterLocation(Region region) {
        Location center = region.getCenter();
        Location safeLocation = findSafeLocationNearby(center);

        if (safeLocation == null) {
            World world = center.getWorld();
            for (int y = world.getMaxHeight(); y >= 0; y--) {
                center.setY(y);
                Material blockType = center.getBlock().getType();
                if (!blockType.isSolid()) {
                    return center.add(0.5, 0, 0.5);
                }
            }
        } else {
            return safeLocation;
        }

        return null;
    }
}
