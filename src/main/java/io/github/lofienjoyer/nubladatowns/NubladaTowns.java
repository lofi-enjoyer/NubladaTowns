package io.github.lofienjoyer.nubladatowns;

import io.github.lofienjoyer.nubladatowns.command.AdminCommand;
import io.github.lofienjoyer.nubladatowns.command.TownCommand;
import io.github.lofienjoyer.nubladatowns.data.DataManager;
import io.github.lofienjoyer.nubladatowns.data.YamlDataManager;
import io.github.lofienjoyer.nubladatowns.listener.PowerListener;
import io.github.lofienjoyer.nubladatowns.listener.ProtectionListener;
import io.github.lofienjoyer.nubladatowns.listener.TownListener;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.power.PowerManager;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import io.github.lofienjoyer.nubladatowns.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
    private DataManager dataManager;

    @Override
    public void onEnable() {
        INSTANCE = this;

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

        setupTownBordersTimer();
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

    private void stopTownBordersTimer() {
        townBordersTask.cancel();
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
        public static final NamespacedKey TOWN_INVITE_KEY = new NamespacedKey("nubladatowns", "town-invite");
    }

}
