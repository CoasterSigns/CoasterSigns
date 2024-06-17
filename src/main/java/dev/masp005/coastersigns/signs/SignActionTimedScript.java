package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.themoep.timedscripts.TimedScripts;
import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.Util;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class SignActionTimedScript extends CSBaseSignAction {
    static String name = "TimedScriptExecutor";
    static String debugName = "tmdScr";
    // subfeatures: execution
    static String basicDesc = "execute a given Script provided by TimedScripts";
    static String helpLink = "signs/timedscript.html";

    public final boolean ready;
    private final TimedScripts timedScriptsPlugin;
    private final CoasterSigns plugin;

    public SignActionTimedScript(CoasterSigns plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().getPlugin("timedscripts") == null) {
            ready = false;
            timedScriptsPlugin = null;
            plugin.logInfo("TrainCarts TimedScripts Executor could not be registered.", "setup");
        } else {
            ready = true;
            timedScriptsPlugin = ((TimedScripts) Bukkit.getPluginManager().getPlugin("timedscripts"));
            SignAction.register(this);
            plugin.logInfo("TrainCarts TimedScripts Executor has been registered.", "setup");
        }
    }

    public boolean match(SignActionEvent info) {
        return info.isType("timedscript") && ready;
    }

    public void execute(SignActionEvent info) {
        if (!ready)
            return;
        if (!info.isPowered() || !info.isAction(SignActionType.GROUP_ENTER))
            return;

        Map<String, String> replacements = new HashMap<>();
        // TODO: account for virtual signs
        replacements.put("sender", "CoasterSigns Sign");
        replacements.put("senderworld", info.getWorld().getName());
        replacements.put("senderx", String.valueOf(info.getBlock().getX()));
        replacements.put("sendery", String.valueOf(info.getBlock().getY()));
        replacements.put("senderz", String.valueOf(info.getBlock().getZ()));
        replacements.put("senderyaw", String.valueOf(Util.blockFaceYaw(info.getFacing())));
        replacements.put("senderpitch", "0");
        replacements.put("senderlocation", Util.blockCoordinates(info.getBlock(), " "));

        String line4 = info.getLine(3);
        if (!line4.equals("")) {
            if (info.getLine(3).contains("=")) {
                int equalIdx = line4.indexOf('=');
                replacements.put(line4.substring(0, equalIdx), line4.substring(equalIdx + 1));
            } else
                plugin.logWarn(
                        String.format("Line 4 \"%s\" is not key=value (%s)", line4,
                                Util.blockCoordinates(info.getBlock())),
                        debugName + ".execution");
        }

        if (timedScriptsPlugin.getScriptManager().runScript(Bukkit.getConsoleSender(), info.getLine(2), replacements))
            plugin.logInfo(
                    String.format("Script %s executed. (%s)", info.getLine(2), Util.blockCoordinates(info.getBlock())),
                    debugName + ".execution");
        else
            plugin.logWarn(
                    String.format("Script %s not found! (%s)", info.getLine(2), Util.blockCoordinates(info.getBlock())),
                    debugName + ".execution");
    }

    public boolean build(SignChangeActionEvent info) {
        SignBuildOptions message = SignBuildOptions.create()
                .setHelpURL(helpLink)
                .setName("TimedScript Executor")
                .setDescription(ready ? basicDesc
                        : basicDesc + ".\n\n§cError: TimedScripts is not installed, so this sign will not work");
        message.handle(info.getPlayer());
        return ready;
    }

    public String name() {
        return name;
    }

    public String description() {
        return String.format("§bTimedScipts Executor\n§6§lSecond line: timedscript\n§r§3This sign can %s.", basicDesc)
                + (ready ? "" : "\n\n§cRequires TimedScripts to be installed.");
    }

    public String helpURL() {
        return plugin.baseDocURL + helpLink;
    }

    public boolean isReady() {
        return ready;
    }

}
