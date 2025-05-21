package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.data.DataManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TownManager {

    private final NubladaTowns instance;
    private final Map<UUID, Town> townMap;
    private final Map<LandChunk, UUID> landMap;
    private final Map<UUID, UUID> residentsMap;

    public TownManager(NubladaTowns instance) {
        this.instance = instance;

        this.townMap = new HashMap<>();
        this.landMap = new HashMap<>();
        this.residentsMap = new HashMap<>();
    }

    public void createTown(String name, Location location, Player founder, int color, List<Pattern> patterns) {
        var town = new Town(name);
        addResidentToTown(founder.getUniqueId(), town);
        var landChunk = new LandChunk(location.getChunk().getX(), location.getChunk().getZ(), location.getWorld());
        town.addLand(landChunk);
        town.setSpawn(location);
        town.setRgbColor(color);
        town.setBannerPatterns(patterns);
        town.setPower(0);
        town.setMayor(founder);

        townMap.put(town.getUniqueId(), town);
        landMap.put(landChunk, town.getUniqueId());
    }

    public void removeTown(Town town) {
        town.getResidents().forEach(uuid -> {
            residentsMap.remove(uuid);
        });

        town.getClaimedLand().forEach(chunk -> {
            landMap.remove(chunk);
        });

        townMap.remove(town.getUniqueId());
    }

    public Town claimChunk(Chunk chunk, Town town) {
        var currentTown = getTownOnChunk(chunk);
        if (currentTown != null) {
            instance.getLogger().warning("Tried to claim an already claimed chunk.");
            return currentTown;
        }

        var landChunk = new LandChunk(chunk.getX(), chunk.getZ(), chunk.getWorld());
        town.addLand(landChunk);
        landMap.put(landChunk, town.getUniqueId());
        return null;
    }

    public Town abandonChunk(Chunk chunk) {
        var currentTown = getTownOnChunk(chunk);
        if (currentTown == null) {
            instance.getLogger().warning("Tried to abandon a non-claimed chunk.");
            return null;
        }

        var landChunk = new LandChunk(chunk.getX(), chunk.getZ(), chunk.getWorld());
        currentTown.removeLand(chunk.getX(), chunk.getZ(), chunk.getWorld());
        landMap.remove(landChunk);
        return currentTown;
    }

    public void loadData(DataManager dataManager) {
        townMap.clear();
        residentsMap.clear();
        landMap.clear();

        var towns = dataManager.loadTowns();
        towns.forEach(town -> {
            townMap.put(town.getUniqueId(), town);
            town.getResidents().forEach(uuid -> {
                residentsMap.put(uuid, town.getUniqueId());
            });
            town.getClaimedLand().forEach(chunk -> {
                landMap.put(chunk, town.getUniqueId());
            });
        });
    }

    public void saveData(DataManager dataManager) throws IOException {
        dataManager.save(townMap.values());
    }

    public void addResidentToTown(UUID playerUuid, Town town) {
        town.addResident(playerUuid);
        residentsMap.put(playerUuid, town.getUniqueId());
    }

    public void removeResidentFromTown(UUID playerUuid, Town town) {
        town.removeResident(playerUuid);
        residentsMap.remove(playerUuid);
    }

    public Town getTownByUUID(UUID uuid) {
        return townMap.get(uuid);
    }

    public Town getTownByName(String name) {
        return townMap.values().stream()
                .filter(town -> town.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Town getTownOnChunk(Chunk chunk) {
        return getTownOnChunk(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }

    public Town getTownOnChunk(int x, int z, World world) {
        var townUuid = landMap.get(new LandChunk(x, z, world));
        return townMap.get(townUuid);
    }

    public Town getPlayerTown(Player player) {
        return getPlayerTown(player.getUniqueId());
    }

    public Town getPlayerTown(UUID uuid) {
        var townUuid = residentsMap.get(uuid);
        return townMap.get(townUuid);
    }

    public boolean hasTown(Player player) {
        return hasTown(player.getUniqueId());
    }

    public boolean hasTown(UUID uuid) {
        return getPlayerTown(uuid) != null;
    }

    public Collection<Town> getTowns() {
        return townMap.values();
    }

    public Collection<UUID> getResidents() {
        return residentsMap.values();
    }

    public Collection<LandChunk> getClaimedLand() {
        return landMap.keySet();
    }

}
