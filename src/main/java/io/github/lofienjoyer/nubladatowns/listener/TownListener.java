package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.power.PowerManager;
import io.github.lofienjoyer.nubladatowns.roles.Permission;
import io.github.lofienjoyer.nubladatowns.roles.Role;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.town.TownUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import io.github.lofienjoyer.nubladatowns.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class TownListener implements Listener {

    private final LocalizationManager localizationManager;
    private final TownManager townManager;

    public TownListener(TownManager townManager) {
        this.localizationManager = NubladaTowns.getInstance().getLocalizationManager();
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

        var state = (Banner) event.getBlock().getState();
        if (playerTown != null) {
            if (!playerTown.getName().equals(townName)) {
                player.sendActionBar(localizationManager.getMessage("cannot-claim-for-other-town"));
                return;
            }
            if (!playerTown.hasPermission(player, Permission.CLAIM_TERRITORY)) {
                player.sendActionBar(localizationManager.getMessage("no-permission"));
                return;
            }
            if (!claimChunk(player, playerTown, event.getBlock().getLocation(), state)) {
                player.playSound(player, Sound.ITEM_WOLF_ARMOR_CRACK, 1, 1.25f);
                return;
            }
            event.getBlock().setType(Material.AIR);
        } else {
            createTown(player, event.getBlock().getLocation(), townName, state);
        }
    }

    @EventHandler
    public void onUnclaim(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() != Material.TNT)
            return;

        if (!event.getItemInHand().hasItemMeta())
            return;

        var meta = event.getItemInHand().getItemMeta();

        var player = event.getPlayer();
        if (!meta.hasDisplayName()) {
            return;
        }

        var townName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));
        var tntTown = townManager.getTownByName(townName);
        if (tntTown == null)
            return;

        var playerTown = townManager.getPlayerTown(player);

        if (playerTown == null) {
            player.sendMessage(localizationManager.getMessage("not-in-a-town", true));
            return;
        }

        if (!playerTown.hasPermission(player, Permission.CLAIM_TERRITORY)) {
            player.sendActionBar(localizationManager.getMessage("no-permission"));
            return;
        }

        if (!playerTown.getUniqueId().equals(tntTown.getUniqueId())) {
            player.sendActionBar(localizationManager.getMessage("cannot-abandon-for-other-town"));
            return;
        }

        if (!playerTown.getUniqueId().equals(townManager.getTownOnChunk(event.getBlock().getChunk()).getUniqueId())) {
            player.sendActionBar(localizationManager.getMessage("land-not-claimed-yet"));
            return;
        }

        if (playerTown.getSpawn().getChunk().equals(event.getBlock().getChunk())) {
            player.sendActionBar(localizationManager.getMessage("move-lectern-before-abandoning"));
            return;
        }

        townManager.abandonChunk(event.getBlock().getChunk());
        event.setCancelled(true);
        event.getItemInHand().subtract();

        var tnt = (TNTPrimed) event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().toCenterLocation(), EntityType.TNT);
        tnt.setYield((float) NubladaTowns.getInstance().getConfigValues().getTownAbandonTntYield());
        tnt.setIsIncendiary(NubladaTowns.getInstance().getConfigValues().isTownAbandonTntFire());
        tnt.setFuseTicks(NubladaTowns.getInstance().getConfigValues().getTownAbandonTntFuseTicks());

        player.sendMessage(localizationManager.getMessage("land-abandoned-successfully"));
        Bukkit.broadcast(ComponentUtils.replaceTownName(localizationManager.getMessage("town-abandoned-land", true), playerTown));
        var returnedPower = NubladaTowns.getInstance().getConfigValues().getClaimLandPower() * (int) (NubladaTowns.getInstance().getConfigValues().getTownAbandonPowerReturnPercentage() / 100f);
        playerTown.setPower(playerTown.getPower() + returnedPower);
    }

    @EventHandler
    public void onLecternPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() != Material.LECTERN)
            return;

        if (!event.getItemInHand().hasItemMeta())
            return;

        var meta = event.getItemInHand().getItemMeta();

        var player = event.getPlayer();
        if (!meta.hasDisplayName()) {
            return;
        }

        var townName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));
        var lecternTown = townManager.getTownByName(townName);
        if (lecternTown == null)
            return;

        var playerTown = townManager.getPlayerTown(player);

        if (playerTown == null) {
            player.sendMessage(localizationManager.getMessage("not-in-a-town", true));
            return;
        }

        if (!playerTown.getUniqueId().equals(lecternTown.getUniqueId())) {
            player.sendActionBar(localizationManager.getMessage("cannot-move-other-town-lectern"));
            return;
        }

        if (!playerTown.getUniqueId().equals(townManager.getTownOnChunk(event.getBlock().getChunk()).getUniqueId())) {
            player.sendActionBar(localizationManager.getMessage("land-not-claimed-yet"));
            return;
        }

        if (!playerTown.hasPermission(player, Permission.CHANGE_SPAWN)) {
            player.sendActionBar(localizationManager.getMessage("no-permission"));
            return;
        }

        playerTown.getSpawn().getBlock().setType(Material.AIR);
        var location = event.getBlock().getLocation();
        playerTown.setSpawn(location);
        player.sendMessage(localizationManager.getMessage("lectern-moved"));
        location.getWorld().playSound(location, Sound.ITEM_LODESTONE_COMPASS_LOCK, 1, 0.75f);
    }

    private boolean createTown(Player player, Location location, String townName, Banner banner) {
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

        var patterns = banner.getPatterns();
        if (patterns.isEmpty()) {
            player.sendActionBar(localizationManager.getMessage("invalid-banner", true));
            return false;
        }

        var color = banner.getBaseColor().getColor().asARGB();
        townManager.createTown(townName, location, player, color, banner.getPatterns());

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

    private boolean claimChunk(Player player, Town town, Location location, Banner banner) {
        var chunk = location.getChunk();

        var currentTown = townManager.getTownOnChunk(chunk);
        if (currentTown != null) {
            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("land-already-of", true), currentTown));
            return false;
        }

        if (!TownUtils.checkNeighborChunks(chunk, town, townManager)) {
            player.sendMessage(localizationManager.getMessage("land-not-connected", true));
            return false;
        }

        if (town.getPower() < NubladaTowns.getInstance().getConfigValues().getClaimLandPower()) {
            player.sendMessage(localizationManager.getMessage("not-enough-power", true));
            return false;
        }

        if (town.getRgbColor() != banner.getBaseColor().getColor().asARGB() || !town.getBannerPatterns().equals(banner.getPatterns())) {
            player.sendMessage(localizationManager.getMessage("not-same-banner", true));
            return false;
        }

        townManager.claimChunk(chunk, town);

        player.sendMessage(localizationManager.getMessage("land-claimed-successfully", true));
        Bukkit.getOnlinePlayers().forEach(resident -> {
            resident.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("town-claimed-land", true), town));
        });

        town.setPower(town.getPower() - NubladaTowns.getInstance().getConfigValues().getClaimLandPower());

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

        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();

        if(item.getType() == Material.PAPER && item.hasItemMeta()) {
            if(!town.hasPermission(player, Permission.MANAGE_ROLES)) {
                player.sendMessage(localizationManager.getMessage("no-permission"));
                return;
            }

            var roleName = Objects.requireNonNull((TextComponent)item.getItemMeta().displayName()).content();
            var regexPattern = Pattern.compile("^[a-zA-Z0-9]*$");
            if (!regexPattern.matcher(roleName).find()) {
                player.sendMessage(localizationManager.getMessage("only-alphanumeric", true));
                return;
            }

            if(roleName.length() > 16) {
                player.sendMessage(localizationManager.getMessage("role-name-too-long", true));
                return;
            }

            if(town.getRole(roleName) != null) {
                player.sendMessage(localizationManager.getMessage("role-already-exists", true));
                return;
            }

            if (town.getRoles().size() >= NubladaTowns.getInstance().getConfigValues().getMaxRolesPerTown()) {
                player.sendMessage(localizationManager.getMessage("role-limit-exceeded"));
                return;
            }

            town.addRole(new Role(roleName));
            player.playSound(player, Sound.ITEM_BOOK_PUT, 1, 1.25f);
            item.setAmount(item.getAmount() - 1);
            player.sendMessage(ComponentUtils.replaceString(localizationManager.getMessage("role-created", true), "%role%", roleName));
            return;
        }

        if (item.getType() == Material.BOOK && (!item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(NubladaTowns.Keys.TOWN_INVITE_KEY))) {
            if (!town.hasPermission(player, Permission.INVITE)) {
                player.sendMessage(localizationManager.getMessage("no-permission"));
                return;
            }

            var levelsNeeded = NubladaTowns.getInstance().getConfigValues().getTownInviteXpLevels();
            if (player.getLevel() < levelsNeeded) {
                player.sendMessage(localizationManager.getMessage("town-invite-not-enough-xp"));
                return;
            }

            player.setLevel(player.getLevel() - levelsNeeded);
            item.subtract();

            var inviteItem = new ItemStack(Material.BOOK);
            var itemMeta = inviteItem.getItemMeta();
            itemMeta.itemName(localizationManager.getMessage("town-invite-book-name"));
            itemMeta.lore(List.of(ComponentUtils.replaceTownName(localizationManager.getMessage("town-invite-book-description"), town)));
            itemMeta.setMaxStackSize(1);
            itemMeta.setEnchantmentGlintOverride(true);
            var pdc = itemMeta.getPersistentDataContainer();
            pdc.set(NubladaTowns.Keys.TOWN_INVITE_KEY, PersistentDataType.STRING, town.getUniqueId().toString());
            inviteItem.setItemMeta(itemMeta);
            player.getInventory().addItem(inviteItem);
            player.sendMessage(localizationManager.getMessage("town-invite-created"));
            return;
        }

        if (item.getType() == Material.NAME_TAG && item.hasItemMeta()) {
            var itemMeta = item.getItemMeta();
            if (!itemMeta.hasDisplayName())
                return;

            if (!town.hasPermission(player, Permission.RENAME)) {
                player.sendMessage(localizationManager.getMessage("no-permission"));
                return;
            }

            var townName = PlainTextComponentSerializer.plainText().serialize(itemMeta.displayName());
            var regexPattern = Pattern.compile("^[a-zA-Z0-9 ]*$");
            if (!regexPattern.matcher(townName).find()) {
                player.sendMessage(localizationManager.getMessage("only-alphanumeric", true));
                return;
            }

            var oldName = town.getName();
            item.subtract();
            town.setName(townName);

            player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("name-changed"), town));
            Bukkit.getOnlinePlayers().forEach(resident -> {
                resident.sendMessage(ComponentUtils.replaceString(
                        ComponentUtils.replaceTownName(localizationManager.getMessage("town-name-changed", true), town),
                        "%old-name%",
                        oldName
                ));
            });
            return;
        }

        player.performCommand("nubladatowns:town menu " + town.getName());
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
