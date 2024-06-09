package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.attachments.api.Attachment;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignActionAttachment extends CSBaseSignAction {
    static String name = "AttachmentSwitcher";
    static String debugName = "attchMod";
    // subfeatures: apply, inline
    static String basicDesc = "Modifies the train or cart's attachments.";
    static String helpLink = "";

    public final boolean ready = true;
    private final CoasterSigns pl;

    public SignActionAttachment(CoasterSigns plugin) {
        pl = plugin;
        SignAction.register(this);
        pl.logInfo("TrainCarts Attachment Switcher Sign has been registered.", "setup");
    }

    public boolean match(SignActionEvent info) {
        return info.isType("attachments");
    }

    public void execute(SignActionEvent info) {
        if (!ready) return;
        if (!info.isPowered() || !info.getAction().isMovement()) return;
        Bukkit.broadcastMessage(info.getAction().name());
        if (isApplySign(info.getTrackedSign())) {
            // TODO: Parse Movement Direction Parameters
            String configName = info.getLine(3);
            YamlConfiguration config = pl.readFile("attachments", configName);
            if (config == null) {
                pl.logWarn("AMC " + configName + " does not exist. " + Util.blockCoordinates(info.getBlock()), debugName + ".apply");
                return;
            }
            try {
                if (!config.isSet("modifications"))
                    throw new IllegalArgumentException("AMC does not have modifications property");
                if (info.isCartSign() && info.getAction() == SignActionType.MEMBER_ENTER) {
                    applyAttachmentConfigSingle(config, info.getMember());
                    pl.logInfo(String.format("AMC %s applied. %s", configName, Util.blockCoordinates(info.getBlock())), debugName + ".apply");
                }
                if (info.isTrainSign() && info.getAction() == SignActionType.GROUP_ENTER) {
                    Bukkit.broadcastMessage(info.getCartEnterDirection().toString());
                    Bukkit.broadcastMessage(info.getFacing().name());
                    applyAttachmentConfigGroup(config, info.getGroup());
                    pl.logInfo(String.format("AMC %s applied. %s", configName, Util.blockCoordinates(info.getBlock())), debugName + ".apply");
                }
            } catch (Error e) {
                pl.logWarn(String.format("AMC %s could not be applied. %s %s", configName, Util.blockCoordinates(info.getBlock()), e.toString()), debugName + ".apply");
            }
            // return;
        }
    }

    public boolean build(SignChangeActionEvent info) {
        SignBuildOptions message = SignBuildOptions.create()
                .setHelpURL(helpLink)
                .setName((info.isCartSign() ? "cart" : "train") + " attachment");
        message.setDescription(basicDesc);
        if (info.getTrackedSign().getHeader().isRC()) {
            message.setDescription(basicDesc + "\n\nError: RC is not supported.").handle(info.getPlayer());
            return false;
        }
        if (isApplySign(info.getTrackedSign())) {
            YamlConfiguration config = pl.readFile("attachments", info.getLine(3));
            if (config == null) {
                if (info.getLine(3).endsWith(".yml"))
                    message.setDescription(basicDesc + "\n\nError: Config file not found. Do not include \".yml\"!");
                else message.setDescription(basicDesc + "\n\nError: Config file not found.");
                message.handle(info.getPlayer());
                return false;
            }
            return true;
        }
        message.setDescription(basicDesc + "\n\nError: Currently, the 3rd line needs to be \"apply\", 4th needs to point to a modification config file.").handle(info.getPlayer());
        message.handle(info.getPlayer());
        return false;
    }

    //<editor-fold desc="AMC application methods" defaultstate="collapsed">

    /**
     * Checks if a Sign is configured to apply some specified AMC.
     *
     * @param sign The sign to check
     * @return true if it is, false otherwise.
     */
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

    /**
     * Applies an Attachment Modification Config (AMC) to a MinecartGroup (a TrainCarts Train)
     *
     * @param config The AMC to apply.
     * @param group  The group to apply it to.
     */
    private void applyAttachmentConfigGroup(YamlConfiguration config, MinecartGroup group) {
        List<Map<?, ?>> mods = config.getMapList("modifications");

        if (mods.size() == 0) return;
        for (int i = 0; i < mods.size(); i++) {
            YamlConfiguration modConfig = Util.makeConfig(mods.get(i));
            pl.logInfo(String.format("Applying #%d: %s", i, modConfig.saveToString()), debugName + ".apply.groupConf");

            int[] range = Util.evaluateRange(modConfig.get("cart"), group.size() - 1);
            pl.logInfo(Arrays.toString(range), debugName + ".apply.groupConf");

            for (int j = range[0]; j <= range[1]; j++)
                applyAttachmentModification(modConfig, group.get(j));
        }
    }

    /**
     * Applies an Attachment Modification Config (AMC) to a single TrainCarts-registered cart
     *
     * @param config The AMC to apply.
     * @param member The cart to apply it to.
     */
    private void applyAttachmentConfigSingle(YamlConfiguration config, MinecartMember<?> member) {
        List<Map<?, ?>> mods = config.getMapList("modifications");

        if (mods.size() == 0) return;
        for (int i = 0; i < mods.size(); i++) {
            YamlConfiguration modConfig = Util.makeConfig(mods.get(i));
            pl.logInfo(String.format("Applying #%d: %s", i, modConfig.saveToString()), debugName + ".apply.singleConf");
            applyAttachmentModification(modConfig, member);
        }
    }

    /**
     * Applies a singular modification from an AMC to a single TrainCarts-registered cart.
     *
     * @param config The AMC to apply.
     * @param member The cart to apply it to.
     */
    private void applyAttachmentModification(YamlConfiguration config, MinecartMember<?> member) {
        Attachment target = member.getAttachments().getRootAttachment();

        pl.logInfo(config.saveToString(), debugName + ".apply.singleMod");

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
    //</editor-fold>

    public String name() {
        return name;
    }

    public String description() {
        return "§bAttachment Modifier\n§6§lSecond line: attachment\n§r§3" + basicDesc;
    }

    public String helpURL() {
        return helpLink;
    }

    public boolean isReady() {
        return ready;
    }
}
