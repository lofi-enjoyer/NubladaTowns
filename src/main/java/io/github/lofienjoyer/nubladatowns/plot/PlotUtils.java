package io.github.lofienjoyer.nubladatowns.plot;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Intersectionf;
import org.joml.Vector3fc;

import java.util.*;

public class PlotUtils {

    public static void showPlotMenu(Player player, Plot plot) {
        var lm = NubladaTowns.getInstance().getLocalizationManager();
        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        var content = lm.getMessage("plot-menu").replaceText(builder -> {
            builder.matchLiteral("%plot%").replacement(Component.text(plot.name(), Style.style(TextColor.color(plot.argbColor()))));
        }).replaceText(builder -> {
            var owner = plot.ownerUuid() != null ? Bukkit.getOfflinePlayer(plot.ownerUuid()).getName() : "";
            builder.matchLiteral("%owner%").replacement(owner);
        }).replaceText(builder -> {
            var members = String.join(", ", plot.members().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toList());
            builder.matchLiteral("%members%").replacement(members);
        });

        player.openBook(Book.book(title, author, content));
    }

    public static Plot createPlotBetween(Location a, Location b, UUID ownerUuid, String plotName) {
        var x0 = Math.min(a.getBlockX(), b.getBlockX());
        var x1 = Math.max(a.getBlockX(), b.getBlockX());
        var y0 = Math.min(a.getBlockY(), b.getBlockY());
        var y1 = Math.max(a.getBlockY(), b.getBlockY());
        var z0 = Math.min(a.getBlockZ(), b.getBlockZ());
        var z1 = Math.max(a.getBlockZ(), b.getBlockZ());

        var min = new Location(a.getWorld(), x0, y0, z0);
        var max = new Location(a.getWorld(), x1, y1, z1).add(1, 1, 1);

        var argbColor = Color.fromARGB(
                1,
                (int)(Math.abs(min.getBlockX() % 16) / 15f * 155) + 100,
                (int)(Math.abs(min.getBlockY() % 16) / 15f * 155) + 100,
                (int)(Math.abs(min.getBlockZ() % 16) / 15f * 155) + 100
        ).asARGB();
        return new Plot(UUID.randomUUID(), ownerUuid, min.toVector(), max.toVector(), plotName, argbColor, a.getWorld(),  new ArrayList<>());
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
        return new Plot(null, null, min.toVector(), max.toVector(), null, 0, a.getWorld(), null);
    }

    public static boolean isPlotInsideTown(Plot plot, Town town) {
        var chunks = getChunksInsidePlot(plot, plot.world());
        return chunks.stream().allMatch(chunk -> {
            var chunkTown = NubladaTowns.getInstance().getTownManager().getTownOnChunk(chunk);
            return chunkTown != null && chunkTown.getUniqueId().equals(town.getUniqueId());
        });
    }

    public static List<Chunk> getChunksInsidePlot(Plot plot, World world) {
        var min = plot.min().toLocation(world);
        var max = plot.max().toLocation(world).subtract(1, 1, 1);
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
                    (plot.min().getX() <= location.x() && plot.max().getX() > location.x()) &&
                    (plot.min().getY() <= location.y() && plot.max().getY() > location.y()) &&
                    (plot.min().getZ() <= location.z() && plot.max().getZ() > location.z())
            ) {
                return Optional.of(plot);
            }
        }
        return Optional.empty();
    }

    public static boolean doPlotsIntersect(Plot a, Plot b) {
        return checkAbb(a.min(), a.max(), b.min(), b.max());
    }

    private static boolean checkAbb(Vector minA, Vector maxA, Vector minB, Vector maxB) {
        return checkAab(
                minA.getBlockX(), minA.getBlockY(), minA.getBlockZ(),
                maxA.getBlockX(), maxA.getBlockY(), maxA.getBlockZ(),
                minB.getBlockX(), minB.getBlockY(), minB.getBlockZ(),
                maxB.getBlockX(), maxB.getBlockY(), maxB.getBlockZ()
        );
    }

    private static boolean checkAab(float minXA, float minYA, float minZA,
                                    float maxXA, float maxYA, float maxZA,
                                    float minXB, float minYB, float minZB,
                                    float maxXB, float maxYB, float maxZB) {
        return maxXA > minXB && maxYA > minYB && maxZA > minZB &&
                minXA < maxXB && minYA < maxYB && minZA < maxZB;
    }

}
