package io.github.lofienjoyer.nubladatowns.utils;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

public class SoundUtils {

    public static void playAscendingSound(Location location, Sound sound, float initialPitch, float increment, int amount) {
        for (int i = 0; i < amount; i++) {
            float pitch = initialPitch + increment * i;
            Bukkit.getScheduler().runTaskLater(NubladaTowns.getInstance(), () -> {
                location.getWorld().playSound(location, sound, 1, pitch);
            }, i * 5L);
        }
    }

}
