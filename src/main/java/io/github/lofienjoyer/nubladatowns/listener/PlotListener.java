package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.plot.PlotUtils;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
        if (!workingPlotSignHeader.equalsIgnoreCase(PlainTextComponentSerializer.plainText().serialize(firstLineComponent)))
            return;

        var plotName = PlainTextComponentSerializer.plainText().serialize(signSide.line(1));
        currentTown.getPlotByName(plotName).ifPresent(plot -> {
            event.setCancelled(true);
            var owner = Bukkit.getOfflinePlayer(plot.ownerUuid());
            var members = plot.members().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toList();
            var plotInfoComponent = localizationManager.getMessage("plot-information")
                    .replaceText(builder -> {
                        builder.matchLiteral("%name%").replacement(plot.name());
                    }).replaceText(builder -> {
                        builder.matchLiteral("%owner%").replacement(owner.getName());
                    }).replaceText(builder -> {
                        builder.matchLiteral("%members%").replacement(String.join(", ", members));
                    });
            event.getPlayer().sendMessage(plotInfoComponent);

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

    public Map<UUID, Location> getPlotsBeingCreated() {
        return plotsBeingCreated;
    }

}
