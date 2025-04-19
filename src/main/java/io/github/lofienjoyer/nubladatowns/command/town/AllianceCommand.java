package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class AllianceCommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public AllianceCommand(TownManager townManager) {
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

        // Mostrar lista de alianzas actuales
        var allies = playerTown.getAlliedTowns();
        
        player.sendMessage(Component.text("=== Alianzas actuales de " + playerTown.getName() + " ===", NamedTextColor.GOLD));
        
        if (allies.isEmpty()) {
            player.sendMessage(Component.text("Tu town no tiene alianzas actualmente.", NamedTextColor.YELLOW));
        } else {
            for (Town ally : allies) {
                player.sendMessage(Component.text("- " + ally.getName(), NamedTextColor.GREEN));
            }
        }

        // Añadir información sobre cómo crear alianzas
        if (playerTown.hasPermission(player, Permission.MANAGE_ROLES)) {
            player.sendMessage(Component.text("", NamedTextColor.WHITE));
            player.sendMessage(Component.text("=== Cómo gestionar alianzas ===", NamedTextColor.GOLD));
            player.sendMessage(Component.text("1. Para crear una alianza:", NamedTextColor.WHITE));
            player.sendMessage(Component.text("   - Consigue un banner del town con el que quieres aliarte", NamedTextColor.GRAY));
            player.sendMessage(Component.text("   - Renómbralo con el nombre exacto del town", NamedTextColor.GRAY));
            player.sendMessage(Component.text("   - Haz clic derecho en el lectern de tu town con el banner", NamedTextColor.GRAY));
            player.sendMessage(Component.text("2. Para eliminar una alianza:", NamedTextColor.WHITE));
            player.sendMessage(Component.text("   - Usa el comando: /town ally remove <nombre_town>", NamedTextColor.GRAY));
            player.sendMessage(Component.text("", NamedTextColor.WHITE));
            player.sendMessage(Component.text("Nota: Todos los miembros de un town aliado tendrán", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("los permisos definidos en el rol 'Aliados'.", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Puedes editar este rol como cualquier otro.", NamedTextColor.YELLOW));
        }
    }
} 