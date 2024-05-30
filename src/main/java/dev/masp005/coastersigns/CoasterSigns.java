package dev.masp005.coastersigns;

import com.bergerkiller.bukkit.tc.attachments.api.Attachment;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import dev.masp005.coastersigns.signs.SignActionAttachment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CoasterSigns extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && getDataFolder().mkdir())
            getLogger().info("Created Config File Folder.");

        SignAction.register(new SignActionAttachment(this));
        getLogger().info("TrainCarts Attachment Switcher Sign has been registered.");
    }

    @Override
    public void onDisable() {
    }

    public YamlConfiguration readFile(String name) {
        File file = new File(getDataFolder(), name + ".yml");
        if (!file.exists()) return null;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isSet("modifications")) return null;
        return config;
    }

    public void apply(YamlConfiguration config, MinecartGroup group) {
        List<Map<?, ?>> mods = config.getMapList("modifications");

        if (mods.size() == 0) return;
        for (Map<?, ?> mod : mods) {
            YamlConfiguration modConfig = makeConfig(mod);

            String range;
            int rangeMin;
            int rangeMax;
            Object rangeRaw = modConfig.get("cart");

            if (rangeRaw instanceof Integer) range = String.valueOf(rangeRaw);
            else if (rangeRaw instanceof String) range = (String) rangeRaw;
            else if (rangeRaw == null) range = "..";
            else throw new IllegalArgumentException("cart");
            range = range.trim();

            if (range.equals("..")) {
                rangeMin = 0;
                rangeMax = group.size() - 1;
            } else if (range.startsWith("..")) {
                rangeMin = 0;
                rangeMax = Integer.parseInt(range.substring(2));
            } else if (range.endsWith("..")) {
                rangeMin = Integer.parseInt(range.substring(0, range.indexOf('.')));
                rangeMax = group.size() - 1;
            } else if (range.contains("..")) {
                int delimiter = range.indexOf('.');
                rangeMin = Integer.parseInt(range.substring(0, delimiter));
                rangeMax = Integer.parseInt(range.substring(delimiter + 2));
            } else {
                rangeMin = rangeMax = Integer.parseInt(range);
            }
            rangeMin = Math.max(0, rangeMin);
            rangeMax = Math.min(group.size() - 1, rangeMax);

            for (int i = rangeMin; i <= rangeMax; i++) {
                applyMod(modConfig, group.get(i));
            }
        }
    }

    public void apply(YamlConfiguration config, MinecartMember<?> member) {
        List<Map<?, ?>> mods = config.getMapList("modifications");

        if (mods.size() == 0) return;
        for (Map<?, ?> mod : mods) {
            applyMod(makeConfig(mod), member);
        }
    }

    public void applyMod(YamlConfiguration config, MinecartMember<?> member) {
        Attachment target = member.getAttachments().getRootAttachment();

        if (config.isSet("child")) {
            Object childRaw = config.get("child");

            if (childRaw instanceof Integer) target = target.getChildren().get((int) childRaw);
            else if (childRaw instanceof String) {
                for (String s : ((String) childRaw).split(":"))
                    target = target.getChildren().get(Integer.parseInt(s));
            } else throw new IllegalArgumentException("child");
        }
        if (config.isSet("type")) {
            Bukkit.broadcastMessage((String) target.getConfig().get("type"));
            String type = Objects.requireNonNull(config.getString("type")).toLowerCase();
            switch (type) {
                case "none":
                case "empty":
                    target.getConfig().set("type", "EMPTY");
                    break;
                case "item":
                    target.getConfig().set("type", "ITEM");
                    break;
                default:
                    throw new IllegalArgumentException("invalid type argument");
            }
        }
        if (config.isSet("custommodeldata") || config.isSet("item")) {
            try {
                ItemStack item;
                ItemMeta meta;
                item = (ItemStack) target.getConfig().get("item");
                meta = item.getItemMeta();
                if (config.isSet("custommodeldata") && meta != null) {
                    meta.setCustomModelData(config.getInt("custommodeldata"));
                    item.setItemMeta(meta);
                }
                if (config.getString("item") != null)
                    item.setType(Material.valueOf(Objects.requireNonNull(config.getString("item")).toUpperCase()));
                target.getConfig().set("item", item);
                member.getAttachments().syncRespawn();
            } catch (ClassCastException err) {
                throw new IllegalStateException("this train does not have an item on the primary attachment.");
            }
        }
    }

    private YamlConfiguration makeConfig(Map<?, ?> values) {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            config.set((String) entry.getKey(), entry.getValue());
        }
        return config;
    }

}
