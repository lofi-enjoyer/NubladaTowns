package io.github.lofienjoyer.nubladatowns.plot;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.town.Town;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class PlotUtils {

    public static Plot getPlotBetween(Location a, Location b, Town town) {
        var x0 = Math.min(a.getBlockX(), b.getBlockX());
        var x1 = Math.max(a.getBlockX(), b.getBlockX());
        var y0 = Math.min(a.getBlockY(), b.getBlockY());
        var y1 = Math.max(a.getBlockY(), b.getBlockY());
        var z0 = Math.min(a.getBlockZ(), b.getBlockZ());
        var z1 = Math.max(a.getBlockZ(), b.getBlockZ());

        var min = new Location(a.getWorld(), x0, y0, z0);
        var max = new Location(a.getWorld(), x1, y1, z1);
        return new Plot(town.getUniqueId(), min.toVector(), max.toVector());
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
        return new Plot(null, min.toVector(), max.toVector());
    }

    public static boolean isPlotInsideTown(Location posA, Location posB, Town town) {
        var plot = getPlotBetween(posA, posB);
        var chunks = getChunksInsidePlot(plot, posA.getWorld());
        return chunks.stream().allMatch(chunk -> {
            var chunkTown = NubladaTowns.getInstance().getTownManager().getTownOnChunk(chunk);
            return chunkTown == null || !chunkTown.getUniqueId().equals(town.getUniqueId());
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

}
