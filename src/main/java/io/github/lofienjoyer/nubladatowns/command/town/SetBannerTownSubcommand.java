package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Banner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.function.BiConsumer;

public class SetBannerTownSubcommand implements BiConsumer<CommandSender, String[]> {

    private static final int SET_BANNER_COST = 10;

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public SetBannerTownSubcommand(TownManager townManager) {
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

        if (!playerTown.hasPermission(player, Permission.CHANGE_BANNER)) {
            sender.sendMessage(localizationManager.getMessage("no-permission"));
            return;
        }

        if (player.getLocation().distanceSquared(playerTown.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        if (player.getLevel() < SET_BANNER_COST) {
            player.sendMessage(ComponentUtils.replaceInteger(localizationManager.getMessage("not-enough-xp"), "%amount%", SET_BANNER_COST));
            return;
        }

        var item = player.getInventory().getItemInMainHand();
        if (!item.getType().toString().contains("BANNER")) {
            sender.sendMessage(localizationManager.getMessage("not-banner-in-hand"));
            return;
        }

        var meta = (BannerMeta) item.getItemMeta();
        var state = (Banner) item.getType().createBlockData().createBlockState();

        if (meta.getPatterns().isEmpty()) {
            sender.sendMessage(localizationManager.getMessage("invalid-banner"));
            return;
        }

        playerTown.setRgbColor(state.getBaseColor().getColor().asARGB());
        playerTown.setBannerPatterns(meta.getPatterns());
        sender.sendMessage(localizationManager.getMessage("banner-changed"));
        Bukkit.broadcast(ComponentUtils.replaceTownName(localizationManager.getMessage("town-banner-changed", true), playerTown));
    }

}
