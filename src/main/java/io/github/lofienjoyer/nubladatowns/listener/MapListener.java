package io.github.lofienjoyer.nubladatowns.listener;

import io.github.lofienjoyer.nubladatowns.map.TownMapRenderer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;

public class MapListener implements Listener {

    @EventHandler
    public void onMapInitialize(MapInitializeEvent event) {
        event.getMap().addRenderer(new TownMapRenderer());
    }

}
