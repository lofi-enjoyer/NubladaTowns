package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class JoinTownSubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public JoinTownSubcommand(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.townManager = townManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        var playerTown = townManager.getPlayerTown(player);
        if (playerTown != null) {
            sender.sendMessage(localizationManager.getMessage("already-on-a-town"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        var town = townManager.getTownByName(String.join(" ", args));
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }

        var playerUuid = player.getUniqueId();
        townManager.addResidentToTown(playerUuid, town);
        sender.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("joined-town"), town));
        TownUtils.broadcastToTown(ComponentUtils.replacePlayerName(localizationManager.getMessage("player-joined-town"), player.getName()), town);
    }

}
