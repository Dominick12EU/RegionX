package it.dominick.regionx.region;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/*
  **********************************************************
  *                                                        *
  *                  Fixed by @Alzyy                       *
  *                                                        *
  **********************************************************
 */
@Getter
public abstract class Cuboid {
    protected String name;
    protected Location min;
    protected Location max;

    protected int minX;
    protected int minY;
    protected int minZ;
    protected int maxX;
    protected int maxY;
    protected int maxZ;

    public Cuboid(String name, Location pos1, Location pos2) {
        this.min = new Location(pos1.getWorld(),
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()));
        this.max = new Location(pos1.getWorld(),
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ()));
        minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        this.name = name;
    }

    public Location getCenter() {
        return new Location(min.getWorld(),
                (min.getX() + max.getX()) / 2,
                (min.getY() + max.getY()) / 2,
                (min.getZ() + max.getZ()) / 2);
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(min.getWorld())) {
            return false;
        }

        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();

        return blockX >= minX && blockX <= maxX
                && blockY >= minY && blockY <= maxY
                && blockZ >= minZ && blockZ <= maxZ;
    }

    public abstract void onEnter(Player player);
    public abstract void onExit(Player player);
}
