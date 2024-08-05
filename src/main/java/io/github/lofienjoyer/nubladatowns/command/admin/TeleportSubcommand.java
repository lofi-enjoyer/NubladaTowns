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

public class TeleportSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public TeleportSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        var town = NubladaTowns.getInstance().getTownManager().getTownByName(String.join(" ", args));
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }

        player.teleportAsync(town.getSpawn().clone().add(0.5, 1, 0.5));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        var townName = String.join(" ", args);

        return NubladaTowns.getInstance().getTownManager().getTowns().stream()
                .map(Town::getName)
                .filter(s1 -> s1.startsWith(townName))
                .toList();
    }

}
