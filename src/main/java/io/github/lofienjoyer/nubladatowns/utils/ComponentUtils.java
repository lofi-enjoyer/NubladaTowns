package io.github.lofienjoyer.nubladatowns.utils;

import io.github.lofienjoyer.nubladatowns.town.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Map;

public class ComponentUtils {

    public static Component getTownNameComponent(Town town) {
        return getTownNameComponent(town.getName(), town.getRgbColor());
    }

    public static Component getTownNameComponent(String name, int color) {
        return Component.text(name, TextColor.color(color));
    }

    public static Component replaceTownName(Component component, Town town) {
        return replaceTownName(component, town.getName(), town.getRgbColor());
    }

    public static Component replaceTownName(Component component, String name, int color) {
        return component.replaceText(builder -> {
            builder.matchLiteral("%town%").replacement(getTownNameComponent(name, color));
        });
    }

    public static Component replacePlayerName(Component component, String name) {
        return component.replaceText(builder -> {
            builder.matchLiteral("%player%").replacement(name);
        });
    }

    public static Component replaceInteger(Component component, String literal, int number) {
        return component.replaceText(builder -> {
            builder.matchLiteral(literal).replacement(String.valueOf(number));
        });
    }

    public static Component replaceIntegers(Component component, Map<String, Integer> replace) {
        for (Map.Entry<String, Integer> entry : replace.entrySet()) {
            component = replaceInteger(component, entry.getKey(), entry.getValue());
        }

        return component;
    }

}
