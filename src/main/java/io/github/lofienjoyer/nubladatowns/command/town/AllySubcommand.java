package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class AllySubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public AllySubcommand(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.townManager = townManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        Town playerTown = townManager.getPlayerTown(player);
        if (playerTown == null) {
            sender.sendMessage(localizationManager.getMessage("not-in-a-town"));
            return;
        }

        // Solo el alcalde o usuarios con permiso MANAGE_ROLES pueden manejar alianzas
        if (!playerTown.hasPermission(player, Permission.MANAGE_ROLES)) {
            player.sendMessage(localizationManager.getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        String action = args[0].toLowerCase();
        String targetTownName = args[1];
        Town targetTown = townManager.getTownByName(targetTownName);

        if (targetTown == null) {
            player.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }

        // No permitir alianzas con su propio town
        if (playerTown.getUniqueId().equals(targetTown.getUniqueId())) {
            player.sendMessage("No puedes crear una alianza con tu propio town.");
            return;
        }

        switch (action) {
            case "add" -> {
                // Buscar el rol de Aliados en ambos towns
                var playerTownAliadosRole = getAliadosRole(playerTown);
                var targetTownAliadosRole = getAliadosRole(targetTown);
                
                if (playerTownAliadosRole == null) {
                    player.sendMessage("Tu town no tiene un rol de Aliados.");
                    return;
                }
                
                if (targetTownAliadosRole == null) {
                    player.sendMessage("El town objetivo no tiene un rol de Aliados.");
                    return;
                }
                
                // Agregar el alcalde del town objetivo como miembro del rol Aliados en tu town
                playerTownAliadosRole.addPlayer(targetTown.getMayor());
                
                // Notificar al jugador
                player.sendMessage("Se ha agregado a " + targetTownName + " como aliado de tu town.");
                
                // Enviar mensaje a todos los residentes del town objetivo
                targetTown.getResidents().forEach(residentUuid -> {
                    var resident = NubladaTowns.getInstance().getServer().getPlayer(residentUuid);
                    if (resident != null && resident.isOnline()) {
                        resident.sendMessage(playerTown.getName() + " ha establecido una alianza con tu town.");
                    }
                });
            }
            case "remove" -> {
                // Buscar el rol de Aliados en tu town
                var playerTownAliadosRole = getAliadosRole(playerTown);
                
                if (playerTownAliadosRole == null) {
                    player.sendMessage("Tu town no tiene un rol de Aliados.");
                    return;
                }
                
                // Verificar que el town objetivo es un aliado
                if (!playerTown.isAlliedWith(targetTown)) {
                    player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-not-allied"), targetTown));
                    return;
                }
                
                // Eliminar al alcalde del town objetivo como miembro del rol Aliados en tu town
                playerTownAliadosRole.removePlayer(targetTown.getMayor());
                
                // Notificar al jugador
                var replacements = Map.of("%town1%", playerTown.getName(), "%town2%", targetTown.getName());
                player.sendMessage(ComponentUtils.replaceStrings(localizationManager.getMessage("alliance-broken", true), replacements));
                player.playSound(player, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f);
                
                // Notificar al town objetivo
                targetTown.getResidents().forEach(residentUuid -> {
                    var resident = NubladaTowns.getInstance().getServer().getPlayer(residentUuid);
                    if (resident != null && resident.isOnline()) {
                        resident.sendMessage(ComponentUtils.replaceStrings(localizationManager.getMessage("alliance-broken", true), replacements));
                    }
                });
            }
            default -> {
                player.sendMessage("Uso: /town ally <add|remove> <nombre del town>");
            }
        }
    }
    
    // Método para obtener el rol de Aliados de un town
    private io.github.lofienjoyer.nubladatowns.roles.Role getAliadosRole(Town town) {
        var role = town.getRole("Aliados");
        if (role == null) {
            // Recrear el rol de Aliados si fue eliminado
            role = new Role("Aliados");
            role.addPermission(Permission.INTERACT);
            town.addRole(role);
        }
        
        // Verificar si existe el rol de Asistente y crearlo si no existe
        checkAndCreateAsistenteRole(town);
        
        return role;
    }
    
    // Método para verificar y crear el rol de Asistente si no existe
    private void checkAndCreateAsistenteRole(Town town) {
        if (town.getRole("Asistente") == null) {
            Role asistenteRole = new Role("Asistente");
            // Configurar permisos básicos para el rol de Asistente
            asistenteRole.addPermission(Permission.BUILD);
            asistenteRole.addPermission(Permission.DESTROY);
            asistenteRole.addPermission(Permission.INTERACT);
            town.addRole(asistenteRole);
        }
    }
} 