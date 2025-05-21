package io.github.lofienjoyer.nubladatowns.economy;

import io.github.lofienjoyer.nubladatowns.town.Town;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import ovh.mythmc.banco.api.Banco;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.UUID;

public class NubladaEconomyHandler {

    public double getTownBalance(Town town) {
        var townAccount = Banco.get().getAccountManager().getByUuid(town.getUniqueId());
        if (townAccount == null) {
            Banco.get().getAccountManager().create(town.getUniqueId());
        }

        return Banco.get().getAccountManager().amount(town.getUniqueId()).doubleValue();
    }

    public boolean depositToTown(Town town, double amount) {
        Banco.get().getAccountManager().deposit(town.getUniqueId(), BigDecimal.valueOf(amount));
        return true;
    }

    public boolean withdrawFromTown(Town town, double amount) {
        Banco.get().getAccountManager().withdraw(town.getUniqueId(), BigDecimal.valueOf(amount));
        return true;
    }

}
