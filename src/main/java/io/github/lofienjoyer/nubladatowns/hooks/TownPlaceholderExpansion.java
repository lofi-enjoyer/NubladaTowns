package io.github.lofienjoyer.nubladatowns.hooks;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
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

        switch (identifier) {
            case "town":
                if (townManager.hasTown(uuid))
                    return townManager.getPlayerTown(uuid).getName();
                return "";
            case "town_power":
                if (townManager.hasTown(uuid))
                    return String.valueOf(townManager.getPlayerTown(uuid).getPower());
                return "";
            case "town_mayor":
                if (townManager.hasTown(uuid))
                    return Bukkit.getOfflinePlayer(townManager.getPlayerTown(uuid).getMayor()).getName();
                return "";
            case "town_residents_amount":
                if (townManager.hasTown(uuid))
                    return String.valueOf(townManager.getPlayerTown(uuid).getResidents().size());
                return "";
            case "town_claimed_land_amount":
                if (townManager.hasTown(uuid))
                    return String.valueOf(townManager.getClaimedLand().size());
                return "";
            case "town_spawn":
                if (townManager.hasTown(uuid))
                    return townManager.getPlayerTown(uuid).getSpawn().toString();
                return "";
            case "town_is_open":
                if (townManager.hasTown(uuid))
                    return String.valueOf(townManager.getPlayerTown(uuid).isOpen());
                return "";
            case "has_town":
                return String.valueOf(townManager.hasTown(player.getUniqueId()));
        }

        return  null;
    }
}
