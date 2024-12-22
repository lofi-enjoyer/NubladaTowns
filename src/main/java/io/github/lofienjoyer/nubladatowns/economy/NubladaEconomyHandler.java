package io.github.lofienjoyer.nubladatowns.economy;

import io.github.lofienjoyer.nubladatowns.town.Town;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

public class NubladaEconomyHandler {

    private final Economy economy;

    public NubladaEconomyHandler(Economy economy) {
        this.economy = economy;
    }

    public double getTownBalance(Town town) {
        return economy.getBalance(getTownAsOfflinePlayer(town));
    }

    public boolean depositToTown(Town town, double amount) {
        var response = economy.depositPlayer(getTownAsOfflinePlayer(town), amount);
        return response.transactionSuccess();
    }

    public boolean withdrawFromTown(Town town, double amount) {
        var response = economy.withdrawPlayer(getTownAsOfflinePlayer(town), amount);
        return response.transactionSuccess();
    }

    /**
     * https://github.com/TownyAdvanced/Towny/blob/fa24d5755a67150a5c3c37d23888e5adde541797/Towny/src/main/java/com/palmergames/bukkit/towny/object/economy/Account.java#L460
     */
    private OfflinePlayer getTownAsOfflinePlayer(Town town) {
        try {
            var gameProfile = getGameProfileConstructor().invoke(town.getUniqueId(), town.getName());

            return (OfflinePlayer) getOfflinePlayerConstructor().invoke(Bukkit.getServer(), gameProfile);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * https://github.com/TownyAdvanced/Towny/blob/fa24d5755a67150a5c3c37d23888e5adde541797/Towny/src/main/java/com/palmergames/bukkit/towny/object/economy/Account.java#L436
     */
    private static MethodHandle getGameProfileConstructor() {
        try {
            return MethodHandles.publicLookup().findConstructor(Class.forName("com.mojang.authlib.GameProfile"), MethodType.methodType(void.class, UUID.class, String.class));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * https://github.com/TownyAdvanced/Towny/blob/fa24d5755a67150a5c3c37d23888e5adde541797/Towny/src/main/java/com/palmergames/bukkit/towny/object/economy/Account.java#L445
     */
    private static MethodHandle getOfflinePlayerConstructor() {
        try {
            var packagePath = Bukkit.getServer().getClass().getPackage().getName();
            var offlinePlayerClass = Class.forName(packagePath + ".CraftOfflinePlayer");
            var constructor = offlinePlayerClass.getDeclaredConstructor(Class.forName(packagePath + ".CraftServer"), Class.forName("com.mojang.authlib.GameProfile"));
            constructor.setAccessible(true);

            return MethodHandles.lookup().unreflectConstructor(constructor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
