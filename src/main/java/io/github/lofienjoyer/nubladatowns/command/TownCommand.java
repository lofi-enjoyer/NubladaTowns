package io.github.lofienjoyer.nubladatowns.command;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.town.*;
import io.github.lofienjoyer.nubladatowns.command.town.EditSubcommand;
import io.github.lofienjoyer.nubladatowns.command.town.JoinTownSubcommand;
import io.github.lofienjoyer.nubladatowns.command.town.ListResidentsSubcommand;
import io.github.lofienjoyer.nubladatowns.command.town.RolesSubcommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class TownCommand implements CommandExecutor, TabCompleter {

    private final TownManager townManager;
    private final LocalizationManager localizationManager;
    private final Map<String, BiConsumer<CommandSender, String[]>> subCommands;

    public TownCommand(TownManager townManager) {
        this.townManager = townManager;
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();

        this.subCommands = new HashMap<>();
        subCommands.put("list", new ListResidentsSubcommand(townManager));
        subCommands.put("join", new JoinTownSubcommand(townManager));
        subCommands.put("roles", new RolesSubcommand(townManager));
        subCommands.put("edit", new EditSubcommand(townManager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return true;
        }

        var command = subCommands.get(args[0]);
        if (command == null) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return true;
        }

        if (args.length > 1) {
            command.accept(sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            command.accept(sender, new String[0]);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 1)
            return null;
        return List.copyOf(subCommands.keySet());
    }

}
