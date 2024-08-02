package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class LeaveTownSubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public LeaveTownSubcommand(TownManager townManager) {
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
        if (playerTown == null) {
            sender.sendMessage(localizationManager.getMessage("not-in-a-town"));
            return;
        }

        if (playerTown.getMayor().equals(player.getUniqueId()) && playerTown.getResidents().size() > 1) {
            sender.sendMessage(localizationManager.getMessage("mayor-cannot-leave"));
            return;
        }

        if (player.getLocation().distanceSquared(playerTown.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        var playerUuid = player.getUniqueId();
        townManager.removeResidentFromTown(playerUuid, playerTown);
        sender.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("left-town"), playerTown));
        TownUtils.broadcastToTown(ComponentUtils.replacePlayerName(localizationManager.getMessage("player-left-town"), player.getName()), playerTown);
    }

}
