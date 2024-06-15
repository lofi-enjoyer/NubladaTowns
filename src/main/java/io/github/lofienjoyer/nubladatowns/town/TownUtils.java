package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class TownUtils {

    public static void showTownMenu(Player player, Town town) {
        var lm = NubladaTowns.getInstance().getLocalizationManager();
        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        var content = List.of(
                Component.empty()
                        .append(ComponentUtils.replaceTownName(lm.getMessage("town-menu-title"), town))
                        .appendNewline()
                        .append(ComponentUtils.replaceInteger(lm.getMessage("town-menu-population"), "%count%", town.getResidents().size()))
                        .appendNewline()
                        .append(ComponentUtils.replaceInteger(lm.getMessage("town-menu-land"), "%count%", town.getClaimedLand().size()))
                        .appendNewline()
                        .appendNewline()
                        .append(lm.getMessage("town-menu-resident-list").clickEvent(ClickEvent.runCommand("/nubladatowns:town list " + town.getName())))
        );

        player.openBook(Book.book(title, author, content));
    }

    public static void showResidentsList(Player player, Town town) {
        var residentNames = town.getResidents().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(Objects::nonNull)
                .map(OfflinePlayer::getName)
                .toList();

        var componentList = Component.text()
                .append(NubladaTowns.getInstance().getLocalizationManager().getMessage("resident-list"))
                .appendNewline()
                .appendNewline();

        for (String residentName : residentNames) {
            componentList = componentList
                    .append(Component.text("▪ ", NamedTextColor.GRAY))
                    .append(Component.text(residentName, NamedTextColor.DARK_GRAY))
                    .appendNewline();
        }

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    public static boolean checkNeighborChunks(Chunk chunk, Town town, TownManager townManager) {
        var world = chunk.getWorld();
        return townManager.getTownOnChunk(world.getChunkAt(chunk.getX(), chunk.getZ() + 1)) == town ||
                townManager.getTownOnChunk(world.getChunkAt(chunk.getX(), chunk.getZ() - 1)) == town ||
                townManager.getTownOnChunk(world.getChunkAt(chunk.getX() + 1, chunk.getZ())) == town ||
                townManager.getTownOnChunk(world.getChunkAt(chunk.getX() - 1, chunk.getZ())) == town;
    }

    public static void broadcastToTown(Component component, Town town) {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> town.getResidents().contains(player.getUniqueId()))
                .forEach(player -> {
                    player.sendMessage(component);
                });
    }

}
