package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class ReloadSubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;

    public ReloadSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        sender.sendMessage(localizationManager.getMessage("reloading-plugin"));
        NubladaTowns.getInstance().reloadPlugin();
        sender.sendMessage(localizationManager.getMessage("plugin-reloaded"));
    }

}
