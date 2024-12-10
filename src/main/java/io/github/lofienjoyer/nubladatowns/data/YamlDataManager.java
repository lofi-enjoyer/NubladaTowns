package io.github.lofienjoyer.nubladatowns.data;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import io.github.lofienjoyer.nubladatowns.town.LandChunk;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownHistoryEvent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class YamlDataManager implements DataManager {

    private static final SimpleDateFormat HISTORY_EVENTS_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm");

    private final File file;

    public YamlDataManager(File file) {
        this.file = file;
    }

    @Override
    public Collection<Town> loadTowns() {
        if (!file.exists())
            return new ArrayList<>();

        var dataConfig = YamlConfiguration.loadConfiguration(file);
        var towns = new ArrayList<Town>();

        // Reminder: assign defaults to the ConfigurationSection::getSomething methods when adding new data to the Town class
        var townsSection = dataConfig.getConfigurationSection("towns");
        townsSection.getKeys(false).forEach(key -> {
            var section = townsSection.getConfigurationSection(key);
            var townUuid = UUID.fromString(key);
            var name = section.getString("name");
            var residentUniqueIds = section.getStringList("residents").stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            var landChunks = section.getStringList("land").stream()
                    .map(s -> {
                        var parts = s.split(":");
                        return new LandChunk(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Bukkit.getWorld(parts[2]));
                    })
                    .collect(Collectors.toList());

            var historyEvents = parseTownHistoryEvents(section.getStringList("history"));

            var town = new Town(townUuid, name, residentUniqueIds, landChunks, historyEvents);
            town.setRgbColor(section.getInt("color"));
            var patterns = section.getStringList("banner-patterns").stream()
                    .map(s -> {
                        var parts = s.split(":");
                        return new Pattern(DyeColor.getByColor(Color.fromARGB(Integer.parseInt(parts[0]))), PatternType.valueOf(parts[1]));
                    })
                    .toList();
            town.setBannerPatterns(patterns);
            town.setSpawn(section.getLocation("spawn"));
            town.setOpen(section.getBoolean("open", true));
            town.setPower(section.getInt("power", 0));
            town.setMayor(UUID.fromString(section.getString("mayor")));

            var roles = section.getConfigurationSection("roles");
            if (roles != null) {
                roles.getKeys(false).forEach(roleName -> {
                    var role = new Role(roleName);
                    var permissions = section.getStringList("roles." + roleName + ".permissions");
                    var players = section.getStringList("roles." + roleName + ".players");

                    permissions.forEach(permission -> {
                        role.addPermission(Permission.valueOf(permission));
                    });

                    players.forEach(uuid -> {
                        role.addPlayer(UUID.fromString(uuid));
                    });

                    town.addRole(role);
                });
            }

            towns.add(town);
        });

        return towns;
    }

    @Override
    public void save(Collection<Town> towns) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }

        var dataConfig = new YamlConfiguration();

        var townsSection = dataConfig.createSection("towns");
        towns.forEach(town -> {
            var section = townsSection.createSection(town.getUniqueId().toString());
            section.set("name", town.getName());
            section.set("color", town.getRgbColor());
            var patterns = town.getBannerPatterns().stream()
                    .map(pattern -> {
                        return pattern.getColor().getColor().asARGB() + ":" + pattern.getPattern().name();
                    })
                    .toList();
            section.set("banner-patterns", patterns);
            section.set("spawn", town.getSpawn());
            section.set("power", town.getPower());
            section.set("mayor", town.getMayor().toString());
            var residentUniqueIds = town.getResidents().stream().map(UUID::toString).toList();
            section.set("residents", residentUniqueIds);
            var landChunks = town.getClaimedLand().stream()
                    .map(chunk -> chunk.x() + ":" + chunk.z() + ":" + chunk.world().getName())
                    .toList();
            section.set("land", landChunks);
            for (Role role : town.getRoles()) {
                var permissions = role.getPermissions().stream().map(Enum::name).toList();
                var players = role.getPlayers().stream().map(UUID::toString).toList();

                section.set("roles." + role.getName() + ".permissions", permissions);
                section.set("roles." + role.getName() + ".players", players);
            }
            var historyEvents = town.getHistoryEvents().stream()
                    .map(event -> {
                        return String.format("%s:%s:%s", HISTORY_EVENTS_TIMESTAMP_FORMAT.format(event.getTimestamp()), event.getDescription().replace(":", ""), event.getType());
                    })
                    .toList();
            section.set("history", historyEvents);
        });

        dataConfig.save(file);
    }

    private List<TownHistoryEvent> parseTownHistoryEvents(List<String> eventsList) {
        return eventsList.stream()
                .map(s -> {
                    try {
                        var parts = s.split(":");
                        var date = HISTORY_EVENTS_TIMESTAMP_FORMAT.parse(parts[0]);
                        return new TownHistoryEvent(date, parts[1], TownHistoryEvent.Type.valueOf(parts[2].toUpperCase()));
                    } catch (Exception e) {
                        NubladaTowns.getInstance().getLogger().severe(String.format("Could not parse town event: '%s'", s));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
