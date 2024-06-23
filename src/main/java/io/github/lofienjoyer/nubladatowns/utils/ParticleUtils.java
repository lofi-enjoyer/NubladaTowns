package io.github.lofienjoyer.nubladatowns.utils;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ParticleUtils {

    public static void showChunkBorders(Chunk chunk, Particle particle, double baseHeight, int amount) {
        var world = chunk.getWorld();
        var chunkX = chunk.getX() * 16;
        var chunkZ = chunk.getZ() * 16;
        var counter = new AtomicInteger();
        var taskReference = new AtomicReference<BukkitTask>();
        taskReference.set(Bukkit.getScheduler().runTaskTimer(NubladaTowns.getPlugin(NubladaTowns.class), () -> {
            var height = baseHeight + counter.get() / 4f - 1;

            for (int i = 0; i < 16; i++) {
                world.spawnParticle(particle, chunkX + i, height, chunkZ, 1);
            }

            for (int i = 0; i < 16; i++) {
                world.spawnParticle(particle, chunkX + i, height, chunkZ + 16, 1);
            }

            for (int i = 0; i < 16; i++) {
                world.spawnParticle(particle, chunkX, height, chunkZ + i, 1);
            }

            for (int i = 0; i < 16; i++) {
                world.spawnParticle(particle, chunkX + 16, height, chunkZ + i, 1);
            }

            if (counter.incrementAndGet() > amount) {
                taskReference.get().cancel();
            }
        }, 0, 1));
    }

    public static void showTownBorders(Player player) {
        var tm = NubladaTowns.getInstance().getTownManager();
        var chunk = player.getChunk();
        var baseHeight = player.getLocation().getY();
        var world = chunk.getWorld();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                var chunkX = chunk.getX() - 1 + i;
                var chunkZ = chunk.getZ() - 1 + j;
                var chunkBlockX = chunkX * 16;
                var chunkBlockZ = chunkZ * 16;
                var currentTown = tm.getTownOnChunk(chunkX, chunkZ, world);
                if (currentTown == null)
                    continue;

                var showNorth = tm.getTownOnChunk(chunkX, chunkZ - 1, world) != currentTown;
                var showSouth = tm.getTownOnChunk(chunkX, chunkZ + 1, world) != currentTown;
                var showWest = tm.getTownOnChunk(chunkX - 1, chunkZ, world) != currentTown;
                var showEast = tm.getTownOnChunk(chunkX + 1, chunkZ, world) != currentTown;
                var dustOptions = new Particle.DustOptions(Color.fromARGB(currentTown.getRgbColor()), 1);
                for (int counter = 0; counter <= 6; counter++) {
                    var height = baseHeight + counter / 2f;

                    if (showNorth)
                        for (int b = 0; b <= 32; b++) {
//                            player.spawnParticle(particle, chunkBlockX + b, height, chunkBlockZ, 1);
                            player.spawnParticle(Particle.DUST, chunkBlockX + b / 2f, height, chunkBlockZ, 0, dustOptions);
                        }

                    if (showSouth)
                        for (int b = 0; b <= 32; b++) {
//                            player.spawnParticle(particle, chunkBlockX + b, height, chunkBlockZ + 16, 1);
                            player.spawnParticle(Particle.DUST, chunkBlockX + b / 2f, height, chunkBlockZ + 16, 0, dustOptions);
                        }

                    if (showWest)
                        for (int b = 0; b <= 32; b++) {
//                            player.spawnParticle(particle, chunkBlockX, height, chunkBlockZ + b, 1);
                            player.spawnParticle(Particle.DUST, chunkBlockX, height, chunkBlockZ + b / 2f, 0, dustOptions);
                        }

                    if (showEast)
                        for (int b = 0; b <= 32; b++) {
//                            player.spawnParticle(particle, chunkBlockX + 16, height, chunkBlockZ + b, 1);
                            player.spawnParticle(Particle.DUST, chunkBlockX + 16, height, chunkBlockZ + b / 2f, 0, dustOptions);
                        }
                }
            }
        }
    }

    public static void showPlot(Location posA, Location posB, Particle particle) {
        var world = posA.getWorld();
        var counter = new AtomicInteger();
        var taskReference = new AtomicReference<BukkitTask>();
        taskReference.set(Bukkit.getScheduler().runTaskTimer(NubladaTowns.getPlugin(NubladaTowns.class), () -> {
            for (int i = 0; i < Math.abs(posB.x() - posA.x()); i++) {
                world.spawnParticle(particle, posA.x() + i, posA.y(), posA.z(), 1);
            }

            for (int i = 0; i < Math.abs(posB.x() - posA.x()); i++) {
                world.spawnParticle(particle, posA.x() + i, posA.y(), posB.z() + 1, 1);
            }

            for (int i = 0; i < Math.abs(posB.x() - posA.x()); i++) {
                world.spawnParticle(particle, posA.x() + i, posB.y() + 1, posA.z(), 1);
            }

            for (int i = 0; i < Math.abs(posB.x() - posA.x()); i++) {
                world.spawnParticle(particle, posA.x() + i, posB.y() + 1, posB.z() + 1, 1);
            }

            for (int i = 0; i < Math.abs(posB.z() - posA.z()); i++) {
                world.spawnParticle(particle, posA.x(), posA.y(), posA.z() + i, 1);
            }

            for (int i = 0; i < Math.abs(posB.z() - posA.z()); i++) {
                world.spawnParticle(particle, posB.x() + 1, posA.y(), posA.z() + i, 1);
            }

            for (int i = 0; i < Math.abs(posB.z() - posA.z()); i++) {
                world.spawnParticle(particle, posA.x(), posB.y() + 1, posA.z() + i, 1);
            }

            for (int i = 0; i < Math.abs(posB.z() - posA.z()); i++) {
                world.spawnParticle(particle, posB.x() + 1, posB.y() + 1, posA.z() + i, 1);
            }

            for (int i = 0; i < Math.abs(posB.y() - posA.y()); i++) {
                world.spawnParticle(particle, posA.x(), posA.y() + i, posA.z(), 1);
            }

            for (int i = 0; i < Math.abs(posB.y() - posA.y()); i++) {
                world.spawnParticle(particle, posB.x() + 1, posA.y() + i, posA.z(), 1);
            }

            for (int i = 0; i < Math.abs(posB.y() - posA.y()); i++) {
                world.spawnParticle(particle, posA.x(), posA.y() + i, posB.z() + 1, 1);
            }

            for (int i = 0; i < Math.abs(posB.y() - posA.y()); i++) {
                world.spawnParticle(particle, posB.x() + 1, posA.y() + i, posB.z() + 1, 1);
            }

            if (counter.incrementAndGet() > 5) {
                taskReference.get().cancel();
            }
        }, 0, 1));
    }

}
