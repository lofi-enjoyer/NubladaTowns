package io.github.lofienjoyer.nubladatowns.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
                        .appendNewline()
                        .append(lm.getMessage("town-menu-roles-list").clickEvent(ClickEvent.runCommand("/nubladatowns:town roles " + town.getName())))
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

    public static void showRolesList(Player player, Town town) {
        var componentList = Component.text()
                .append(NubladaTowns.getInstance().getLocalizationManager().getMessage("roles-list"))
                .appendNewline()
                .appendNewline();

        for (Role role : town.getRoles()) {
            componentList = componentList
                    .append(Component.text("▪ ", NamedTextColor.GRAY))
                    .append(Component.text(role.getName(), NamedTextColor.DARK_GRAY)
                            .clickEvent(ClickEvent.runCommand("/nubladatowns:town edit role " + role.getName()))
                    )
                    .appendNewline();
        }

        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    public static void showRoleEditor(Player player, Town town, Role role) {
        var lm = NubladaTowns.getInstance().getLocalizationManager();

        var componentList = Component.text()
                .append(ComponentUtils.replaceString(lm.getMessage("role-editor-title"), "%role%", role.getName()))
                .appendNewline()
                .appendNewline()
                .append(lm.getMessage("role-editor-edit"))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-build", Permission.BUILD, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-destroy", Permission.DESTROY, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-interact", Permission.INTERACT, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-invite", Permission.INVITE, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-kick", Permission.KICK, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-rename", Permission.RENAME, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-change-spawn", Permission.CHANGE_SPAWN, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-manage-roles", Permission.MANAGE_ROLES, town, role))
                .appendNewline()
                .append(getPermissionWithColor("role-editor-edit-assign-roles", Permission.ASSIGN_ROLES, town, role))
                .appendNewline()
                .appendNewline()
                .append(lm.getMessage("role-editor-delete").clickEvent(ClickEvent.runCommand("/nubladatowns:town edit role " + role.getName() + " delete")))
                .appendNewline();

        // grant permissions
        // revoke permissions
        // remove role
        var title = Component.text("Town menu");
        var author = Component.text("NubladaTowns");
        player.openBook(Book.book(title, author, componentList.build()));
    }

    private static Component getPermissionWithColor(String message, Permission permission, Town town, Role role) {
        var lm = NubladaTowns.getInstance().getLocalizationManager();

        var color = NamedTextColor.GOLD;
        var status = lm.getMessage("role-editor-permission-not-granted");
        var command = "/nubladatowns:town edit role " + role.getName() + " grant " + permission.name();
        if (role.getPermissions().contains(permission)) {
            color = NamedTextColor.GREEN;
            status = lm.getMessage("role-editor-permission-granted");
            command = "/nubladatowns:town edit role " + role.getName() + " revoke " + permission.name();
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

}
