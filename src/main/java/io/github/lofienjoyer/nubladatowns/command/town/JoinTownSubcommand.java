package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

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

        if (player.getLocation().distanceSquared(town.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        var itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.BOOK || !itemInHand.hasItemMeta()) {
            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-invite-required"), town));
            return;
        }

        var pdc = itemInHand.getItemMeta().getPersistentDataContainer();
        var inviteTownUuid = pdc.get(NubladaTowns.Keys.TOWN_INVITE_KEY, PersistentDataType.STRING);
        if (!town.getUniqueId().toString().equals(inviteTownUuid)) {
            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-invite-wrong-town"), town));
            return;
        }

        var playerUuid = player.getUniqueId();
        townManager.addResidentToTown(playerUuid, town);
        itemInHand.subtract();
        sender.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("joined-town"), town));
        TownUtils.broadcastToTown(ComponentUtils.replacePlayerName(localizationManager.getMessage("player-joined-town"), player.getName()), town);
    }

}
