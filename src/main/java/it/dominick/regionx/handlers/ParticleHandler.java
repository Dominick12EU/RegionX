package it.dominick.regionx.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ParticleHandler {
    private final Map<Player, CompletableFuture<?>> playerParticleTasks = new HashMap<>();

    public void showAreaParticles(Player player, Location pos1, Location pos2) {
        stopParticles(player);

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        CompletableFuture<?> particleTask = CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            long maxDuration = TimeUnit.SECONDS.toMillis(60);

            try {
                while (!Thread.currentThread().isInterrupted() && System.currentTimeMillis() - startTime < maxDuration) {
                    if (!player.isOnline()) {
                        stopParticles(player);
                    }

                    List<Location> particleLocations = calculateBorders(pos1, minX, minY, minZ, maxX, maxY, maxZ);

                    Particle<?> particle = new Particle<>(ParticleTypes.FLAME, null);

                    for (Location loc : particleLocations) {
                        WrapperPlayServerParticle particlePacket = new WrapperPlayServerParticle(
                                particle,
                                true,
                                new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
                                new Vector3f(0.0F, 0.0F, 0.0F),
                                0.0F,
                                1
                        );

                        PacketEvents.getAPI().getPlayerManager().sendPacket(player, particlePacket);
                    }

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (CancellationException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        particleTask.whenComplete((result, throwable) -> {
            playerParticleTasks.remove(player);
            if (throwable != null && !(throwable instanceof CancellationException)) {
                throwable.printStackTrace();
            }
        });

        playerParticleTasks.put(player, particleTask);
    }

    public void stopParticles(Player player) {
        CompletableFuture<?> task = playerParticleTasks.get(player);
        if (task != null && !task.isDone()) {
            task.cancel(true);
        }

        playerParticleTasks.remove(player);
    }

    public void stopAllParticles() {
        for (Player player : new ArrayList<>(playerParticleTasks.keySet())) {
            stopParticles(player);
        }
    }

    private List<Location> calculateBorders(Location pos1, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<Location> particleLocations = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            particleLocations.add(new Location(pos1.getWorld(), x, minY, minZ));
            particleLocations.add(new Location(pos1.getWorld(), x, minY, maxZ));
            particleLocations.add(new Location(pos1.getWorld(), x, maxY, minZ));
            particleLocations.add(new Location(pos1.getWorld(), x, maxY, maxZ));
        }
        for (int z = minZ; z <= maxZ; z++) {
            particleLocations.add(new Location(pos1.getWorld(), minX, minY, z));
            particleLocations.add(new Location(pos1.getWorld(), maxX, minY, z));
            particleLocations.add(new Location(pos1.getWorld(), minX, maxY, z));
            particleLocations.add(new Location(pos1.getWorld(), maxX, maxY, z));
        }
        for (int y = minY; y <= maxY; y++) {
            particleLocations.add(new Location(pos1.getWorld(), minX, y, minZ));
            particleLocations.add(new Location(pos1.getWorld(), maxX, y, minZ));
            particleLocations.add(new Location(pos1.getWorld(), minX, y, maxZ));
            particleLocations.add(new Location(pos1.getWorld(), maxX, y, maxZ));
        }

        return particleLocations;
    }
}