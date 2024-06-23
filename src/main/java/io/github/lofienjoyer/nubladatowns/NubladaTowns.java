package io.github.lofienjoyer.nubladatowns;

import io.github.lofienjoyer.nubladatowns.command.AdminCommand;
import io.github.lofienjoyer.nubladatowns.command.TownCommand;
import io.github.lofienjoyer.nubladatowns.listener.PlotListener;
import io.github.lofienjoyer.nubladatowns.listener.PowerListener;
import io.github.lofienjoyer.nubladatowns.listener.ProtectionListener;
import io.github.lofienjoyer.nubladatowns.listener.TownListener;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.plot.PlotUtils;
import io.github.lofienjoyer.nubladatowns.power.PowerManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;

public final class NubladaTowns extends JavaPlugin {

    private static NubladaTowns INSTANCE;

    private LocalizationManager localizationManager;
    private PowerManager powerManager;
    private TownManager townManager;
    private BukkitTask townBordersTask;
    private PlotListener plotListener;
    private BukkitTask plotCreationTask;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.localizationManager = new LocalizationManager();
        this.powerManager = new PowerManager();

        this.townManager = new TownManager(this);
        loadData();

        getCommand("town").setExecutor(new TownCommand(townManager));
        getCommand("nubladatownsadmin").setExecutor(new AdminCommand());

        getServer().getPluginManager().registerEvents(new TownListener(townManager), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(townManager), this);
        getServer().getPluginManager().registerEvents(new PowerListener(townManager), this);
        this.plotListener = new PlotListener(this);
        getServer().getPluginManager().registerEvents(plotListener, this);

        setupTownBordersTimer();
        setupPlotCreationTimer();
    }

    @Override
    public void onDisable() {
        stopTownBordersTimer();
        try {
            saveData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadPlugin() {
        this.localizationManager.reloadConfig();
        try {
            saveData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadData();
    }

    private void loadData() {
        var dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists())
            return;

        var dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        townManager.loadData(dataConfig);
    }

    private void saveData() throws IOException {
        var dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.createNewFile();
        }

        var dataConfig = new YamlConfiguration();
        townManager.saveData(dataConfig);
        dataConfig.save(dataFile);
    }

    private void setupTownBordersTimer() {
        this.townBordersTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || player.getInventory().getItemInOffHand().getType().equals(Material.COMPASS))
                    ParticleUtils.showTownBorders(player);
            });
        }, 0, 10);
    }

    private void setupPlotCreationTimer() {
        this.plotCreationTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            plotListener.getPlotsBeingCreated().forEach((uuid, location) -> {
                var player = Bukkit.getPlayer(uuid);
                if (player == null)
                    return;

                if (!player.getWorld().equals(location.getWorld()))
                    return;

                var lookingAt = player.rayTraceBlocks(5, FluidCollisionMode.ALWAYS);
                if (lookingAt == null)
                    return;

                var plot = PlotUtils.getPlotBetween(location, lookingAt.getHitBlock().getLocation());
                ParticleUtils.showPlot(plot.min().toLocation(player.getWorld()), plot.max().toLocation(player.getWorld()), Particle.WAX_ON);
            });
        }, 0, 20);
    }

    private void stopTownBordersTimer() {
        townBordersTask.cancel();
    }

    private void stopPlotCreationTimer() {
        plotCreationTask.cancel();
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public PowerManager getPowerManager() { return powerManager; }

    public TownManager getTownManager() {
        return townManager;
    }

    public static NubladaTowns getInstance() {
        return INSTANCE;
    }

    public static class Keys {
        public static final NamespacedKey TOWN_CREATION_BANNER_KEY = new NamespacedKey("nubladatowns", "town-creation-banner");
    }

}
