package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.signactions.SignAction;

public abstract class CSBaseSignAction extends SignAction {
    public abstract String name();

    public abstract String description();

    public abstract boolean isReady();
}
