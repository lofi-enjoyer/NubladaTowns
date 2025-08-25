package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbandonSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public AbandonSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        var currentTown = NubladaTowns.getInstance().getTownManager().abandonChunk(player.getChunk());
        if (currentTown != null) {
            player.sendMessage(localizationManager.getMessage("land-abandoned-successfully"));
            Bukkit.getOnlinePlayers().forEach(resident -> {
                resident.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-abandoned-land", true), currentTown));
            });
        } else {
            player.sendMessage(localizationManager.getMessage("land-not-claimed-yet"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }

}
