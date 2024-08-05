package io.github.lofienjoyer.nubladatowns.town;

import org.bukkit.World;

import java.util.Objects;

public record LandChunk(int x, int z, World world) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandChunk landChunk = (LandChunk) o;
        return x == landChunk.x && z == landChunk.z && Objects.equals(world.getUID(), landChunk.world.getUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, world.getUID());
    }
}
