package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.power.PowerManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import io.github.lofienjoyer.nubladatowns.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class TownListener implements Listener {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;
    private final PowerManager powerManager;

    public TownListener(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();;
        this.powerManager = NubladaTowns.getInstance().getPowerManager();
        this.townManager = townManager;
    }

    @EventHandler
    public void onBannerPlace(BlockPlaceEvent event) {
        if (!event.getItemInHand().getType().toString().contains("BANNER"))
            return;

        if (!event.getItemInHand().hasItemMeta())
            return;

        var meta = event.getItemInHand().getItemMeta();

        var player = event.getPlayer();
        if (!meta.hasDisplayName()) {
            return;
        }

        var townName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));
        var playerTown = townManager.getPlayerTown(player);

        if (playerTown != null) {
            if (!playerTown.getName().equals(townName)) {
                player.sendMessage(localizationManager.getMessage("cannot-claim-for-other-town", true));
                return;
            }
            if (!claimChunk(player, playerTown, event.getBlock().getLocation())) {
                player.playSound(player, Sound.ITEM_WOLF_ARMOR_CRACK, 1, 1.25f);
                return;
            }
            event.getBlock().setType(Material.AIR);
        } else {
            var townColor = ((Banner) event.getBlock().getState()).getBaseColor().getColor().asARGB();
            createTown(player, event.getBlock().getLocation(), townName, townColor);
        }
    }

    private boolean createTown(Player player, Location location, String townName, int color) {
        var currentTown = townManager.getTownOnChunk(location.getChunk());
        if (currentTown != null) {
            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("land-already-of", true), currentTown));
            return false;
        }

        var townWithSameName = townManager.getTownByName(townName);
        if (townWithSameName != null) {
            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-already-exists", true), townWithSameName));
            return false;
        }

        townManager.createTown(townName, location, player, color);

        Bukkit.broadcast(ComponentUtils.replacePlayerName(
                ComponentUtils.replaceTownName(localizationManager.getMessage("player-founded-town", true), townName, color),
                player.getName())
        );

        ParticleUtils.showChunkBorders(location.getChunk(), Particle.HAPPY_VILLAGER, player.getLocation().getY(), 40);
        SoundUtils.playAscendingSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.25f, 4);

        var block = location.getBlock();
        block.setType(Material.LECTERN);
        var rotatable = (Directional) block.getBlockData();
        rotatable.setFacing(player.getFacing().getOppositeFace());
        block.setBlockData(rotatable);
        return true;
    }

    private boolean claimChunk(Player player, Town town, Location location) {
        var chunk = location.getChunk();

        if (!TownUtils.checkNeighborChunks(chunk, town, townManager)) {
            player.sendMessage(localizationManager.getMessage("land-not-connected", true));
            return false;
        }

        if (town.getPower() < powerManager.getAmount("claim-land")) {
            player.sendMessage(localizationManager.getMessage("not-enough-power", true));
            return false;
        }

        var currentTown = townManager.claimChunk(chunk, town);
        if (currentTown != null) {
            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("land-already-of", true), currentTown));
            return false;
        }

        player.sendMessage(localizationManager.getMessage("land-claimed-successfully", true));
        Bukkit.getOnlinePlayers().forEach(resident -> {
            resident.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-claimed-land", true), town));
        });

        town.setPower(town.getPower() - powerManager.getAmount("claim-land"));

        ParticleUtils.showChunkBorders(location.getChunk(), Particle.GLOW, player.getLocation().getY(), 20);
        SoundUtils.playAscendingSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.25f, 2);
        return true;
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        var block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN)
            return;

        var town = townManager.getTownOnChunk(event.getClickedBlock().getChunk());
        if (town == null || !town.getSpawn().equals(event.getClickedBlock().getLocation()))
            return;

        event.setCancelled(true);
        TownUtils.showTownMenu(event.getPlayer(), town);
    }

    @EventHandler
    public void onChunkChange(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk()))
            return;

        var previousTown = townManager.getTownOnChunk(event.getFrom().getChunk());
        var currentTown = townManager.getTownOnChunk(event.getTo().getChunk());
        if (previousTown == currentTown) {
            return;
        }

        if (!event.getPlayer().getInventory().contains(Material.COMPASS))
            return;

        if (currentTown != null) {
            event.getPlayer().sendActionBar(ComponentUtils.replaceTownName(localizationManager.getMessage("entering-town"), currentTown));
        } else {
            event.getPlayer().sendActionBar(ComponentUtils.replaceTownName(localizationManager.getMessage("exiting-town"), previousTown));
        }
    }

}
