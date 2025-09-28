package it.dominick.regionx.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import it.dominick.regionx.RegionX;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ParticleHandler {
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    private static final int STEP = 1;
    private static final int DASH_SPACING = 3;
    private static final int ANIM_TICKS = 4;
    private static final double CORNER_BOOST = 0.25;
    private static final double MAX_DISTANCE = 64.0;
    private static final Particle<?> P_FLAME = new Particle<>(ParticleTypes.FLAME);
    private static final Particle<?> P_ENDROD = new Particle<>(ParticleTypes.END_ROD);

    public void showAreaParticles(Player player, Location pos1, Location pos2) {
        stopParticles(player);
        if (pos1 == null || pos2 == null || pos1.getWorld() != pos2.getWorld()) return;

        final UUID uuid = player.getUniqueId();
        final World world = pos1.getWorld();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;

        int minY = Math.max(Math.min(pos1.getBlockY(), pos2.getBlockY()), worldMinY);
        int maxY = Math.min(Math.max(pos1.getBlockY(), pos2.getBlockY()), worldMaxY);

        final List<Location> edges = calculateWireframe(world, minX, minY, minZ, maxX, maxY, maxZ, STEP);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(RegionX.getInstance(), new Runnable() {
            int phase = 0;
            int humTick = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) {
                    stopParticles(uuid);
                    return;
                }
                try {
                    final Location eye = p.getEyeLocation();
                    sendCornerBeacons(p, minX, minY, minZ, maxX, maxY, maxZ, eye);
                    for (Location loc : edges) {
                        if (loc.getWorld() != eye.getWorld()) continue;
                        if (loc.distanceSquared(eye) > (MAX_DISTANCE * MAX_DISTANCE)) continue;
                        int key = (loc.getBlockX() + loc.getBlockY() + loc.getBlockZ() + phase) % DASH_SPACING;
                        if (key != 0) continue;
                        boolean isVerticalEdge = isVerticalEdge(loc, minX, minZ, maxX, maxZ);
                        Particle<?> type = isVerticalEdge ? P_ENDROD : P_FLAME;
                        double jitterY = isVerticalEdge ? 0.0 : 0.02;
                        sendParticle(p, type,
                                loc.getX() + 0.5,
                                loc.getY() + 0.5 + jitterY,
                                loc.getZ() + 0.5,
                                0, 0, 0,
                                isVerticalEdge ? 0.0f : 0.01f,
                                1);
                    }
                    if (++humTick % 16 == 0) {
                        Location c = new Location(world, (minX+maxX)/2.0+0.5, (minY+maxY)/2.0+0.5, (minZ+maxZ)/2.0+0.5);
                        playSoundTo(player, c, Sound.BLOCK_BEACON_AMBIENT, 0.35f, 1.65f);
                    }
                    phase = (phase + 1) % DASH_SPACING;
                } catch (Exception ex) {
                    RegionX.getInstance().getLogger().severe("Errore particelle: " + ex.getMessage());
                    stopParticles(player);
                }
            }
        }, 0L, ANIM_TICKS);

        tasks.put(uuid, task);
    }

    public void playBlackHoleCreate(Player player, Location pos1, Location pos2) {
        if (player == null || pos1 == null || pos2 == null) return;
        if (pos1.getWorld() != pos2.getWorld()) return;
        stopParticles(player.getUniqueId());

        final World world = pos1.getWorld();
        final UUID uuid = player.getUniqueId();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.max(Math.min(pos1.getBlockY(), pos2.getBlockY()), world.getMinHeight());
        int maxY = Math.min(Math.max(pos1.getBlockY(), pos2.getBlockY()), world.getMaxHeight() - 1);
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        final double cx = (minX + maxX) / 2.0 + 0.5;
        final double cy = (minY + maxY) / 2.0 + 0.5;
        final double cz = (minZ + maxZ) / 2.0 + 0.5;

        final int PARTICLE_COUNT = Math.max(60, (int)((maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1) / 96.0));
        final int TICK_PERIOD = 1;
        final double MAXD = 64.0;
        final int IN_TICKS = 18;
        final int OUT_TICKS = 22;
        final double IN_SPEED = 0.35;
        final double OUT_SPEED = 1.25;
        final double DRAG = 0.92;
        final double UP_BIAS = 0.02;

        final Particle<?> P_PORTAL = new Particle<>(ParticleTypes.PORTAL);
        final Particle<?> P_ENDROD = new Particle<>(ParticleTypes.END_ROD);
        final Particle<?> P_CLOUD  = new Particle<>(ParticleTypes.CLOUD);
        final Particle<?> P_CRIT   = new Particle<>(ParticleTypes.CRIT);

        class SimP {
            double x, y, z, vx, vy, vz;
            int life;
            boolean outPhase;
            SimP(double x, double y, double z) {
                this.x = x; this.y = y; this.z = z;
                double dx = cx - x, dy = cy - y, dz = cz - z;
                double n = Math.sqrt(dx*dx + dy*dy + dz*dz) + 1e-6;
                double s = IN_SPEED * (0.85 + Math.random()*0.30);
                this.vx = dx / n * s;
                this.vy = dy / n * s;
                this.vz = dz / n * s;
                this.life = IN_TICKS + OUT_TICKS;
                this.outPhase = false;
            }
            void tick() {
                if (!outPhase) {
                    vx *= DRAG; vy *= DRAG; vz *= DRAG;
                    x += vx; y += vy; z += vz;
                    double dist2 = (x-cx)*(x-cx)+(y-cy)*(y-cy)+(z-cz)*(z-cz);
                    if (dist2 < 0.25 || life <= OUT_TICKS) {
                        outPhase = true;
                        double rx = (Math.random()*2-1), ry = (Math.random()*2-1), rz = (Math.random()*2-1);
                        double rn = Math.sqrt(rx*rx + ry*ry + rz*rz) + 1e-6;
                        double s = OUT_SPEED * (0.85 + Math.random()*0.40);
                        vx = (rx/rn) * s;
                        vy = (ry/rn) * s + UP_BIAS;
                        vz = (rz/rn) * s;
                    }
                } else {
                    vx *= 0.96; vy *= 0.96; vz *= 0.96;
                    x += vx; y += vy; z += vz;
                }
                life--;
            }
        }

        final List<SimP> sim = new ArrayList<>(PARTICLE_COUNT);
        final Random rnd = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double x = rnd.nextInt(maxX - minX + 1) + minX + rnd.nextDouble();
            double y = rnd.nextInt(maxY - minY + 1) + minY + rnd.nextDouble();
            double z = rnd.nextInt(maxZ - minZ + 1) + minZ + rnd.nextDouble();
            sim.add(new SimP(x, y, z));
        }

        final int[] tickCounter = {0};
        final boolean[] switched = {false};

        playSoundTo(player, (new Location(world, cx, cy, cz)), Sound.BLOCK_PORTAL_AMBIENT, 0.65f, 0.75f);
        playSoundTo(player, (new Location(world, cx, cy, cz)), Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.4f);

        Bukkit.getScheduler().runTaskTimer(RegionX.getInstance(), task -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                task.cancel();
                return;
            }
            final Location eye = p.getEyeLocation();
            final double max2 = MAXD * MAXD;
            try {
                tickCounter[0]++;
                double ang = (tickCounter[0] * 0.35);
                double r = 0.6 + 0.15 * Math.sin(tickCounter[0]*0.25);
                double sx = cx + Math.cos(ang) * r;
                double sz = cz + Math.sin(ang) * r;
                double sy = cy + 0.05 * Math.sin(ang*2);
                if (eye.getWorld() == world && eye.distanceSquared(new Location(world, sx, sy, sz)) <= max2) {
                    sendParticle(p, P_PORTAL, sx, sy, sz, 0,0,0, 0f, 1);
                    sendParticle(p, P_ENDROD,  cx, cy, cz,  0,0,0, 0f, 1);
                }

                if (!switched[0] && tickCounter[0] >= IN_TICKS) {
                    switched[0] = true;
                    playSoundTo(player, (new Location(world, cx, cy, cz)), Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 0.6f);
                    playSoundTo(player, (new Location(world, cx, cy, cz)), Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.9f);
                }

                Iterator<SimP> it = sim.iterator();
                int alive = 0;
                while (it.hasNext()) {
                    SimP sp = it.next();
                    sp.tick();
                    if (sp.life <= 0) { it.remove(); continue; }
                    alive++;
                    if (eye.getWorld() != world) continue;
                    double dx = eye.getX()-sp.x, dy = eye.getY()-sp.y, dz = eye.getZ()-sp.z;
                    if (dx*dx+dy*dy+dz*dz > max2) continue;
                    if (!sp.outPhase) {
                        sendParticle(p, P_PORTAL, sp.x, sp.y, sp.z, 0,0,0, 0.0f, 1);
                        if ((sp.life & 1) == 0) sendParticle(p, P_CRIT, sp.x, sp.y, sp.z, 0,0,0, 0.0f, 1);
                    } else {
                        sendParticle(p, P_CLOUD, sp.x, sp.y, sp.z, 0,0,0, 0.0f, 1);
                        if ((sp.life % 3) == 0) sendParticle(p, P_ENDROD, sp.x, sp.y, sp.z, 0,0,0, 0.0f, 1);
                        if ((sp.life % 4) == 0) playSoundTo(player, (new Location(world, sp.x, sp.y, sp.z)), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.2f, 1.9f);
                    }
                }

                if (alive == 0) {
                    playSoundTo(player, (new Location(world, cx, cy, cz)), Sound.ENTITY_GENERIC_EXPLODE, 0.55f, 1.25f);
                    playSoundTo(player, (new Location(world, cx, cy, cz)), Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 1.2f);
                    task.cancel();
                }
            } catch (Exception ex) {
                RegionX.getInstance().getLogger().severe("Errore particelle (create): " + ex.getMessage());
                task.cancel();
            }
        }, 0L, TICK_PERIOD);
    }

    public void stopParticles(Player player) {
        if (player != null) stopParticles(player.getUniqueId());
    }

    private void stopParticles(UUID uuid) {
        BukkitTask task = tasks.remove(uuid);
        if (task != null) task.cancel();
    }

    public void stopAllParticles() {
        for (BukkitTask t : tasks.values()) {
            if (t != null) t.cancel();
        }
        tasks.clear();
    }

    private void sendCornerBeacons(Player p,
                                   int minX, int minY, int minZ,
                                   int maxX, int maxY, int maxZ,
                                   Location eye) {
        int[] xs = new int[]{minX, maxX};
        int[] ys = new int[]{minY, maxY};
        int[] zs = new int[]{minZ, maxZ};
        for (int x : xs) for (int y : ys) for (int z : zs) {
            double cx = x + 0.5 + Math.copySign(CORNER_BOOST, x == minX ? 1 : -1);
            double cy = y + 0.5 + CORNER_BOOST;
            double cz = z + 0.5 + Math.copySign(CORNER_BOOST, z == minZ ? 1 : -1);
            if (eye.getWorld() != null && eye.getWorld() == p.getWorld()) {
                if (eye.distanceSquared(new Location(eye.getWorld(), cx, cy, cz)) > (MAX_DISTANCE * MAX_DISTANCE)) {
                    continue;
                }
            }
            sendParticle(p, P_ENDROD, cx, cy, cz, 0, 0, 0, 0.0f, 1);
            sendParticle(p, P_FLAME,  cx, cy, cz, 0, 0, 0, 0.02f, 1);
        }
    }

    private boolean isVerticalEdge(Location loc, int minX, int minZ, int maxX, int maxZ) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        boolean onMinX = x == minX;
        boolean onMaxX = x == maxX;
        boolean onMinZ = z == minZ;
        boolean onMaxZ = z == maxZ;
        return (onMinX || onMaxX) && (onMinZ || onMaxZ);
    }

    private void sendParticle(Player p, Particle<?> particle,
                              double x, double y, double z,
                              float offX, float offY, float offZ,
                              float speed, int count) {
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                particle,
                true,
                new Vector3d(x, y, z),
                new Vector3f(offX, offY, offZ),
                speed,
                count
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
    }

    private List<Location> calculateWireframe(World world,
                                              int minX, int minY, int minZ,
                                              int maxX, int maxY, int maxZ,
                                              int step) {
        Set<Long> seen = new HashSet<>();
        List<Location> out = new ArrayList<>();
        final java.util.function.BiConsumer<Integer[], Integer> add = (coords, dummy) -> {
            int x = coords[0], y = coords[1], z = coords[2];
            long key = (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | (y & 0xFFF);
            if (seen.add(key)) out.add(new Location(world, x, y, z));
        };
        for (int x = minX; x <= maxX; x += step) {
            add.accept(new Integer[]{x, minY, minZ}, 0);
            add.accept(new Integer[]{x, minY, maxZ}, 0);
            add.accept(new Integer[]{x, maxY, minZ}, 0);
            add.accept(new Integer[]{x, maxY, maxZ}, 0);
        }
        for (int z = minZ; z <= maxZ; z += step) {
            add.accept(new Integer[]{minX, minY, z}, 0);
            add.accept(new Integer[]{maxX, minY, z}, 0);
            add.accept(new Integer[]{minX, maxY, z}, 0);
            add.accept(new Integer[]{maxX, maxY, z}, 0);
        }
        for (int y = minY; y <= maxY; y += step) {
            add.accept(new Integer[]{minX, y, minZ}, 0);
            add.accept(new Integer[]{maxX, y, minZ}, 0);
            add.accept(new Integer[]{minX, y, maxZ}, 0);
            add.accept(new Integer[]{maxX, y, maxZ}, 0);
        }
        return out;
    }

    private void playSoundTo(Player p, Location loc, Sound sound, float volume, float pitch) {
        p.playSound(loc, sound, volume, pitch);
    }
}