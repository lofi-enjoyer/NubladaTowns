package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.power.PowerManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.town.TownManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class PowerListener implements Listener {

    private final PowerManager powerManager;
    private final TownManager townManager;

    public PowerListener(TownManager townManager) {
        this.powerManager = NubladaTowns.getInstance().getPowerManager();
        this.townManager = townManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity targetEntity = event.getEntity();
        if(targetEntity.getKiller() instanceof Player killer) {
            Town town = townManager.getPlayerTown(killer);
            if(town == null) return;

            var amount = powerManager.getAmount(targetEntity.getType().toString().toLowerCase());

            if(amount == null) amount = powerManager.getAmount("fallback-amount");
            if(town.getPower() + amount > powerManager.getAmount("max-power-multiplier") * town.getResidents().size()) return;
            town.setPower(town.getPower() + amount);
        }
    }
}
