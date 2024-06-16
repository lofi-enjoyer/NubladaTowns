package io.github.lofienjoyer.nubladatowns.command.town;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.BiConsumer;

public class EditSubcommand implements BiConsumer<CommandSender, String[]> {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public EditSubcommand(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
        this.townManager = townManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        Town town = townManager.getPlayerTown(player);
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("not-in-a-town"));
            return;
        }

        if (player.getLocation().distanceSquared(town.getSpawn()) > 5 * 5) {
            player.sendMessage(localizationManager.getMessage("too-far-from-lectern"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        if (args[0].equals("role")) {
            if (args.length < 2) {
                player.sendMessage(localizationManager.getMessage("not-enough-arguments"));
                return;
            }

            Role role = town.getRole(args[1]);
            if (role == null) {
                player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("non-existent-role"), "%role%", args[1]));
                return;
            }

            if (args.length == 2) {
                TownUtils.showRoleEditor(player, town, role);
            } else if (args.length == 3 && args[2].equals("delete")) {
                if (!town.hasPermission(player, Permission.MANAGE_ROLES)) {
                    player.sendMessage(localizationManager.getMessage("no-permission"));
                    return;
                }

                player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("role-deleted", true), "%role%", role.getName()));
                town.removeRole(role);
            } else if (args.length == 4) {
                switch (args[2]) {
                    case "grant" -> {
                        if (!town.hasPermission(player, Permission.MANAGE_ROLES)) {
                            player.sendMessage(localizationManager.getMessage("no-permission"));
                            return;
                        }

                        if (!Permission.contains(args[3])) {
                            player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("invalid-value"), "%value%", args[3]));
                            return;
                        }

                        var permission = Permission.valueOf(args[3]);

                        if (permission.equals(Permission.ASSIGN_ROLES) && !town.getMayor().equals(player.getUniqueId())) {
                            player.sendMessage(localizationManager.getMessage("no-permission"));
                            return;
                        }

                        if (role.getPermissions().contains(permission)) {
                            player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("role-already-has-permission"), "%role%", role.getName()));
                            return;
                        }

                        role.addPermission(permission);
                        TownUtils.showRoleEditor(player, town, role);
                    }
                    case "revoke" -> {
                        if (!town.hasPermission(player, Permission.MANAGE_ROLES)) {
                            player.sendMessage(localizationManager.getMessage("no-permission"));
                            return;
                        }

                        if (!Permission.contains(args[3])) {
                            player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("invalid-value"), "%value%", args[3]));
                            return;
                        }

                        var permission = Permission.valueOf(args[3]);

                        if (permission.equals(Permission.ASSIGN_ROLES) && !town.getMayor().equals(player.getUniqueId())) {
                            player.sendMessage(localizationManager.getMessage("no-permission"));
                            return;
                        }

                        if (!role.getPermissions().contains(permission)) {
                            player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("role-does-not-have-permission"), "%role%", role.getName()));
                            return;
                        }

                        role.removePermission(permission);
                        TownUtils.showRoleEditor(player, town, role);
                    }
                    case "add" -> {
                        if (!town.hasPermission(player, Permission.ASSIGN_ROLES)) {
                            player.sendMessage(localizationManager.getMessage("no-permission"));
                            return;
                        }

                        var target = Bukkit.getOfflinePlayer(args[3]);
                        if (!town.getResidents().contains(target.getUniqueId())) {
                            player.sendMessage(localizationManager.getMessage("other-not-in-this-town"));
                            return;
                        }

                        if (role.getPlayers().contains(target.getUniqueId())) {
                            player.sendMessage(ComponentUtils.replacePlayerName(localizationManager.getMessage("already-has-role"), target.getName()));
                            return;
                        }

                        role.addPlayer(target.getUniqueId());
                        var replace = Map.of("%player%", player.getName(), "%role%", role.getName());
                        player.sendMessage(ComponentUtils.replaceStrings(localizationManager.getMessage("player-added-to-role", true), replace));
                    }
                    case "remove" -> {
                        if (!town.hasPermission(player, Permission.ASSIGN_ROLES)) {
                            player.sendMessage(localizationManager.getMessage("no-permission"));
                            return;
                        }

                        var target = Bukkit.getOfflinePlayer(args[3]);
                        if (!town.getResidents().contains(target.getUniqueId())) {
                            player.sendMessage(localizationManager.getMessage("other-not-in-this-town"));
                            return;
                        }

                        if (!role.getPlayers().contains(target.getUniqueId())) {
                            player.sendMessage(ComponentUtils.replacePlayerName(localizationManager.getMessage("does-not-have-role"), target.getName()));
                            return;
                        }

                        role.removePlayer(target.getUniqueId());
                        var replace = Map.of("%player%", player.getName(), "%role%", role.getName());
                        player.sendMessage(ComponentUtils.replaceStrings(localizationManager.getMessage("player-removed-from-role", true), replace));
                    }
                }
            } else {
                player.sendMessage(localizationManager.getMessage("invalid-command"));
            }
        }
    }

}
