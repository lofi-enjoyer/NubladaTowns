package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class EditSubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public EditSubcommand(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.townManager = townManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        Town town = townManager.getPlayerTown(player);
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("not-in-a-town"));
            return;
        }

        if (player.getLocation().distanceSquared(town.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        if (args[0].equals("role")) {
            if (args.length < 2) {
                player.sendMessage(localizationManager.getMessage("not-enough-arguments"));
                return;
            }

            Role role = town.getRole(args[1]);
            if (role == null) {
                player.sendMessage(localizationManager.getMessage("non-existent-role"));
                return;
            }
            TownUtils.showRoleEditor(player, town, role);
        }
    }

}
