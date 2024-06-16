package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.GameMode;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        if (player.hasPermission("nubladatowns.admin"))
            return;

        var currentTown = townManager.getTownOnChunk(event.getBlock().getChunk());
        if (currentTown == null)
            return;

        if (!currentTown.getResidents().contains(player.getUniqueId())) {
            player.sendMessage(localizationManager.getMessage("cannot-place-here"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        var currentTown = townManager.getTownOnChunk(event.getBlock().getChunk());
        if (currentTown != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        var block = event.getClickedBlock();
        var playerUuid = event.getPlayer().getUniqueId();
        var blockState = block.getState();
        var blockData = block.getBlockData();

        if (blockState instanceof Container || blockData instanceof Openable) {
            var currentTown = townManager.getTownOnChunk(block.getChunk());
            if (currentTown == null || !currentTown.getResidents().contains(playerUuid)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(localizationManager.getMessage("cannot-place-here"));
            }
        }
    }

}
