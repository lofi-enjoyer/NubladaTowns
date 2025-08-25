package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoadSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public LoadSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (args.length < 1 || !"--confirm".equals(args[0])) {
            sender.sendMessage(localizationManager.getMessage("confirm-loading"));
            return;
        }

        sender.sendMessage(localizationManager.getMessage("loading-data"));
        NubladaTowns.getInstance().loadData();
        sender.sendMessage(localizationManager.getMessage("data-loaded", true));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }

}
