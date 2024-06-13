package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.signactions.SignAction;

public abstract class CSBaseSignAction extends SignAction {
    /**
     * @return The name of the sign.
     */
    public abstract String name();

    /**
     * @return The description of the sign's functionality.
     */
    public abstract String description();

    /**
     * @return A URL to find help for the sign.
     */
    public abstract String helpURL();

    /**
     * @return Whether or not the sign can be used based on dependencies being
     *         installed.
     */
    public abstract boolean isReady();
}
