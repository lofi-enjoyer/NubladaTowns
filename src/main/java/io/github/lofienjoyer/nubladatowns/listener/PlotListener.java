package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.plot.PlotUtils;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
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

        var player = event.getPlayer();
        var playerTown = townManager.getPlayerTown(player);
        if (plotsBeingCreated.get(player.getUniqueId()) != null) {
            handlePlotCreation(player, plotsBeingCreated.get(player.getUniqueId()), event.getClickedBlock().getLocation(), playerTown);
            plotsBeingCreated.remove(player.getUniqueId());
            return;
        }

        plotsBeingCreated.put(player.getUniqueId(), event.getClickedBlock().getLocation());
        player.sendMessage(localizationManager.getMessage("started-plot-creation"));
    }

    private void handlePlotCreation(Player player, Location posA, Location posB, Town town) {
        // TODO: check if overlaps with another plot
        player.sendMessage(localizationManager.getMessage("stopped-plot-creation"));
        var plot = PlotUtils.getPlotBetween(posA, posB);
        if (PlotUtils.isPlotInsideTown(posA, posB, town)) {
            townManager.addPlotToTown(plot, town);
        } else {
            player.sendMessage(localizationManager.getMessage("plot-outside-town"));
        }
    }

    public Map<UUID, Location> getPlotsBeingCreated() {
        return plotsBeingCreated;
    }

}
