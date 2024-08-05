package io.github.lofienjoyer.nubladatowns.command;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.admin.*;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final LocalizationManager localizationManager;
    private final Map<String, SubCommand> subCommands;

    public AdminCommand() {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.subCommands = new HashMap<>();
        subCommands.put("reload", new ReloadSubcommand(localizationManager));
        subCommands.put("power", new PowerSubcommand(localizationManager));
        subCommands.put("delete", new DeleteSubcommand(localizationManager));
        subCommands.put("claim", new ClaimSubcommand(localizationManager));
        subCommands.put("abandon", new AbandonSubcommand(localizationManager));
        subCommands.put("tp", new TeleportSubcommand(localizationManager));
        subCommands.put("load", new LoadSubcommand(localizationManager));
        subCommands.put("save", new SaveSubcommand(localizationManager));
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

        command.accept(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        var commands = subCommands.keySet().stream()
                .filter(s1 -> s1.startsWith(args[0]))
                .toList();

        if (commands.isEmpty())
            return null;

        if (args.length == 1)
            return commands;

        var subCommand = subCommands.get(args[0]);
        if (subCommand != null) {
            return subCommand.onTabComplete(sender, cmd, s, Arrays.copyOfRange(args, 1, args.length));
        } else {
            return null;
        }
    }
}
