package io.github.lofienjoyer.nubladatowns;

import io.github.lofienjoyer.nubladatowns.command.AdminCommand;
import io.github.lofienjoyer.nubladatowns.command.TownCommand;
import io.github.lofienjoyer.nubladatowns.configuration.ConfigValues;
import io.github.lofienjoyer.nubladatowns.data.DataManager;
import io.github.lofienjoyer.nubladatowns.data.YamlDataManager;
import io.github.lofienjoyer.nubladatowns.economy.NubladaEconomyHandler;
import io.github.lofienjoyer.nubladatowns.hooks.BancoIntegration;
import io.github.lofienjoyer.nubladatowns.hooks.SquareMapIntegration;
import io.github.lofienjoyer.nubladatowns.hooks.TownPlaceholderExpansion;
import io.github.lofienjoyer.nubladatowns.listener.*;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.plot.PlotUtils;
import io.github.lofienjoyer.nubladatowns.power.PowerManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ovh.mythmc.banco.api.Banco;

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
    private DataManager dataManager;
    private ConfigValues configValues;

    private SquareMapIntegration squareMapIntegration;
    private NubladaEconomyHandler economyHandler;

    private boolean economyEnabled;

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveDefaultConfig();
        this.configValues = new ConfigValues();

        this.localizationManager = new LocalizationManager();
        this.powerManager = new PowerManager();

        this.townManager = new TownManager(this);
        dataManager = new YamlDataManager(new File(getDataFolder(), "data.yml"));
        loadData();

        getCommand("town").setExecutor(new TownCommand(townManager));
        getCommand("nubladatownsadmin").setExecutor(new AdminCommand());

        getServer().getPluginManager().registerEvents(new TownListener(townManager), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(townManager), this);
        getServer().getPluginManager().registerEvents(new PowerListener(townManager), this);
        this.plotListener = new PlotListener(this);
        getServer().getPluginManager().registerEvents(plotListener, this);
        getServer().getPluginManager().registerEvents(new MapListener(), this);

        setupTownBordersTimer();
        setupPlotCreationTimer();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new TownPlaceholderExpansion(this, this.townManager);

        if (Bukkit.getPluginManager().isPluginEnabled("squaremap"))
            this.squareMapIntegration = new SquareMapIntegration(this, this.townManager);

        if (Bukkit.getPluginManager().isPluginEnabled("vault"))
            setupEconomy();

        if (Bukkit.getPluginManager().isPluginEnabled("banco"))
            Banco.get().getStorageRegistry().registerStorage(new BancoIntegration());
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
        reloadConfig();
        this.configValues = new ConfigValues();
        powerManager.reloadConfig();
        this.localizationManager.reloadConfig();
    }

    public void loadData() {
        townManager.loadData(dataManager);
    }

    public void saveData() throws IOException {
        townManager.saveData(dataManager);
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

                if (plot.max().getX() - plot.min().getX() > 64 || plot.max().getZ() - plot.min().getZ() > 64)
                    return;

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

    private void setupEconomy() {
        this.economyHandler = new NubladaEconomyHandler();
        this.economyEnabled = true;
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public PowerManager getPowerManager() { return powerManager; }

    public TownManager getTownManager() {
        return townManager;
    }

    public ConfigValues getConfigValues() {
        return configValues;
    }

    public NubladaEconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public static NubladaTowns getInstance() {
        return INSTANCE;
    }

    public static class Keys {
        public static final NamespacedKey TOWN_INVITE_KEY = new NamespacedKey("nubladatowns", "town-invite");
    }

}
