package io.github.lofienjoyer.nubladatowns.roles;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private final String name;
    private final List<Permission> permissions = new ArrayList<>();

    public Role(String name) {
        this.name = name;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    public String getName() {
        return name;
    }
}
