package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class PowerSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public PowerSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        var town = NubladaTowns.getInstance().getTownManager().getTownByName(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (Exception e) {
            sender.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("invalid-value"), "%value%", args[1]));
            return;
        }

        switch (args[0]) {
            case "get" -> handleGet(town, sender);
            case "add" -> handleAdd(value, town, sender);
            case "set" -> handleSet(value, town, sender);
            default -> sender.sendMessage(localizationManager.getMessage("invalid-command"));
        }
    }

    private void handleAdd(int value, Town town, CommandSender sender) {
        var newValue = town.getPower() + value;
        town.setPower(newValue);
        sender.sendMessage(ComponentUtils.replaceTownName(
                ComponentUtils.replaceInteger(localizationManager.getMessage("town-power-changed"), "%value%", newValue),
                town
        ));
    }

    private void handleSet(int value, Town town, CommandSender sender) {
        town.setPower(value);
        sender.sendMessage(ComponentUtils.replaceTownName(
                ComponentUtils.replaceInteger(localizationManager.getMessage("town-power-changed"), "%value%", value),
                town
        ));
    }

    private void handleGet(Town town, CommandSender sender) {
        var value = town.getPower();
        sender.sendMessage(ComponentUtils.replaceTownName(
                ComponentUtils.replaceInteger(localizationManager.getMessage("town-power"), "%value%", value),
                town
        ));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("get", "add", "set")
                    .filter(s1 -> s1.startsWith(args[0]))
                    .toList();
        }

        if ("get".equals(args[0])) {
            var townName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            return NubladaTowns.getInstance().getTownManager().getTowns().stream()
                    .map(Town::getName)
                    .filter(s1 -> s1.startsWith(townName))
                    .toList();
        } else if (args.length == 2) {
            return List.of("<amount>");
        }

        var townName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        return NubladaTowns.getInstance().getTownManager().getTowns().stream()
                .map(Town::getName)
                .filter(s1 -> s1.startsWith(townName))
                .toList();
    }

}
