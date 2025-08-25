package io.github.lofienjoyer.nubladatowns.data;

import io.github.lofienjoyer.nubladatowns.town.Town;

import java.io.IOException;
import java.util.Collection;

public interface DataManager {

    Collection<Town> loadTowns();

    void save(Collection<Town> towns) throws IOException;

}
