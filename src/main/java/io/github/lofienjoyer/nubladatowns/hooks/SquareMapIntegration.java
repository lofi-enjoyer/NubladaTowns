package io.github.lofienjoyer.nubladatowns.hooks;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.awt.*;

public class SquareMapIntegration {

    private final Squaremap api;

    public SquareMapIntegration(NubladaTowns instance, TownManager townManager) {
        this.api = SquaremapProvider.get();

        Bukkit.getWorlds().forEach(world -> {

            api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
                var key = Key.of("nubladatowns_layer_" + world.getName());
                var layerProvider = SimpleLayerProvider.builder("NubladaTowns")
                        .showControls(true)
                        .defaultHidden(false)
                        .layerPriority(1)
                        .zIndex(500)
                        .build();

                mapWorld.layerRegistry().register(key, layerProvider);
                Bukkit.getScheduler().runTaskTimer(instance, () -> updateMap(layerProvider, townManager, world), 0, 60 * 20);
            });

        });
    }

    private void updateMap(SimpleLayerProvider provider, TownManager townManager, World world) {
        provider.clearMarkers();

        townManager.getTowns().forEach(town -> {
            town.getClaimedLand().forEach(chunk -> {
                if (world != chunk.world())
                    return;

                var key = Key.of(String.format("%s_%d_%d", chunk.world().getName(), chunk.x(), chunk.z()));

                var p1 = Point.of(chunk.x() * 16, chunk.z() * 16);
                var p2 = Point.of(chunk.x() * 16 + 16, chunk.z() * 16 + 16);

                var marker = Marker.rectangle(p1, p2);
                marker.markerOptions(
                        MarkerOptions.builder()
                                .fillColor(new Color(town.getRgbColor(), false))
                                .fillOpacity(0.4)
                                .strokeOpacity(0.0)
                                .hoverTooltip(town.getName())
                                .build()
                );

                provider.addMarker(key, marker);
            });
        });
    }

}
