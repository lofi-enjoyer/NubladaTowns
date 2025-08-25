package io.github.lofienjoyer.nubladatowns.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.function.BiConsumer;

public interface SubCommand extends BiConsumer<CommandSender, String[]>, TabCompleter {

}
