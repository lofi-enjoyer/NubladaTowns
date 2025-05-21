package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class EconomySubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public EconomySubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!NubladaTowns.getInstance().isEconomyEnabled()) {
            sender.sendMessage(localizationManager.getMessage("economy-disabled"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        switch (args[0]) {
            case "get" -> handleGet(getTownNameFromArgs(args, 1), sender);
            case "add" -> handleAdd(args, getTownNameFromArgs(args, 2), sender);
            case "set" -> handleSet(args, getTownNameFromArgs(args, 2), sender);
            default -> sender.sendMessage(localizationManager.getMessage("invalid-command"));
        }
    }

    private void handleAdd(String[] args, String townName, CommandSender sender) {
        var town = NubladaTowns.getInstance().getTownManager().getTownByName(townName);
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

        var success = NubladaTowns.getInstance().getEconomyHandler().depositToTown(town, value);
        var newValue = NubladaTowns.getInstance().getEconomyHandler().getTownBalance(town);
        sender.sendMessage(ComponentUtils.replaceTownName(
                ComponentUtils.replaceInteger(localizationManager.getMessage("town-balance-changed"), "%value%", (int) newValue),
                town
        ));
    }

    private void handleSet(String[] args, String townName, CommandSender sender) {
        var town = NubladaTowns.getInstance().getTownManager().getTownByName(townName);
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

        var economyHandler = NubladaTowns.getInstance().getEconomyHandler();
        var balance = economyHandler.getTownBalance(town);
        if (balance > value) {
            economyHandler.withdrawFromTown(town, balance - value);
        } else {
            economyHandler.depositToTown(town, value - balance);
        }
        sender.sendMessage(ComponentUtils.replaceTownName(
                ComponentUtils.replaceInteger(localizationManager.getMessage("town-balance-changed"), "%value%", value),
                town
        ));
    }

    private void handleGet(String townName, CommandSender sender) {
        var town = NubladaTowns.getInstance().getTownManager().getTownByName(townName);
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }
        var value = NubladaTowns.getInstance().getEconomyHandler().getTownBalance(town);
        sender.sendMessage(ComponentUtils.replaceTownName(
                ComponentUtils.replaceInteger(localizationManager.getMessage("town-balance"), "%value%", (int) value),
                town
        ));
    }

    private String getTownNameFromArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
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
