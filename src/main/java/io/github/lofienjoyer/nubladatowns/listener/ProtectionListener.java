package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Objects;

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

        if (!currentTown.hasPermission(player, Permission.DESTROY)) {
            player.sendActionBar(localizationManager.getMessage("cannot-break-here"));
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

        if (!currentTown.hasPermission(player, Permission.BUILD)) {
            player.sendActionBar(localizationManager.getMessage("cannot-place-here"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        var player = event.getPlayer();
        if (player.hasPermission("nubladatowns.admin"))
            return;

        var block = event.getClickedBlock();
        var blockState = block.getState();
        var blockData = block.getBlockData();

        if (blockState instanceof Container || blockData instanceof Openable) {
            var currentTown = townManager.getTownOnChunk(block.getChunk());
            if (currentTown != null && !currentTown.hasPermission(player, Permission.INTERACT)) {
                event.setCancelled(true);
                player.sendActionBar(localizationManager.getMessage("cannot-interact-here"));
            }
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        var pistonTown = townManager.getTownOnChunk(event.getBlock().getChunk());

        for (Block movedBlock : event.getBlocks()) {
            Location location = movedBlock.getLocation().add(event.getDirection().getDirection());
            var movedBlockTown = townManager.getTownOnChunk(location.getChunk());

            if (movedBlockTown == null || movedBlockTown.equals(pistonTown))
                return;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        var pistonTown = townManager.getTownOnChunk(event.getBlock().getChunk());

        for (Block movedBlock : event.getBlocks()) {
            var movedBlockTown = townManager.getTownOnChunk(movedBlock.getChunk());
            if (movedBlockTown == null || movedBlockTown.equals(pistonTown))
                return;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager))
            return;
        if (!(event.getEntity() instanceof Animals))
            return;
        if (damager.hasPermission("nubladatowns.admin"))
            return;

        var eventTown = townManager.getTownOnChunk(event.getEntity().getChunk());
        if (eventTown == null || Objects.equals(townManager.getPlayerTown(damager), eventTown))
            return;

        damager.sendActionBar(localizationManager.getMessage("cannot-attack-animals-here"));
        event.setCancelled(true);
    }

    private void handleExplosion(List<Block> blocks) {
        var iterator = blocks.iterator();
        while (iterator.hasNext()) {
            var block = iterator.next();
            var currentTown = townManager.getTownOnChunk(block.getChunk());
            if (currentTown != null)
                iterator.remove();
        }
    }

}
