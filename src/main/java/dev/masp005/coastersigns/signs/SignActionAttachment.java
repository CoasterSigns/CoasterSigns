package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.attachments.api.Attachment;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignActionAttachment extends CSBaseSignAction {
    static String name = "AttachmentSwitcher";

    public final boolean ready = true;
    private final CoasterSigns pl;

    public SignActionAttachment(CoasterSigns plugin) {
        pl = plugin;
        pl.logInfo("TrainCarts Attachment Switcher Sign has been registered.", "setup");
    }

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("attachments");
    }

    @Override
    public void execute(SignActionEvent info) {
        if (!info.isPowered() || !info.getAction().isMovement()) return;
        Bukkit.broadcastMessage(info.getAction().name());
        if (isApplySign(info.getTrackedSign())) {
            // TODO: Parse Movement Direction Parameters
            YamlConfiguration config = pl.readFile(info.getLine(3));
            if (config == null) return;
            try {
                if (info.isCartSign() && info.getAction() == SignActionType.MEMBER_ENTER)
                    applyAttachmentConfigSingle(config, info.getMember());
                if (info.isTrainSign() && info.getAction() == SignActionType.GROUP_ENTER) {
                    Bukkit.broadcastMessage(info.getCartEnterDirection().toString());
                    Bukkit.broadcastMessage(info.getFacing().name());
                    applyAttachmentConfigGroup(config, info.getGroup());
                }
            } catch (Error e) {
                pl.getLogger().warning("Invalid modification config: " + info.getLine(3));
                pl.getLogger().warning(e.toString());
            }
            // return;
        }
    }

    @Override
    public boolean build(SignChangeActionEvent info) {
        if (info.getTrackedSign().getHeader().isRC()) {
            info.getPlayer().sendMessage("rc is not supported (yet™)");
            return false;
        }
        if (isApplySign(info.getTrackedSign())) {
            YamlConfiguration config = pl.readFile(info.getLine(3));
            if (config == null) {
                info.getPlayer().sendMessage("missing or invalid config");
                if (info.getLine(3).endsWith(".yml"))
                    info.getPlayer().sendMessage("no .yml just the filename");
                return false;
            }
            return true;
        }
        info.getPlayer().sendMessage("3rd line needs to be \"apply\", 4th needs to point to a modification config");

        /*SignBuildOptions.create()
                .setName(info.isCartSign() ? "cart enableslowdown" : "train enableslowdown")
                .setDescription("leaves the train free to handle by gravity and changes its maxspeed")
                .handle(info.getPlayer());*/
        return false;
    }

    private boolean isApplySign(RailLookup.TrackedSign sign) {
        String line2 = sign.getLine(2);
        if (!line2.startsWith("apply")) return false;
        if (line2.equals("apply")) return true;
        String extras = line2.substring("apply".length()).trim();
        if ((extras.equals(">") || extras.equals("<")) && sign.isRealSign()) return true;
        try {
            BlockFace.valueOf(extras.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }


    public void applyAttachmentConfigGroup(YamlConfiguration config, MinecartGroup group) {
        List<Map<?, ?>> mods = config.getMapList("modifications");

        if (mods.size() == 0) return;
        for (Map<?, ?> mod : mods) {
            YamlConfiguration modConfig = Util.makeConfig(mod);

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
                applyAttachmentConfigSingle(modConfig, group.get(i));
            }
        }
    }

    public void applyAttachmentConfigSingle(YamlConfiguration config, MinecartMember<?> member) {
        List<Map<?, ?>> mods = config.getMapList("modifications");

        if (mods.size() == 0) return;
        for (Map<?, ?> mod : mods) {
            applyAttachmentModification(Util.makeConfig(mod), member);
        }
    }

    public void applyAttachmentModification(YamlConfiguration config, MinecartMember<?> member) {
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

    @Override
    public String name() {
        return "AttachmentModifier";
    }

    @Override
    public String description() {
        return "§bAttachment Modifier\n§3Modifies the train or cart's attachments.";
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
