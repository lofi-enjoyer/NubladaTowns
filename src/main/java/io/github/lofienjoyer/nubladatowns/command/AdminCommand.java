package io.github.lofienjoyer.nubladatowns.command;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.admin.PowerSubcommand;
import io.github.lofienjoyer.nubladatowns.command.admin.ReloadSubcommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
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

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final LocalizationManager localizationManager;
    private final Map<String, BiConsumer<CommandSender, String[]>> subCommands;

    public AdminCommand() {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.subCommands = new HashMap<>();
        subCommands.put("reload", new ReloadSubcommand(localizationManager));
        subCommands.put("power", new PowerSubcommand(localizationManager));
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
        if (args.length > 1)
            return null;
        return List.copyOf(subCommands.keySet());
    }
}
