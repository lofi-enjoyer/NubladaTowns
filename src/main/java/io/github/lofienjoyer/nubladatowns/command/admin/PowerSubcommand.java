package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class PowerSubcommand implements BiConsumer<CommandSender, String[]> {

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

}
