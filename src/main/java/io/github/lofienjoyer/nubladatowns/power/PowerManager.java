package io.github.lofienjoyer.nubladatowns.power;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import org.bukkit.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PowerManager {

    private Map<EntityType, Integer> power;

    public PowerManager() {
        reloadConfig();
    }

    public void reloadConfig() {
        this.power = loadPower(NubladaTowns.getInstance());
    }

    private Map<EntityType, Integer> loadPower(NubladaTowns instance) {
        var powerSection = instance.getConfig().getConfigurationSection("power");
        if (powerSection == null)
            return Map.of();

        var powerMap = new HashMap<EntityType, Integer>();
        powerSection.getKeys(false).forEach(key -> {
            var entityType = EntityType.fromName(key);
            if (entityType != null)
                powerMap.put(entityType, powerSection.getInt(key));
        });
        return powerMap;
    }

    public Integer getAmount(EntityType key) {
        return power.getOrDefault(key, NubladaTowns.getInstance().getConfigValues().getDefaultPowerAmount());
    }
}
