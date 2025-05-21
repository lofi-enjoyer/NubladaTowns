package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Town {

    private final UUID uniqueId;
    private String name;
    private int rgbColor;
    private List<Pattern> bannerPatterns;
    private final List<UUID> residents;
    private final List<LandChunk> claimedLand;
    private Location spawn;
    private boolean open;
    private int power;
    private UUID mayor;
    private ArrayList<Role> roles = new ArrayList<>();
    private List<TownHistoryEvent> historyEvents;
    private Inventory inventory;

    public Town(UUID uniqueId, String name, List<UUID> residents, List<LandChunk> claimedLand, List<TownHistoryEvent> historyEvents, List<ItemStack> inventoryItems) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.residents = residents;
        this.claimedLand = claimedLand;
        this.historyEvents = historyEvents;
        this.inventory = Bukkit.createInventory(null, 9, Component.text(name));
        if (inventoryItems != null && !inventoryItems.isEmpty()) {
            for (int i = 0; i < Math.min(9, inventoryItems.size()); i++) {
                inventory.setItem(i, inventoryItems.get(i));
            }
        }
    }

    public Town(String name) {
        this(UUID.randomUUID(), name, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        
        // Añadir rol de Aliados por defecto
        Role aliadosRole = new Role("Aliados");
        // Dar permisos básicos a los aliados: pueden interactuar pero no construir ni destruir
        aliadosRole.addPermission(Permission.INTERACT);
        this.roles.add(aliadosRole);
    }

    protected void addLand(LandChunk chunk) {
        claimedLand.add(chunk);
    }

    protected void addLand(int x, int z, World world) {
        var landChunk = new LandChunk(x, z, world);
        addLand(landChunk);
    }

    protected void removeLand(LandChunk chunk) {
        claimedLand.remove(chunk);
    }

    protected void removeLand(int x, int z, World world) {
        var landChunk = new LandChunk(x, z, world);
        removeLand(landChunk);
    }

    protected void addResident(UUID uuid) {
        residents.add(uuid);
    }

    protected void addResident(Player player) {
        addResident(player.getUniqueId());
    }

    protected void removeResident(UUID uuid) { residents.remove(uuid); }

    protected void removeResident(Player player) { removeResident(player.getUniqueId()); }

    protected void addHistoryEvent(TownHistoryEvent event) {
        historyEvents.add(event);
    }

    public List<UUID> getResidents() {
        return Collections.unmodifiableList(residents);
    }

    public List<LandChunk> getClaimedLand() {
        return Collections.unmodifiableList(claimedLand);
    }

    public List<TownHistoryEvent> getHistoryEvents() {
        return historyEvents;
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

    public List<Pattern> getBannerPatterns() {
        return bannerPatterns;
    }

    public void setBannerPatterns(List<Pattern> bannerPatterns) {
        this.bannerPatterns = bannerPatterns;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getPower() { return power; }

    public void setPower(int power) { this.power = power; }

    public void setMayor(UUID uuid) { this.mayor = uuid; }

    protected void setMayor(Player player) { setMayor(player.getUniqueId()); }

    public UUID getMayor() { return mayor; }

    public Inventory getInventory() {
        return inventory;
    }

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

    // Modificar el método isAlly para considerar a todos los miembros de towns aliados
    public boolean isAlly(UUID playerUuid) {
        // Obtener o crear el rol de Aliados si no existe
        var aliadosRole = getRole("Aliados");
        if (aliadosRole == null) {
            // Recrear el rol de Aliados si fue eliminado
            aliadosRole = new Role("Aliados");
            aliadosRole.addPermission(Permission.INTERACT);
            this.roles.add(aliadosRole);
            // Como acabamos de crear el rol, no hay aliados todavía
            return false;
        }
        
        // Verificar si el jugador es directamente un aliado (para compatibilidad con código existente)
        if (aliadosRole.getPlayers().contains(playerUuid)) {
            return true;
        }
        
        // Verificar si el jugador pertenece a un town aliado
        TownManager townManager = NubladaTowns.getInstance().getTownManager();
        Town playerTown = townManager.getPlayerTown(playerUuid);
        
        // Si el jugador no pertenece a ningún town, no es aliado
        if (playerTown == null) {
            return false;
        }
        
        // Obtener el alcalde del town del jugador
        UUID mayorUUID = playerTown.getMayor();
        if (mayorUUID == null) {
            return false;
        }
        
        // Verificar si el town del jugador está aliado con este town
        // Si el alcalde del town del jugador está en nuestro rol de aliados, consideramos
        // que todos los jugadores de ese town son aliados
        return aliadosRole.getPlayers().contains(mayorUUID);
    }

    public boolean isAlly(Player player) {
        return isAlly(player.getUniqueId());
    }

    // Método para obtener una lista de Towns aliados
    public List<Town> getAlliedTowns() {
        var aliadosRole = getRole("Aliados");
        if (aliadosRole == null) {
            return Collections.emptyList();
        }
        
        List<Town> alliedTowns = new ArrayList<>();
        TownManager townManager = NubladaTowns.getInstance().getTownManager();
        
        // Buscar towns cuyos alcaldes estén en nuestro rol de aliados
        for (UUID playerUuid : aliadosRole.getPlayers()) {
            for (Town town : townManager.getTowns()) {
                UUID mayor = town.getMayor();
                // Evitar NullPointerException si el alcalde es null
                if (mayor != null && mayor.equals(playerUuid)) {
                    alliedTowns.add(town);
                    break;
                }
            }
        }
        
        return alliedTowns;
    }

    // Método para verificar si un town es aliado
    public boolean isAlliedWith(Town otherTown) {
        if (otherTown == null) {
            return false;
        }
        
        var aliadosRole = getRole("Aliados");
        if (aliadosRole == null) {
            return false;
        }
        
        UUID otherMayor = otherTown.getMayor();
        // Evitar NPE si el alcalde del otro town es null
        return otherMayor != null && aliadosRole.getPlayers().contains(otherMayor);
    }
}
