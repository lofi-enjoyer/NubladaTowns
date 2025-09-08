package io.github.lofienjoyer.nubladatowns.plot;

import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public record Plot(UUID ownerUuid, Vector min, Vector max, String name, int argbColor, World world, List<UUID> members) {

}
