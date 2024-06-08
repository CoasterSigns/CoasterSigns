package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import de.themoep.timedscripts.TimedScripts;
import dev.masp005.coastersigns.CoasterSigns;
import org.bukkit.Bukkit;

public class SignActionTimedScript extends CSBaseSignAction {
    static String name = "TimedScriptExecutor";

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
            pl.logInfo("TrainCarts TimedScripts Executor has been registered.", "setup");
        }
    }

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("timedscript") && ready;
    }

    @Override
    public void execute(SignActionEvent info) {
        if (!info.isPowered() || !info.isAction(SignActionType.GROUP_ENTER)) return;
        timedScriptsPlugin.getScriptManager().runScript(Bukkit.getConsoleSender(), info.getLine(2));
    }

    @Override
    public boolean build(SignChangeActionEvent signChangeActionEvent) {
        return ready;
    }

    @Override
    public String name() {
        return "TimedScriptsExecutor";
    }

    @Override
    public String description() {
        return "§bTimedScipts Executor\n§3Executes the given Script provided by TimedScripts" + (ready ? "" : "\n\n§cRequires TimedScripts to be installed.");
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
