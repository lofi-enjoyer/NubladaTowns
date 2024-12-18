package io.github.lofienjoyer.nubladatowns.roles;

public enum Permission {
    BUILD,
    DESTROY,
    INTERACT,
    INVITE,
    KICK,
    RENAME,
    CHANGE_SPAWN,
    MANAGE_ROLES,
    ASSIGN_ROLES,
    CHANGE_BANNER,
    CLAIM_TERRITORY,
    ABANDON_TERRITORY;

    public static boolean contains(String literal) {
        for (Permission permission : Permission.values()) {
            if(permission.name().equals(literal))
                return true;
        }

        return false;
    }
}