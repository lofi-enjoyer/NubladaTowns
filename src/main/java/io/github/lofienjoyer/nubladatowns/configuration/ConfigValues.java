package io.github.lofienjoyer.nubladatowns.configuration;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import lombok.Getter;

@Getter
public class ConfigValues {

    private final int maxRolesPerTown = getValue("max-roles-per-town", Integer.class, 8);
    private final int claimLandPower = getValue("claim-land-power", Integer.class, 16);
    private final int maxTownPowerMultiplier = getValue("max-town-power-multiplier", Integer.class, 64);
    private final int defaultPowerAmount = getValue("default-entity-power-amount", Integer.class, 1);
    private final int townInviteXpLevels = getValue("town-invite-xp-levels", Integer.class, 5);

    public static <T> T getValue(String path, Class<T> type, T defaultValue) {
        var value = NubladaTowns.getInstance().getConfig().get(path);
        if (value == null) {
            NubladaTowns.getInstance().getLogger().warning(String.format("Trying to load a non-existent configuration value: %s (defaulting to %s)", path, defaultValue));
            return defaultValue;
        }
        if (!type.isInstance(value)) {
            NubladaTowns.getInstance().getLogger().warning(String.format("Trying to load an invalid configuration value: %s (defaulting to %s)", path, defaultValue));
            return defaultValue;
        }
        return (T) value;
    }

}
