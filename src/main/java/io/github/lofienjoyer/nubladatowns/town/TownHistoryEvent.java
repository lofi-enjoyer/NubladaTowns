package io.github.lofienjoyer.nubladatowns.town;

import java.util.Date;

public class TownHistoryEvent {

    private final Date timestamp;
    private final String description;
    private final Type type;

    public TownHistoryEvent(Date timestamp, String description, Type type) {
        this.timestamp = timestamp;
        this.description = description;
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public enum Type {

        CREATION, PLAYER_JOIN, PLAYER_LEAVE;

    }

}
