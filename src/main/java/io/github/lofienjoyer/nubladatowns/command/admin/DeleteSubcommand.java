package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeleteSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public DeleteSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        var town = NubladaTowns.getInstance().getTownManager().getTownByName(String.join(" ", args));
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }

        NubladaTowns.getInstance().getTownManager().removeTown(town);
        Bukkit.broadcast(ComponentUtils.replaceTownName(localizationManager.getMessage("town-deleted"), town));
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
