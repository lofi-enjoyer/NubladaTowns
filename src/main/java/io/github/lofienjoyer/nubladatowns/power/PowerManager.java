package io.github.lofienjoyer.nubladatowns.power;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PowerManager {

    private Map<String, Integer> power;

    public PowerManager() { reloadConfig(); }

    public void reloadConfig() { this.power = loadPower(NubladaTowns.getInstance()); }

    private Map<String, Integer> loadPower(NubladaTowns instance) {
        var powerFile = new File(instance.getDataFolder(), "power.yml");
        if (!powerFile.exists())
            instance.saveResource("power.yml", false);

        var powerConfig = YamlConfiguration.loadConfiguration(powerFile);
        var defaultPowerConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(instance.getResource("power.yml")));

        var powerMap = new HashMap<String, Integer>();
        for ( String key : defaultPowerConfig.getKeys(true)) {
            var value = powerConfig.getInt(key, defaultPowerConfig.getInt(key));
            powerConfig.set(key, value);
            powerMap.put(key, value);
        }
        try {
            powerConfig.save(powerFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return powerMap;
    }

    public Integer getAmount(String key) {
        return power.get(key);
    }
}
