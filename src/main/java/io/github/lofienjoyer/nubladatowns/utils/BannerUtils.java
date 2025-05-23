package io.github.lofienjoyer.nubladatowns.utils;

import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.map.MapCursor;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public class BannerUtils {

    private static final Map<Material, MapCursor.Type> BANNER_TYPES = new HashMap<>(
            Map.ofEntries(
                    entry(Material.WHITE_BANNER, MapCursor.Type.BANNER_WHITE),
                    entry(Material.BLACK_BANNER, MapCursor.Type.BANNER_BLACK),
                    entry(Material.BLUE_BANNER, MapCursor.Type.BANNER_BLUE),
                    entry(Material.BROWN_BANNER, MapCursor.Type.BANNER_BROWN),
                    entry(Material.CYAN_BANNER, MapCursor.Type.BANNER_CYAN),
                    entry(Material.GRAY_BANNER, MapCursor.Type.BANNER_GRAY),
                    entry(Material.GREEN_BANNER, MapCursor.Type.BANNER_GREEN),
                    entry(Material.LIME_BANNER, MapCursor.Type.BANNER_LIME),
                    entry(Material.PINK_BANNER, MapCursor.Type.BANNER_PINK),
                    entry(Material.MAGENTA_BANNER, MapCursor.Type.BANNER_MAGENTA),
                    entry(Material.ORANGE_BANNER, MapCursor.Type.BANNER_ORANGE),
                    entry(Material.PURPLE_BANNER, MapCursor.Type.BANNER_PURPLE),
                    entry(Material.YELLOW_BANNER, MapCursor.Type.BANNER_YELLOW),
                    entry(Material.RED_BANNER, MapCursor.Type.BANNER_RED),
                    entry(Material.LIGHT_BLUE_BANNER, MapCursor.Type.BANNER_LIGHT_BLUE),
                    entry(Material.LIGHT_GRAY_BANNER, MapCursor.Type.BANNER_LIGHT_GRAY)
            )
    );

    public static MapCursor.Type getMapCursorBannerByColor(int argbColor) {
        for (var entry : BANNER_TYPES.entrySet()) {
            var state = (Banner) entry.getKey().createBlockData().createBlockState();
            if (state.getBaseColor().getColor().asARGB() == argbColor)
                return entry.getValue();
        }
        return MapCursor.Type.BANNER_WHITE;
    }

    public static Material getBannerMaterialByColor(int argbColor) {
        for (var material : BANNER_TYPES.keySet()) {
            var bannerColor = ((Banner) material.createBlockData().createBlockState()).getBaseColor().getColor().asARGB();
            if (argbColor == bannerColor)
                return material;
        }

        return Material.WHITE_BANNER;
    }

}
