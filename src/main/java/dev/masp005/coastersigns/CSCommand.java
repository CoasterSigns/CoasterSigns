package dev.masp005.coastersigns;

import dev.masp005.coastersigns.signs.CSBaseSignAction;
import dev.masp005.coastersigns.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CSCommand implements CommandExecutor, TabCompleter {
    private static final List<String> firstArg = new LinkedList<>();
    private static final Map<String, List<String>> secondArg = new HashMap<>();

    static {
        firstArg.add("signs");
        firstArg.add("rides");

        List<String> ridesSubCMD = new LinkedList<>();
        ridesSubCMD.add("create");
        ridesSubCMD.add("changeid");
        ridesSubCMD.add("modify");
        secondArg.put("rides", ridesSubCMD);
    }

    private final CoasterSigns plugin;

    private BaseComponent mainMessage;
    private BaseComponent signListMessage;

    public CSCommand(CoasterSigns plugin) {
        this.plugin = plugin;

        try {
            Objects.requireNonNull(plugin.getCommand("coastersigns")).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand("coastersigns")).setTabCompleter(this);
            plugin.logInfo("CoasterSigns command registered.", "setup");
        } catch (NullPointerException exception) {
            plugin.logError("CoasterSigns command could not be registered.", "setup");
            plugin.logError(exception.getMessage(), "setup");
        }

        buildMessages();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {
        if (args.length == 0) {
            sender.spigot().sendMessage(mainMessage);
            return true;
        }
        if (!sender.hasPermission("coastersigns.core")) {
            sender.spigot().sendMessage(plugin.noPermsMessage);
            return false;

        }
        switch (args[0]) {
            case "signs":
                sender.spigot().sendMessage(signListMessage);
                return true;
            case "rides":
                if (!sender.hasPermission("coastersigns.rides")) {
                    sender.spigot().sendMessage(plugin.noPermsMessage);
                    return false;
                }
                if (args.length == 1) {
                    sender.spigot().sendMessage(plugin.rideManager.overviewMessage());
                    return true;
                }
                switch (args[1]) {
                    case "create":
                        if (args.length == 2)
                            return false;
                        String id = args[2].toLowerCase();
                        String name = args.length == 3 ? args[2]
                                : String.join(" ", Util.arrayToList(args).subList(2, args.length - 2));
                        plugin.rideManager.createRide(id, name);
                        // TODO: Confirmation message
                        return true;
                    case "modify":
                        if (args.length == 3) {
                            plugin.rideManager.modifyMenu((Player) sender, args[2]);
                            return true;
                        }
                        // TODO: command only modification
                        return true;
                    case "changeid":
                        if (args.length == 4) {
                            String from = args[2].toLowerCase();
                            String to = args[3].toLowerCase();
                            plugin.rideManager.changeId(from, to);
                            return true;
                        } else
                            return false;
                }
        }
        return false;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {
        List<String> entries = new LinkedList<>();
        if (args.length == 0) {
            return firstArg;
        }
        if (args.length == 1) {
            for (String arg : firstArg) {
                if (arg.startsWith(args[0]))
                    entries.add(arg);
            }
            return entries;
        }
        if (args.length == 2) {
            if (!secondArg.containsKey(args[0]))
                return Collections.emptyList();
            for (String arg : secondArg.get(args[0])) {
                if (arg.startsWith(args[1]))
                    entries.add(arg);
            }
            return entries;
        }
        if (args[0] == "rides" && args[1] == "modify") {
            if (args.length == 3) {
                for (String ride : plugin.rideManager.listRides()) {
                    if (ride.startsWith(args[2]))
                        entries.add(ride);
                }
            }
        }
        return Collections.emptyList();
    }

    private void buildMessages() {
        mainMessage = new ComponentBuilder("\nCoasterSigns\n\n").color(ChatColor.AQUA)
                .bold(true).underlined(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.baseDocURL))
                .append("Version: " + plugin.getDescription().getVersion()).reset().color(ChatColor.LIGHT_PURPLE)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                        new ComponentBuilder("Click to view Changelogs.").italic(true).color(ChatColor.GRAY)
                                .create())))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.baseDocURL + "changelogs"))
                .append("\n").reset()
                .append("See available Signs").color(ChatColor.DARK_PURPLE)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coastersigns signs"))
                .append("\n").reset()
                .append("Join the Discord").color(ChatColor.BLUE).bold(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/4433WMu5bj"))
                .append("\n").reset().build();

        ComponentBuilder signListBuilder = new ComponentBuilder("These signs are available:\n").color(ChatColor.AQUA);

        boolean first = true;
        for (CSBaseSignAction sign : plugin.signs) {
            if (first)
                first = false;
            else
                signListBuilder.append(", ").reset().color(ChatColor.WHITE);
            signListBuilder.append(sign.name()).color(sign.isReady() ? ChatColor.GREEN : ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(sign.description())))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, sign.helpURL()));
        }

        signListBuilder.append("\n\nYou can hover and click each feature for more info. Additionally, you can ")
                .reset().color(ChatColor.AQUA)
                .append("join the discord").color(ChatColor.BLUE).underlined(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/4433WMu5bj"))
                .append(" for further documentation.").reset().color(ChatColor.AQUA);

        signListMessage = signListBuilder.build();
    }
}
