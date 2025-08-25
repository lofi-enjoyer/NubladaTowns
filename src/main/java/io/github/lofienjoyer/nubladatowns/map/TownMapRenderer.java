package io.github.lofienjoyer.nubladatowns.map;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.BannerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashSet;

public class TownMapRenderer extends MapRenderer {

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        var scale = (int) Math.pow(2, map.getScale().getValue());
        var mapSide = 128 * scale;
        var chunkSide = mapSide / 16;
        var townManager = NubladaTowns.getInstance().getTownManager();
        var startX = (int) Math.floor((map.getCenterX() - mapSide/2) / 16f);
        var startZ = (int) Math.floor((map.getCenterZ() - mapSide/2) / 16f);
        var foundTowns = new HashSet<Town>();
        for (int x = 0; x < chunkSide; x++) {
            for (int z = 0; z < chunkSide; z++) {
                var town = townManager.getTownOnChunk(x + startX, z + startZ, map.getWorld());
                if (town == null) {
                    clearChunk(x * (16 / scale), z * (16 / scale), canvas, 16 / scale);
                    continue;
                }

                foundTowns.add(town);
                var townColor = new Color(town.getRgbColor(), false);
                drawChunk(x * (16 / scale), z * (16 / scale), canvas, townColor, 16 / scale);
            }
        }

        var cursors = new MapCursorCollection();
        foundTowns.forEach(town -> {
            var spawnChunk = town.getSpawn().getChunk();
            if (spawnChunk.getX() < startX || spawnChunk.getX() >= startX + chunkSide || spawnChunk.getZ() < startZ || spawnChunk.getZ() >= startZ + chunkSide)
                return;

            var townSpawn = town.getSpawn();
            var cursorX = ((townSpawn.getBlockX() - startX * 16) / scale);
            var cursorZ = ((townSpawn.getBlockZ() - startZ * 16) / scale);
            if (canvas.getBasePixelColor(cursorX, cursorZ).getAlpha() == 0)
                return;

            cursors.addCursor(new MapCursor((byte)(cursorX * 2 - 128), (byte)(cursorZ * 2 - 128), (byte)8, BannerUtils.getMapCursorBannerByColor(town.getRgbColor()), true, Component.text(town.getName(), TextColor.color(town.getRgbColor()))));
        });
        canvas.setCursors(cursors);
    }

    private void drawChunk(int startX, int startZ, MapCanvas canvas, Color townColor, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                var color = canvas.getBasePixelColor(x + startX, z + startZ);
                if (color.getAlpha() == 0)
                    continue;

                canvas.setPixelColor(x + startX, z + startZ, new Color((color.getRed() + townColor.getRed()) / 2, (color.getGreen() + townColor.getGreen()) / 2, (color.getBlue() + townColor.getBlue()) / 2));
            }
        }
    }

    private void clearChunk(int startX, int startZ, MapCanvas canvas, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                canvas.setPixelColor(x + startX, z + startZ, canvas.getBasePixelColor(x + startX, z + startZ));
            }
        }
    }

}
