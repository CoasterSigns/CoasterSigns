package dev.masp005.coastersigns;

import dev.masp005.coastersigns.signs.CSBaseSignAction;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CSCommand implements CommandExecutor, TabCompleter {
    private final CoasterSigns pl;

    public CSCommand(CoasterSigns plugin) {
        pl = plugin;
        try {
            Objects.requireNonNull(pl.getCommand("coastersigns")).setExecutor(this);
            Objects.requireNonNull(pl.getCommand("coastersigns")).setExecutor(this);
            pl.logInfo("CoasterSigns command registered.", "setup");
        } catch (NullPointerException exception) {
            pl.logError("CoasterSigns command could not be registered.", "setup");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equals("about")) {
            ComponentBuilder component =
                    new ComponentBuilder("\nCoasterSigns\n\n").color(ChatColor.AQUA).bold(true).underlined(true)
                            .append("Version: " + pl.getDescription().getVersion()).reset().color(ChatColor.LIGHT_PURPLE)
                            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://semver.org"))
                            .append("\n").reset()
                            .append("Join the Discord").color(ChatColor.BLUE).bold(true)
                            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/4433WMu5bj"))
                            .append("\n").reset();
            sender.spigot().sendMessage(component.create());
            return true;
        }
        switch (args[0]) {
            case "signlist":
            case "signs":
                ComponentBuilder component = new ComponentBuilder("These signs are available:\n").color(ChatColor.AQUA);

                boolean first = true;
                for (CSBaseSignAction sign : pl.signs) {
                    if (first) first = false;
                    else component.append(", ").reset().color(ChatColor.WHITE);
                    component.append(sign.name()).color(sign.isReady() ? ChatColor.GREEN : ChatColor.RED)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(sign.description())))
                            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, sign.helpURL()));
                }

                component.append("\n\nYou can hover and click each feature for more info. Additionally, you can ").reset().color(ChatColor.AQUA)
                        .append("join the discord").color(ChatColor.BLUE).underlined(true)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/4433WMu5bj"))
                        .append(" for further documentation.").reset().color(ChatColor.AQUA);

                sender.spigot().sendMessage(component.create());
                return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> entries = new LinkedList<>();
        if (args.length == 0) {
            entries.add("signlist");
            entries.add("signs");
            return entries;
        }
        return null;
    }
}
