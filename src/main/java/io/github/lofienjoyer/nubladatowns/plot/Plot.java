package io.github.lofienjoyer.nubladatowns.plot;

import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Plot {
    private final UUID uuid;
    private UUID ownerUuid;
    private final Vector min;
    private final Vector max;
    private final String name;
    private final int argbColor;
    private final World world;
    private final List<UUID> members;

    public Plot(UUID uuid, UUID ownerUuid, Vector min, Vector max, String name, int argbColor, World world, List<UUID> members) {
        this.uuid = uuid;
        this.ownerUuid = ownerUuid;
        this.min = min;
        this.max = max;
        this.name = name;
        this.argbColor = argbColor;
        this.world = world;
        this.members = members;
    }

    public UUID uuid() {
        return uuid;
    }

    public UUID ownerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public Vector min() {
        return min;
    }

    public Vector max() {
        return max;
    }

    public String name() {
        return name;
    }

    public int argbColor() {
        return argbColor;
    }

    public World world() {
        return world;
    }

    public List<UUID> members() {
        return members;
    }

}
