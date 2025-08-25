package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class SaveSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public SaveSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        sender.sendMessage(localizationManager.getMessage("saving-data"));
        try {
            NubladaTowns.getInstance().saveData();
        } catch (IOException e) {
            sender.sendMessage(localizationManager.getMessage("error-loading-data", true));
            throw new RuntimeException(e);
        }
        sender.sendMessage(localizationManager.getMessage("data-saved", true));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }

}
