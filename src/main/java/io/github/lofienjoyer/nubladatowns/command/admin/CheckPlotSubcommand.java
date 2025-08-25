package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.plot.PlotUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CheckPlotSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public CheckPlotSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            PlotUtils.getPlotAtLocation(player.getLocation()).ifPresentOrElse(plot -> {
                var owner = Bukkit.getOfflinePlayer(plot.ownerUuid());
                var members = plot.members().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toList();
                player.sendMessage(Component.text()
                        .append(Component.text("Owner: " + owner.getName()))
                        .append(Component.text("Members: " + String.join(", ", members)))
                );
            }, () -> {
                player.sendMessage(Component.text("No plot here", TextColor.color(1f, 0f, 0f)));
            });
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

}
