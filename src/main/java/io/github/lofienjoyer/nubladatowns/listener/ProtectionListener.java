package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ProtectionListener implements Listener {

    private final TownManager townManager;
    private final LocalizationManager localizationManager;

    public ProtectionListener(TownManager townManager) {
        this.townManager = townManager;
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();
        if (player.hasPermission("nubladatowns.admin"))
            return;

        var currentTown = townManager.getTownOnChunk(event.getBlock().getChunk());
        if (currentTown == null)
            return;

        if (!currentTown.getResidents().contains(player.getUniqueId())) {
            player.sendMessage(localizationManager.getMessage("cannot-break-here"));
            event.setCancelled(true);
        }
    }

}
