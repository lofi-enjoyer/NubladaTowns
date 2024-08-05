package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ListResidentsSubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public ListResidentsSubcommand(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.townManager = townManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        // TODO: Check if using own town's lectern
        Town town;
        if (args.length == 0) {
            town = townManager.getPlayerTown(player);
            if (town == null) {
                sender.sendMessage(localizationManager.getMessage("not-in-a-town"));
                return;
            }
        } else {
            town = townManager.getTownByName(String.join(" ", args));
            if (town == null) {
                sender.sendMessage(localizationManager.getMessage("non-existent-town"));
                return;
            }
        }

        if (player.getLocation().distanceSquared(town.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        TownUtils.showResidentsList(player, town);
    }

}
