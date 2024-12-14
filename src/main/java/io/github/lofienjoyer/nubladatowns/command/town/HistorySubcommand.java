package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class HistorySubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public HistorySubcommand(TownManager townManager) {
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
        var town = townManager.getPlayerTown(player);
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("not-in-a-town"));
            return;
        }

        if (player.getLocation().distanceSquared(town.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        var page = 0;
        if (args.length != 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception ignored) {

            }
        }

        TownUtils.showTownHistory(player, town, page);
    }

}
