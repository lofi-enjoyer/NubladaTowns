package io.github.lofienjoyer.nubladatowns.plot;

import org.bukkit.util.Vector;

import java.util.UUID;

public record Plot(UUID townUuid, Vector min, Vector max) {

}
