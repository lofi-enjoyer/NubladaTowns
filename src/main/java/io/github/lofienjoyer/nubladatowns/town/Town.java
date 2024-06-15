package io.github.lofienjoyer.nubladatowns.town;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Town {

    private final UUID uniqueId;
    private String name;
    private int rgbColor;
    private final List<UUID> residents;
    private final List<LandChunk> claimedLand;
    private Location spawn;
    private boolean open;

    public Town(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.residents = new ArrayList<>();
        this.claimedLand = new ArrayList<>();
    }

    public Town(String name) {
        this(UUID.randomUUID(), name);
    }

    public void addLand(LandChunk chunk) {
        claimedLand.add(chunk);
    }

    public void addLand(int x, int z, World world) {
        var landChunk = new LandChunk(x, z, world);
        claimedLand.add(landChunk);
    }

    public List<LandChunk> getClaimedLand() {
        return claimedLand;
    }

    public void addResident(UUID uuid) {
        residents.add(uuid);
    }

    public void addResident(Player player) {
        addResident(player.getUniqueId());
    }

    public List<UUID> getResidents() {
        return residents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public int getRgbColor() {
        return rgbColor;
    }

    public void setRgbColor(int rgbColor) {
        this.rgbColor = rgbColor;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

}
