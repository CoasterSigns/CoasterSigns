package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import de.themoep.timedscripts.TimedScripts;
import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.Util;
import org.bukkit.Bukkit;

public class SignActionTimedScript extends CSBaseSignAction {
    static String name = "TimedScriptExecutor";
    static String debugName = "tmdScr";
    // subfeatures: execution
    static String basicDesc = "Executes the given Script provided by TimedScripts.";
    static String helpLink = "https://github.com/CoasterSigns/CoasterSigns/blob/main/docs/timedscript.md";

    public final boolean ready;
    private final TimedScripts timedScriptsPlugin;
    private final CoasterSigns pl;

    public SignActionTimedScript(CoasterSigns plugin) {
        pl = plugin;
        if (Bukkit.getPluginManager().getPlugin("timedscripts") == null) {
            ready = false;
            timedScriptsPlugin = null;
            pl.logInfo("TrainCarts TimedScripts Executor could not be registered.", "setup");
        } else {
            ready = true;
            timedScriptsPlugin = ((TimedScripts) Bukkit.getPluginManager().getPlugin("timedscripts"));
            SignAction.register(this);
            pl.logInfo("TrainCarts TimedScripts Executor has been registered.", "setup");
        }
    }

    public boolean match(SignActionEvent info) {
        return info.isType("timedscript") && ready;
    }

    public void execute(SignActionEvent info) {
        if (!ready) return;
        if (!info.isPowered() || !info.isAction(SignActionType.GROUP_ENTER)) return;
        if (timedScriptsPlugin.getScriptManager().runScript(Bukkit.getConsoleSender(), info.getLine(2)))
            pl.logInfo("Script " + info.getLine(2) + " executed." + Util.blockCoordinates(info.getBlock()), debugName + ".execution");
        else
            pl.logWarn("Script " + info.getLine(2) + " not found! " + Util.blockCoordinates(info.getBlock()), debugName + ".execution");
    }

    public boolean build(SignChangeActionEvent signChangeActionEvent) {
        return ready;
    }

    public String name() {
        return name;
    }

    public String description() {
        return "§bTimedScipts Executor\n§6§lSecond line: timedscript\n§r§3" + basicDesc + (ready ? "" : "\n\n§cRequires TimedScripts to be installed.");
    }

    public String helpURL() {
        return helpLink;
    }

    public boolean isReady() {
        return ready;
    }
}
