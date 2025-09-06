package io.github.lofienjoyer.nubladatowns.plot;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.town.Town;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.joml.Intersectionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlotUtils {

    public static Plot createPlotBetween(Location a, Location b, UUID ownerUuid, String plotName) {
        var x0 = Math.min(a.getBlockX(), b.getBlockX());
        var x1 = Math.max(a.getBlockX(), b.getBlockX());
        var y0 = Math.min(a.getBlockY(), b.getBlockY());
        var y1 = Math.max(a.getBlockY(), b.getBlockY());
        var z0 = Math.min(a.getBlockZ(), b.getBlockZ());
        var z1 = Math.max(a.getBlockZ(), b.getBlockZ());

        var min = new Location(a.getWorld(), x0, y0, z0);
        var max = new Location(a.getWorld(), x1, y1, z1);
        return new Plot(ownerUuid, min.toVector(), max.toVector(), plotName.toLowerCase(), a.getWorld(),  new ArrayList<>());
    }

    public static Plot getPlotBetween(Location a, Location b) {
        var x0 = Math.min(a.getBlockX(), b.getBlockX());
        var x1 = Math.max(a.getBlockX(), b.getBlockX());
        var y0 = Math.min(a.getBlockY(), b.getBlockY());
        var y1 = Math.max(a.getBlockY(), b.getBlockY());
        var z0 = Math.min(a.getBlockZ(), b.getBlockZ());
        var z1 = Math.max(a.getBlockZ(), b.getBlockZ());

        var min = new Location(a.getWorld(), x0, y0, z0);
        var max = new Location(a.getWorld(), x1, y1, z1);
        return new Plot(null, min.toVector(), max.toVector(), null, a.getWorld(), null);
    }

    public static boolean isPlotInsideTown(Location posA, Location posB, Town town) {
        var plot = getPlotBetween(posA, posB);
        var chunks = getChunksInsidePlot(plot, posA.getWorld());
        return chunks.stream().allMatch(chunk -> {
            var chunkTown = NubladaTowns.getInstance().getTownManager().getTownOnChunk(chunk);
            return chunkTown != null && chunkTown.getUniqueId().equals(town.getUniqueId());
        });
    }

    public static List<Chunk> getChunksInsidePlot(Plot plot, World world) {
        var min = plot.min().toLocation(world);
        var max = plot.max().toLocation(world);
        var chunks = new ArrayList<Chunk>();
        for (int x = min.getChunk().getX(); x <= max.getChunk().getX(); x++) {
            for (int z = min.getChunk().getZ(); z <= max.getChunk().getZ(); z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    public static Optional<Plot> getPlotAtLocation(Location location) {
        for (var town : NubladaTowns.getInstance().getTownManager().getTowns()) {
            var plot = getPlotAtLocation(location, town);
            if (plot.isPresent())
                return plot;
        }
        return Optional.empty();
    }

    public static Optional<Plot> getPlotAtLocation(Location location, Town town) {
        for (var plot : town.getPlots()) {
            if (!plot.world().equals(location.getWorld()))
                continue;

            if (
                    (plot.min().getX() <= location.x() && plot.max().getX() >= location.x()) &&
                    (plot.min().getY() <= location.y() && plot.max().getY() >= location.y()) &&
                    (plot.min().getZ() <= location.z() && plot.max().getZ() >= location.z())
            ) {
                return Optional.of(plot);
            }
        }
        return Optional.empty();
    }

    public static boolean doPlotsIntersect(Plot a, Plot b) {
        return Intersectionf.testAabAab(
                a.min().getBlockX(), a.min().getBlockY(), a.min().getBlockZ(), a.max().getBlockX(), a.max().getBlockY(), a.max().getBlockZ(),
                b.min().getBlockX(), b.min().getBlockY(), b.min().getBlockZ(), b.max().getBlockX(), b.max().getBlockY(), b.max().getBlockZ()
        );
    }

}
