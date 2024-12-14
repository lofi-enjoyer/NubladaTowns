package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TownUtils {

    private static final SimpleDateFormat HISTORY_EVENTS_TIMESTAMP_FORMAT = new SimpleDateFormat("dd/MM/yy");
    private static final LocalizationManager lm = NubladaTowns.getInstance().getLocalizationManager();


    public static void showTownMenu(Player player, Town town) {
        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        var power = Map.of("%count%", town.getPower(), "%max_count%", NubladaTowns.getInstance().getConfigValues().getMaxTownPowerMultiplier() * town.getResidents().size());
        var content = Component.empty()
                .append(ComponentUtils.replaceTownName(lm.getMessage("town-menu-title"), town))
                .appendNewline()
                .append(ComponentUtils.replaceInteger(lm.getMessage("town-menu-population"), "%count%", town.getResidents().size()))
                .appendNewline()
                .append(ComponentUtils.replacePlayerName(lm.getMessage("town-menu-mayor"), Bukkit.getOfflinePlayer(town.getMayor()).getName()))
                .appendNewline()
                .append(ComponentUtils.replaceInteger(lm.getMessage("town-menu-land"), "%count%", town.getClaimedLand().size()))
                .appendNewline()
                .append(ComponentUtils.replaceIntegers(lm.getMessage("town-menu-power"), power))
                .appendNewline()
                .appendNewline()
                .append(lm.getMessage("town-menu-resident-list").clickEvent(ClickEvent.runCommand("/nubladatowns:town list " + town.getName())))
                .appendNewline()
                .append(lm.getMessage("town-menu-roles-list").clickEvent(ClickEvent.runCommand("/nubladatowns:town roles " + town.getName())))
                .appendNewline()
                .append(lm.getMessage("town-history-list").clickEvent(ClickEvent.runCommand("/nubladatowns:town history")));

        if (town.hasPermission(player, Permission.CHANGE_BANNER)) {
            content = content.appendNewline()
                    .append(lm.getMessage("town-menu-change-banner").clickEvent(ClickEvent.runCommand("/nubladatowns:town setbanner")));
        }

        var playerCity = NubladaTowns.getInstance().getTownManager().getPlayerTown(player);
        if (playerCity == null) {
            content = content.appendNewline().appendNewline()
                    .append(lm.getMessage("town-menu-join").clickEvent(ClickEvent.runCommand("/nubladatowns:town join " + town.getName())));
        } else if (playerCity.getUniqueId().equals(town.getUniqueId())) {
            content = content.appendNewline().appendNewline()
                    .append(lm.getMessage("town-menu-leave").clickEvent(ClickEvent.runCommand("/nubladatowns:town leave")));
        }

        player.openBook(Book.book(title, author, content));
    }

    public static void showResidentsList(Player player, Town town) {
        var residentNames = town.getResidents().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(Objects::nonNull)
                .map(OfflinePlayer::getName)
                .toList();

        var componentList = Component.text()
                .append(getBackButton("/nubladatowns:town menu " + town.getName()))
                .append(NubladaTowns.getInstance().getLocalizationManager().getMessage("resident-list"))
                .appendNewline()
                .appendNewline();

        for (String residentName : residentNames) {
            componentList = componentList
                    .append(Component.text("▪ ", NamedTextColor.GRAY))
                    .append(Component.text(residentName, NamedTextColor.DARK_GRAY)
                            .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit resident " + residentName))
                            .hoverEvent(HoverEvent.showText(lm.getMessage("click-to-edit")))
                    )
                    .appendNewline();
        }

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    public static void showResidentEditor(Player player, Town town, String targetName) {
        var componentList = Component.text()
                .append(getBackButton("/nubladatowns:town list " + town.getName()))
                .append(ComponentUtils.replaceString(lm.getMessage("resident-editor-title"), "%player%", targetName))
                .appendNewline()
                .appendNewline()
                .append(lm.getMessage("resident-editor-manage-roles")
                        .hoverEvent(HoverEvent.showText(lm.getMessage("click-to-edit")))
                        .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit resident " + targetName + " roles"))
                )
                .appendNewline()
                .appendNewline()
                .append(lm.getMessage("resident-editor-kick")
                        .hoverEvent(HoverEvent.showText(lm.getMessage("cannot-be-undone")))
                        .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit resident " + targetName + " kick"))
                )
                .appendNewline()
                .append(lm.getMessage("resident-editor-set-mayor")
                        .hoverEvent(HoverEvent.showText(lm.getMessage("cannot-be-undone")))
                        .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit mayor " + targetName))
                )
                .appendNewline();

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    public static void showResidentRoleEditor(Player player, Town town, OfflinePlayer target) {
        var componentList = Component.text()
                .append(getBackButton("/nubladatowns:town edit resident " + target.getName()))
                .append(ComponentUtils.replacePlayerName(lm.getMessage("resident-role-editor-title"), target.getName()))
                .appendNewline()
                .appendNewline();

        for (Role role : town.getRoles()) {
            componentList = componentList
                    .append(Component.text("▪ ", NamedTextColor.GRAY))
                    .append(getRoleWithColor(target, role))
                    .appendNewline();
        }

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    public static void showTownHistory(Player player, Town town, int page) {
        if (page < 0)
            return;

        var minEvent = page * 5;
        if (minEvent >= Permission.values().length)
            return;
        var maxEvent = Math.min(minEvent + 5, town.getHistoryEvents().size());

        var componentList = Component.text()
                .append(getBackButton("/nubladatowns:town menu " + town.getName()))
                .append(lm.getMessage("town-history-title"))
                .appendNewline();

        for (int i = minEvent; i < maxEvent; i++) {
            var event = town.getHistoryEvents().get(i);
            var eventName = PlainTextComponentSerializer.plainText().serialize(lm.getMessage("town-history-type-" + event.getType().name().toLowerCase().replace("_", "-")));
            var timestamp = HISTORY_EVENTS_TIMESTAMP_FORMAT.format(event.getTimestamp());
            var description = event.getDescription().isBlank() ? "" : "(" + event.getDescription() + ")";
            componentList = componentList.appendNewline().append(
                    ComponentUtils.replaceString(ComponentUtils.replaceString(ComponentUtils.replaceString(
                                    lm.getMessage("town-history-event-format"), "%timestamp%", timestamp),
                            "%type%",
                            eventName),
                            "%description%",
                            description)
            );
        }

        componentList = componentList.appendNewline();

        if (minEvent != 0) {
            componentList = componentList.append(getPreviousButton("/nubladatowns:town history " + (page - 1)));
        }

        if (maxEvent != town.getHistoryEvents().size()) {
            componentList = componentList.append(getNextButton("/nubladatowns:town history " + (page + 1)));
        }

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    private static Component getRoleWithColor(OfflinePlayer target, Role role) {
        var color = NamedTextColor.GRAY;
        var status = lm.getMessage("resident-role-editor-not-granted");
        var command = "/nubladatowns:town edit role " + role.getName() + " add " + target.getName();
        if (role.getPlayers().contains(target.getUniqueId())) {
            color = NamedTextColor.GREEN;
            status = lm.getMessage("resident-role-editor-granted");
            command = "/nubladatowns:town edit role " + role.getName() + " remove " + target.getName();
        }

        var component = Component.text()
                .append(Component.text(role.getName()))
                .hoverEvent(HoverEvent.showText(status
                        .appendNewline()
                        .append(lm.getMessage("click-to-edit"))
                ))
                .clickEvent(ClickEvent.runCommand(command))
                .color(color);

        return component.build();
    }

    public static void showRolesList(Player player, Town town) {
        var componentList = Component.text()
                .append(getBackButton("/nubladatowns:town menu " + town.getName()))
                .append(lm.getMessage("roles-list"))
                .hoverEvent(HoverEvent.showText(lm.getMessage("create-role-hint")))
                .appendNewline()
                .appendNewline();

        for (Role role : town.getRoles()) {
            componentList = componentList
                    .append(Component.text("▪ ", NamedTextColor.GRAY))
                    .append(Component.text(role.getName(), NamedTextColor.DARK_GRAY)
                            .hoverEvent(HoverEvent.showText(lm.getMessage("click-to-edit")))
                            .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit role " + role.getName()))
                    )
                    .appendNewline();
        }

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    public static void showRoleEditor(Player player, Town town, Role role) {
        showRoleEditor(player, town, role, 0);
    }

    public static void showRoleEditor(Player player, Town town, Role role, int page) {
        if (page < 0)
            return;

        var minPermission = page * 5;
        if (minPermission >= Permission.values().length)
            return;
        var maxPermission = Math.min(minPermission + 5, Permission.values().length);

        var componentList = Component.text()
                .append(getBackButton("/nubladatowns:town roles " + town.getName()))
                .append(ComponentUtils.replaceString(lm.getMessage("role-editor-title"), "%role%", role.getName()))
                .appendNewline()
                .append(lm.getMessage("role-editor-edit"))
                .appendNewline();

        for (int i = minPermission; i < maxPermission; i++) {
            var permission = Permission.values()[i];
            var permissionName = permission.name().toLowerCase().replace("_", "-");
            componentList = componentList.appendNewline().append(getPermissionWithColor("role-editor-edit-" + permissionName, permission, role, page));
        }

        componentList = componentList.appendNewline();

        var hasButtons = false;
        if (minPermission != 0) {
            componentList = componentList.append(getPreviousButton("/nubladatowns:town edit role " + role.getName() + " " + (page - 1)));
            hasButtons = true;
        }

        if (maxPermission != Permission.values().length) {
            componentList = componentList.append(getNextButton("/nubladatowns:town edit role " + role.getName() + " " + (page + 1)));
            hasButtons = true;
        }

        if (hasButtons) {
            componentList = componentList.appendNewline();
        }
        componentList = componentList.appendNewline().append(lm.getMessage("role-editor-delete")
                .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit role " + role.getName() + " delete"))
                .hoverEvent(HoverEvent.showText(lm.getMessage("cannot-be-undone"))))
                .appendNewline();

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    private static Component getPermissionWithColor(String message, Permission permission, Role role, int currentPage) {
        var color = NamedTextColor.GRAY;
        var status = lm.getMessage("role-editor-permission-not-granted");
        var command = "/nubladatowns:town edit role " + role.getName() + " grant " + permission.name() + " " + currentPage;
        if (role.getPermissions().contains(permission)) {
            color = NamedTextColor.GREEN;
            status = lm.getMessage("role-editor-permission-granted");
            command = "/nubladatowns:town edit role " + role.getName() + " revoke " + permission.name() + " " + currentPage;
        }

        var component = Component.text()
                .append(lm.getMessage(message))
                .hoverEvent(HoverEvent.showText(status))
                .clickEvent(ClickEvent.runCommand(command))
                .color(color);

        return component.build();
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

    private static Component getBackButton(String previousPageCommand) {
        return Component.text()
                .append(lm.getMessage("menu-back-button"))
                .clickEvent(ClickEvent.runCommand(previousPageCommand))
                .hoverEvent(HoverEvent.showText(lm.getMessage("menu-back-button-hover")))
                .build();
    }

    private static Component getPreviousButton(String command) {
        return Component.text()
                .append(lm.getMessage("menu-previous-button"))
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(lm.getMessage("menu-previous-button-hover")))
                .build();
    }

    private static Component getNextButton(String command) {
        return Component.text()
                .append(lm.getMessage("menu-next-button"))
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(lm.getMessage("menu-next-button-hover")))
                .build();
    }

}
