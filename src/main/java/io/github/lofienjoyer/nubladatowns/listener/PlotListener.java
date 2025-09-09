package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.plot.Plot;
import io.github.lofienjoyer.nubladatowns.plot.PlotUtils;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlotListener implements Listener {

    private final Map<UUID, Location> plotsBeingCreated;
    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public PlotListener(NubladaTowns instance) {
        this.plotsBeingCreated = new HashMap<>();
        this.localizationManager = instance.getLocalizationManager();
        this.townManager = instance.getTownManager();
    }

    @EventHandler
    public void onPlotCreate(PlayerInteractEvent event) {
        if (!event.hasItem() || !event.hasBlock())
            return;

        var item = event.getItem();
        if (item.getType() != Material.AMETHYST_SHARD)
            return;

        if (!item.hasItemMeta())
            return;

        var meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }

        var player = event.getPlayer();
        var plotName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        if (plotName.length() > 10) {
            player.sendMessage(localizationManager.getMessage("too-long", true));
            return;
        }
        if (!plotName.matches("^[a-zA-Z0-9 ]*$")) {
            player.sendMessage(localizationManager.getMessage("only-alphanumeric", true));
            return;
        }

        var playerTown = townManager.getPlayerTown(player);
        if (plotsBeingCreated.get(player.getUniqueId()) != null) {
            var success = handlePlotCreation(player, plotsBeingCreated.get(player.getUniqueId()), event.getClickedBlock().getLocation(), playerTown, plotName);
            plotsBeingCreated.remove(player.getUniqueId());
            if (success) {
                item.subtract();
            }
            return;
        }

        plotsBeingCreated.put(player.getUniqueId(), event.getClickedBlock().getLocation());
        player.sendMessage(localizationManager.getMessage("started-plot-creation"));
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        var firstLineComponent = event.line(0);
        if (firstLineComponent == null)
            return;

        var plotSignCreationHeader = PlainTextComponentSerializer.plainText().serialize(localizationManager.getMessage("plot-sign-creation-header"));
        if (!plotSignCreationHeader.equalsIgnoreCase(PlainTextComponentSerializer.plainText().serialize(firstLineComponent)))
            return;

        var plotNameComponent = event.line(1);
        if (plotNameComponent == null)
            return;

        var currentTown = townManager.getTownOnChunk(event.getBlock().getChunk());
        if (currentTown == null)
            return;

        var plot = currentTown.getPlotByName(PlainTextComponentSerializer.plainText().serialize(plotNameComponent));
        if (plot.isEmpty()) {
            event.line(0, localizationManager.getMessage("plot-sign-creation-header"));
            event.getPlayer().sendMessage(localizationManager.getMessage("invalid-plot-name", true));
            return;
        }

        event.line(0, localizationManager.getMessage("working-plot-sign-header"));
        event.getPlayer().sendMessage(localizationManager.getMessage("sign-linked", true));
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick())
            return;

        var block = event.getClickedBlock();
        if (block == null)
            return;

        if (!(block.getState() instanceof Sign sign))
            return;

        var currentTown = townManager.getTownOnChunk(block.getChunk());
        if (currentTown == null)
            return;

        var signSide = sign.getSide(Side.FRONT);
        var firstLineComponent = signSide.line(0);

        var workingPlotSignHeader = PlainTextComponentSerializer.plainText().serialize(localizationManager.getMessage("working-plot-sign-header"));
        if (!workingPlotSignHeader.equals(PlainTextComponentSerializer.plainText().serialize(firstLineComponent)))
            return;

        var plotName = PlainTextComponentSerializer.plainText().serialize(signSide.line(1));
        currentTown.getPlotByName(plotName).ifPresent(plot -> {
            event.setCancelled(true);
            var itemInHand = event.getItem();
            if (itemInHand != null) {
                var player = event.getPlayer();
                if (itemInHand.getType() == Material.BOOK) {
                    handlePlotContract(player, currentTown, plot, itemInHand);
                    return;
                } else if (itemInHand.getType() == Material.PAPER) {
                    handlePlotInvite(player, currentTown, plot, itemInHand);
                    return;
                }
            }

            PlotUtils.showPlotMenu(event.getPlayer(), plot);

            var world = plot.world();
            ParticleUtils.showPlot(plot.min().toLocation(world), plot.max().toLocation(world), 0.5f, Color.fromARGB(plot.argbColor()), 10);
        });
    }

    private boolean handlePlotCreation(Player player, Location posA, Location posB, Town town, String plotName) {
        if (town.doesPlotExist(plotName)) {
            player.sendMessage("Plot already exists");
            return false;
        }

        var plot = PlotUtils.createPlotBetween(posA, posB, player.getUniqueId(), plotName);
        if (!PlotUtils.isPlotInsideTown(plot, town)) {
            player.sendMessage(localizationManager.getMessage("plot-outside-town"));
            return false;
        }

        var intersectsWithExistingPlot = town.getPlots().stream().anyMatch(existingPlot -> {
            return PlotUtils.doPlotsIntersect(existingPlot, plot);
        });
        if (intersectsWithExistingPlot) {
            player.sendMessage(localizationManager.getMessage("intersects-with-existing-plot"));
            return false;
        }

        townManager.addPlotToTown(plot, town);
        player.sendMessage(localizationManager.getMessage("plot-created"));
        return true;
    }

    private void handlePlotContract(Player player, Town town, Plot plot, ItemStack itemInHand) {
        if (itemInHand.getPersistentDataContainer().has(NubladaTowns.Keys.PLOT_CONTRACT_KEY)) {
            var plotUuidString = itemInHand.getPersistentDataContainer().get(NubladaTowns.Keys.PLOT_CONTRACT_KEY, PersistentDataType.STRING);
            var plotUuid = UUID.fromString(plotUuidString);
            if (!plotUuid.equals(plot.uuid()))
                return;

            plot.setOwnerUuid(player.getUniqueId());
            player.sendMessage(localizationManager.getMessage("plot-claimed").replaceText(builder -> {
                builder.matchLiteral("%plot%").replacement(Component.text(plot.name(), Style.style(TextColor.color(plot.argbColor()))));
            }));
            itemInHand.subtract();
            return;
        }

        if (!town.hasPermission(player, Permission.MANAGE_PLOTS) && !plot.ownerUuid().equals(player.getUniqueId())) {
            player.sendMessage(localizationManager.getMessage("no-permission"));
            return;
        }

        var levelsNeeded = NubladaTowns.getInstance().getConfigValues().getTownInviteXpLevels();
        if (player.getLevel() < levelsNeeded) {
            player.sendMessage(localizationManager.getMessage("plot-invite-not-enough-xp").replaceText(builder -> {
                builder.matchLiteral("%levels%").replacement(String.valueOf(levelsNeeded));
            }));
            return;
        }

        player.setLevel(player.getLevel() - levelsNeeded);
        itemInHand.subtract();

        var inviteItem = new ItemStack(Material.BOOK);
        var itemMeta = inviteItem.getItemMeta();
        itemMeta.itemName(localizationManager.getMessage("plot-contract-book-name"));
        itemMeta.lore(List.of(ComponentUtils.replaceString(localizationManager.getMessage("plot-contract-book-description"), "%plot%", plot.name())));
        itemMeta.setMaxStackSize(1);
        itemMeta.setEnchantmentGlintOverride(true);
        var pdc = itemMeta.getPersistentDataContainer();
        pdc.set(NubladaTowns.Keys.PLOT_CONTRACT_KEY, PersistentDataType.STRING, plot.uuid().toString());
        inviteItem.setItemMeta(itemMeta);
        player.getInventory().addItem(inviteItem);
        player.sendMessage(localizationManager.getMessage("plot-contract-created"));
    }

    private void handlePlotInvite(Player player, Town town, Plot plot, ItemStack itemInHand) {
        if (itemInHand.getPersistentDataContainer().has(NubladaTowns.Keys.PLOT_INVITE_KEY)) {
            var plotUuidString = itemInHand.getPersistentDataContainer().get(NubladaTowns.Keys.PLOT_INVITE_KEY, PersistentDataType.STRING);
            var plotUuid = UUID.fromString(plotUuidString);
            if (!plotUuid.equals(plot.uuid()))
                return;

            if (plot.ownerUuid().equals(player.getUniqueId()) || plot.members().contains(player.getUniqueId())) {
                player.sendMessage(localizationManager.getMessage("plot-already-member"));
                return;
            }

            plot.members().add(player.getUniqueId());
            player.sendMessage(localizationManager.getMessage("plot-joined").replaceText(builder -> {
                builder.matchLiteral("%plot%").replacement(Component.text(plot.name(), Style.style(TextColor.color(plot.argbColor()))));
            }));
            itemInHand.subtract();
            return;
        }

        if (!town.hasPermission(player, Permission.MANAGE_PLOTS) && !plot.ownerUuid().equals(player.getUniqueId())) {
            player.sendMessage(localizationManager.getMessage("no-permission"));
            return;
        }

        var levelsNeeded = NubladaTowns.getInstance().getConfigValues().getTownInviteXpLevels();
        if (player.getLevel() < levelsNeeded) {
            player.sendMessage(localizationManager.getMessage("plot-invite-not-enough-xp").replaceText(builder -> {
                builder.matchLiteral("%levels%").replacement(String.valueOf(levelsNeeded));
            }));
            return;
        }

        player.setLevel(player.getLevel() - levelsNeeded);
        itemInHand.subtract();

        var inviteItem = new ItemStack(Material.PAPER);
        var itemMeta = inviteItem.getItemMeta();
        itemMeta.itemName(localizationManager.getMessage("plot-invite-book-name"));
        itemMeta.lore(List.of(ComponentUtils.replaceString(localizationManager.getMessage("plot-invite-book-description"), "%plot%", plot.name())));
        itemMeta.setMaxStackSize(1);
        itemMeta.setEnchantmentGlintOverride(true);
        var pdc = itemMeta.getPersistentDataContainer();
        pdc.set(NubladaTowns.Keys.PLOT_INVITE_KEY, PersistentDataType.STRING, plot.uuid().toString());
        inviteItem.setItemMeta(itemMeta);
        player.getInventory().addItem(inviteItem);
        player.sendMessage(localizationManager.getMessage("plot-invite-created"));
    }

    public Map<UUID, Location> getPlotsBeingCreated() {
        return plotsBeingCreated;
    }

}
