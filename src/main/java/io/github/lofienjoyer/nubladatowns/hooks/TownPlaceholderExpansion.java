package io.github.lofienjoyer.nubladatowns.hooks;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TownPlaceholderExpansion extends PlaceholderExpansion {

    private final NubladaTowns plugin;

    private final TownManager townManager;

    public TownPlaceholderExpansion(NubladaTowns plugin,
                                    TownManager townManager) {
        this.plugin = plugin;
        this.townManager = townManager;

        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nubladatowns";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        final @NotNull UUID uuid = player.getUniqueId();

        return switch (identifier) {
            case "town" -> {
                if (townManager.hasTown(uuid))
                    yield townManager.getPlayerTown(uuid).getName();
                yield "";
            }
            case "town_power" -> {
                if (townManager.hasTown(uuid))
                    yield String.valueOf(townManager.getPlayerTown(uuid).getPower());
                yield "";
            }
            case "town_mayor" -> {
                if (townManager.hasTown(uuid))
                    yield Bukkit.getOfflinePlayer(townManager.getPlayerTown(uuid).getMayor()).getName();
                yield "";
            }
            case "town_residents_amount" -> {
                if (townManager.hasTown(uuid))
                    yield String.valueOf(townManager.getPlayerTown(uuid).getResidents().size());
                yield "";
            }
            case "town_claimed_land_amount" -> {
                if (townManager.hasTown(uuid))
                    yield String.valueOf(townManager.getClaimedLand().size());
                yield "";
            }
            case "town_spawn" -> {
                if (townManager.hasTown(uuid)) {
                    Location spawn = townManager.getPlayerTown(uuid).getSpawn();
                    yield String.format("%s %s %s",
                            spawn.x(),
                            spawn.y(),
                            spawn.z()
                    );
                }
                yield "";
            }
            case "town_spawn_x" -> {
                if (townManager.hasTown(uuid))
                    yield townManager.getPlayerTown(uuid).getSpawn().x() + "";
                yield "";
            }
            case "town_spawn_y" -> {
                if (townManager.hasTown(uuid))
                    yield townManager.getPlayerTown(uuid).getSpawn().y() + "";
                yield "";
            }
            case "town_spawn_z" -> {
                if (townManager.hasTown(uuid))
                    yield townManager.getPlayerTown(uuid).getSpawn().z() + "";
                yield "";
            }
            case "town_color_hex" -> {
                if (townManager.hasTown(uuid)) {
                    yield TextColor.color(townManager.getPlayerTown(uuid).getRgbColor()) + "";
                }
                yield "";
            }
            case "town_is_open" -> {
                if (townManager.hasTown(uuid))
                    yield String.valueOf(townManager.getPlayerTown(uuid).isOpen());
                yield "";
            }
            case "has_town" -> String.valueOf(townManager.hasTown(player.getUniqueId()));
            default -> null;
        };

    }
}
