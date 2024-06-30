package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.plot.Plot;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
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
    private int power;
    private UUID mayor;
    private List<Role> roles;
    private List<Plot> plots;

    public Town(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.residents = new ArrayList<>();
        this.claimedLand = new ArrayList<>();
        this.roles = new ArrayList<>();
    }

    public Town(String name) {
        this(UUID.randomUUID(), name);
    }

    protected void addLand(LandChunk chunk) {
        claimedLand.add(chunk);
    }

    protected void addLand(int x, int z, World world) {
        var landChunk = new LandChunk(x, z, world);
        claimedLand.add(landChunk);
    }

    protected void addResident(UUID uuid) {
        residents.add(uuid);
    }

    protected void addResident(Player player) {
        addResident(player.getUniqueId());
    }

    protected void removeResident(UUID uuid) { residents.remove(uuid); }

    protected void removeResident(Player player) { removeResident(player.getUniqueId()); }

    protected void addPlot(Plot plot) {
        plots.add(plot);
    }

    protected void removePlot(Plot plot) {
        plots.remove(plot);
    }

    public List<UUID> getResidents() {
        return Collections.unmodifiableList(residents);
    }

    public List<LandChunk> getClaimedLand() {
        return Collections.unmodifiableList(claimedLand);
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public Location getSpawn() {
        return spawn;
    }

    protected void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public int getRgbColor() {
        return rgbColor;
    }

    protected void setRgbColor(int rgbColor) {
        this.rgbColor = rgbColor;
    }

    public boolean isOpen() {
        return open;
    }

    protected void setOpen(boolean open) {
        this.open = open;
    }

    public int getPower() { return power; }

    public void setPower(int power) { this.power = power; }
  
    public void setMayor(UUID uuid) { this.mayor = uuid; }

    public void setMayor(Player player) { setMayor(player.getUniqueId()); }

    public UUID getMayor() { return mayor; }

    public void addRole(Role role) { this.roles.add(role); }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public List<Role> getRoles() { return roles; }

    public Role getRole(String name) {
        for (Role role : roles) {
            if(role.getName().equals(name)) return role;
        }

        return null;
    }

    protected void setRoles(ArrayList<Role> roles) { this.roles = roles; }

    public boolean hasPermission(UUID uuid, Permission permission) {
        if(getMayor().equals(uuid))
            return true;

        for(Role role : getRoles()) {
            if(role.getPlayers().contains(uuid) && role.getPermissions().contains(permission))
                return true;
        }

        return false;
    }

    public boolean hasPermission(Player player, Permission permission) {
        return hasPermission(player.getUniqueId(), permission);
    }
}
