package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class TownManager {

    private final NubladaTowns instance;
    private Map<UUID, Town> townMap;
    private Map<LandChunk, UUID> landMap;
    private Map<UUID, UUID> residentsMap;

    public TownManager(NubladaTowns instance) {
        this.instance = instance;

        this.townMap = new HashMap<>();
        this.landMap = new HashMap<>();
        this.residentsMap = new HashMap<>();
    }

    public void createTown(String name, Location location, Player founder, int color) {
        var town = new Town(name);
        addResidentToTown(founder.getUniqueId(), town);
        var landChunk = new LandChunk(location.getChunk().getX(), location.getChunk().getZ(), location.getWorld());
        town.addLand(landChunk);
        town.setSpawn(location);
        town.setRgbColor(color);
        town.setMayor(founder);

        townMap.put(town.getUniqueId(), town);
        landMap.put(landChunk, town.getUniqueId());
    }

    public Town claimChunk(Chunk chunk, Town town) {
        var currentTown = getTownOnChunk(chunk);
        if (currentTown != null) {
            //TODO
            return currentTown;
        }

        var landChunk = new LandChunk(chunk.getX(), chunk.getZ(), chunk.getWorld());
        town.addLand(landChunk);
        landMap.put(landChunk, town.getUniqueId());
        return null;
    }

    public void loadData(YamlConfiguration dataConfig) {
        this.townMap = new HashMap<>();
        this.landMap = new HashMap<>();
        this.residentsMap = new HashMap<>();

        // Reminder: assign defaults to the ConfigurationSection::getSomething methods when adding new data to the Town class
        var townsSection = dataConfig.getConfigurationSection("towns");
        townsSection.getKeys(false).forEach(key -> {
            var section = townsSection.getConfigurationSection(key);
            var townUuid = UUID.fromString(key);
            var name = section.getString("name");
            var town = new Town(townUuid, name);
            town.setRgbColor(section.getInt("color"));
            town.setSpawn(section.getLocation("spawn"));
            town.setOpen(section.getBoolean("open", true));
            town.setMayor(UUID.fromString(section.getString("mayor")));
            var residentUniqueIds = section.getStringList("residents");
            residentUniqueIds.forEach(resident -> {
                var residentUuid = UUID.fromString(resident);
                town.addResident(residentUuid);
                residentsMap.put(residentUuid, townUuid);
            });
            var landChunks = section.getStringList("land");
            landChunks.forEach(land -> {
                var parts = land.split(":");
                var chunk = new LandChunk(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Bukkit.getWorld(parts[2]));
                town.addLand(chunk);
                landMap.put(chunk, townUuid);
            });
            var roles = section.getConfigurationSection("roles");
            // Todo: optimize
            if (roles != null) {
                roles.getKeys(false).forEach(roleName -> {
                    var role = new Role(roleName);
                    var permissions = section.getStringList("roles." + roleName + ".permissions");
                    var players = section.getStringList("roles." + roleName + ".players");

                    for(String permission : permissions) {
                        role.addPermission(Permission.valueOf(permission));
                    }

                    for(String player : players) {
                        role.addPlayer(UUID.fromString(player));
                    }

                    town.addRole(role);
                });
            }
            townMap.put(townUuid, town);
        });
    }

    public void saveData(YamlConfiguration dataConfig) {
        var townsSection = dataConfig.createSection("towns");
        getTowns().forEach(town -> {
            var section = townsSection.createSection(town.getUniqueId().toString());
            section.set("name", town.getName());
            section.set("color", town.getRgbColor());
            section.set("spawn", town.getSpawn());
            section.set("mayor", town.getMayor().toString());
            var residentUniqueIds = town.getResidents().stream().map(UUID::toString).toList();
            section.set("residents", residentUniqueIds);
            var landChunks = town.getClaimedLand().stream()
                    .map(chunk -> chunk.x() + ":" + chunk.z() + ":" + chunk.world().getName())
                    .toList();
            section.set("land", landChunks);
            // Todo: looks janky
            for (Role role : town.getRoles()) {
                List permissions = new ArrayList();
                for (Permission permission : role.getPermissions()) {
                    permissions.add(permission.name());
                }
                section.set("roles." + role.getName() + ".permissions", permissions);

                List players = new ArrayList();
                for(UUID player : role.getPlayers()) {
                    players.add(player.toString());
                }
                section.set("roles." + role.getName() + ".players", players);
            }
        });
    }

    public void addResidentToTown(UUID playerUuid, Town town) {
        town.addResident(playerUuid);
        residentsMap.put(playerUuid, town.getUniqueId());
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
