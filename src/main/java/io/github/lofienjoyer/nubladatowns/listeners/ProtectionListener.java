package io.github.lofienjoyer.nubladatowns.listeners;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.core.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Permission;
import io.github.lofienjoyer.nubladatowns.town.Role;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent.Action;
import org.bukkit.entity.Player;
import org.bukkit.entity.Animals;
import java.util.Objects;

public class ProtectionListener implements Listener {
    private final TownManager townManager;
    private final LocalizationManager localizationManager;

    public ProtectionListener() {
        this.townManager = NubladaTowns.getInstance().getTownManager();
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();
        if (player.hasPermission("nubladatowns.admin"))
            return;

        var currentTown = townManager.getTownOnChunk(event.getBlock().getChunk());
        if (currentTown == null)
            return;

        // Verificar si el jugador puede romper bloques
        if (!currentTown.hasPermission(player, Permission.DESTROY)) {
            // Verificar si es un aliado con permiso para destruir
            Role aliadosRole = currentTown.getRole("Aliados");
            if (!(currentTown.isAlly(player) && aliadosRole != null && 
                  aliadosRole.getPermissions().contains(Permission.DESTROY))) {
                player.sendActionBar(localizationManager.getMessage("cannot-break-here"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        if (player.hasPermission("nubladatowns.admin"))
            return;

        var currentTown = townManager.getTownOnChunk(event.getBlock().getChunk());
        if (currentTown == null)
            return;

        // Verificar si el jugador puede colocar bloques
        if (!currentTown.hasPermission(player, Permission.BUILD)) {
            // Verificar si es un aliado con permiso para construir
            Role aliadosRole = currentTown.getRole("Aliados");
            if (!(currentTown.isAlly(player) && aliadosRole != null && 
                  aliadosRole.getPermissions().contains(Permission.BUILD))) {
                player.sendActionBar(localizationManager.getMessage("cannot-place-here"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Ignorar eventos de aire o nulos
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR)
            return;
        
        var player = event.getPlayer();
        if (player.hasPermission("nubladatowns.admin"))
            return;
        
        var block = event.getClickedBlock();
        if (block == null)
            return;
        
        var currentTown = townManager.getTownOnChunk(block.getChunk());
        if (currentTown == null)
            return;
        
        // Verificar si el jugador puede interactuar con bloques
        if (!currentTown.hasPermission(player, Permission.INTERACT)) {
            // Verificar si es un aliado con permiso para interactuar
            Role aliadosRole = currentTown.getRole("Aliados");
            if (!(currentTown.isAlly(player) && aliadosRole != null && 
                  aliadosRole.getPermissions().contains(Permission.INTERACT))) {
                player.sendActionBar(localizationManager.getMessage("cannot-interact-here"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager))
            return;
        if (!(event.getEntity() instanceof Animals))
            return;
        if (damager.hasPermission("nubladatowns.admin"))
            return;

        var eventTown = townManager.getTownOnChunk(event.getEntity().getChunk());
        // Permitir atacar animales si está fuera de un town o es miembro del mismo town
        if (eventTown == null || 
            Objects.equals(townManager.getPlayerTown(damager), eventTown))
            return;
        
        // Verificar si es un aliado con permiso para interactuar
        Role aliadosRole = eventTown.getRole("Aliados");
        if (eventTown.isAlly(damager) && aliadosRole != null && 
            aliadosRole.getPermissions().contains(Permission.INTERACT))
            return;

        damager.sendActionBar(localizationManager.getMessage("cannot-attack-animals-here"));
        event.setCancelled(true);
    }
} 