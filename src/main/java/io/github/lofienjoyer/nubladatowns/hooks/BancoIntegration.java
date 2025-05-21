package io.github.lofienjoyer.nubladatowns.hooks;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import ovh.mythmc.banco.api.storage.BancoInventory;

import java.util.UUID;

public class BancoIntegration extends BancoInventory {

    @Override
    public @NotNull Inventory inventory(UUID uuid) {
        var town = NubladaTowns.getInstance().getTownManager().getTownByUUID(uuid);
        if (town == null)
            return Bukkit.createInventory(null, 9);

        return town.getInventory();
    }

    @Override
    public String friendlyName() {
        return "Town Vault";
    }

    @Override
    public boolean supportsOfflinePlayers() {
        return true;
    }
}
