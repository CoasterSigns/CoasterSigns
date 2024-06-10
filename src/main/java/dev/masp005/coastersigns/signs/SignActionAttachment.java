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
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignActionAttachment extends CSBaseSignAction {
    static String name = "AttachmentSwitcher";
    static String debugName = "attchMod";
    // subfeatures: apply, inline, direction
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
        if (!((info.isCartSign() && info.getAction() == SignActionType.MEMBER_ENTER) ||
                (info.isTrainSign() && info.getAction() == SignActionType.GROUP_ENTER)))
            return;
        ModSignType type = new ModSignType(info.getTrackedSign());

        // TODO: Parse Movement Direction Parameters
        pl.logInfo(String.format("Direction: %s Facing: %s Type dir: %s", info.getCartEnterDirection().toString(), info.getFacing().name(), type.direction), debugName + ".direction");
        if (!type.matchDirection(info.getCartEnterDirection(), info.getFacing())) return;
        pl.logInfo("Direction check passed.", debugName + ".direction");
        if (type.isApply) {
            String configName = info.getLine(3);
            YamlConfiguration config = pl.readFile("attachments", configName);
            if (config == null) {
                pl.logWarn("AMC " + configName + " does not exist. " + Util.blockCoordinates(info.getBlock()), debugName + ".apply");
                return;
            }
            try {
                if (!config.isSet("modifications"))
                    throw new IllegalArgumentException("AMC does not have modifications property");
                if (info.isCartSign()) {
                    applyAttachmentListConfigSingle(config, info.getMember());
                } else {
                    applyAttachmentListConfigGroup(config, info.getGroup());
                }
                pl.logInfo(String.format("AMC %s applied. %s", configName, Util.blockCoordinates(info.getBlock())), debugName + ".apply");
            } catch (Error e) {
                pl.logWarn(String.format("AMC %s could not be applied. %s %s", configName, Util.blockCoordinates(info.getBlock()), e.toString()), debugName + ".apply");
            }
        } else {
            YamlConfiguration modification = type.toSingleModConfig();
            String modStr = info.getLine(3);
            if (modStr.startsWith("i=")) modification.set("item", modStr.substring(2));
            if (modStr.startsWith("t=")) modification.set("type", modStr.substring(2));
            if (modStr.startsWith("m="))
                modification.set("custommodeldata", Integer.parseInt(modStr.substring(2)));

            pl.logInfo(String.format("Inline mod result %s:\n%s", Util.blockCoordinates(info.getBlock()), modification.saveToString()), debugName + ".inline.parse");

            if (info.isCartSign()) {
                applyAttachmentModification(modification, info.getMember());
                pl.logInfo(String.format("Inline member modification applied. %s", Util.blockCoordinates(info.getBlock())), debugName + ".inline");
            } else {
                applySingleAttachmentConfigGroup(modification, info.getGroup());
                pl.logInfo(String.format("Inline group modification applied. %s", Util.blockCoordinates(info.getBlock())), debugName + ".inline");
            }
        }
    }

    public boolean build(SignChangeActionEvent info) {
        SignBuildOptions message = SignBuildOptions.create()
                .setHelpURL(helpLink)
                .setName((info.isCartSign() ? "cart" : "train") + " attachment")
                .setDescription(basicDesc);
        if (info.getTrackedSign().getHeader().isRC()) {
            message.setDescription(basicDesc + "\n\nError: RC is not supported.").handle(info.getPlayer());
            message.handle(info.getPlayer());
            return false;
        }
        if (new ModSignType(info.getTrackedSign()).isApply) {
            YamlConfiguration config = pl.readFile("attachments", info.getLine(3));
            if (config == null) {
                if (info.getLine(3).endsWith(".yml"))
                    message.setDescription(basicDesc + "\n\nError: Config file not found. Do not include \".yml\"!");
                else message.setDescription(basicDesc + "\n\nError: Config file not found.");
                message.handle(info.getPlayer());
                return false;
            }
            message.handle(info.getPlayer());
            return true;
        }
        message.handle(info.getPlayer());
        return true;
    }

    /**
     * Applies an Attachment Modification Config (AMC) to a MinecartGroup (a TrainCarts Train)
     *
     * @param config The AMC to apply.
     * @param group  The group to apply it to.
     */
    private void applyAttachmentListConfigGroup(YamlConfiguration config, MinecartGroup group) {
        List<Map<?, ?>> mods = config.getMapList("modifications");
        pl.logInfo("Mod count: " + mods.size(), debugName + ".apply.groupConf");

        if (mods.size() == 0) return;
        for (int i = 0; i < mods.size(); i++) {
            YamlConfiguration modConfig = Util.makeConfig(mods.get(i));
            pl.logInfo(String.format("Applying #%d: %s", i, modConfig.saveToString()), debugName + ".apply.groupConf");
            applySingleAttachmentConfigGroup(modConfig, group);
        }
    }

    /**
     * Applies a single entry of an AMC to a MinecartGroup (a TrainCarts Train)
     *
     * @param config The AMC to apply.
     * @param group  The group to apply it to.
     */
    private void applySingleAttachmentConfigGroup(YamlConfiguration config, MinecartGroup group) {
        int[] range = Util.evaluateRange(config.get("cart"), group.size() - 1);
        pl.logInfo(Arrays.toString(range), debugName + ".apply.groupConf");

        for (int j = range[0]; j <= range[1]; j++)
            applyAttachmentModification(config, group.get(j));
    }

    /**
     * Applies an Attachment Modification Config (AMC) to a single TrainCarts-registered cart
     *
     * @param config The AMC to apply.
     * @param member The cart to apply it to.
     */
    private void applyAttachmentListConfigSingle(YamlConfiguration config, MinecartMember<?> member) {
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

    private static class ModSignType {
        public boolean isApply;
        public boolean isInline;
        public String range;
        public String child;
        public char direction;

        public ModSignType(RailLookup.TrackedSign sign) {
            String[] line2 = sign.getLine(2).trim().split(" ");

            String target = line2[0];
            if (target.equals("apply")) {
                isApply = true;
                isInline = false;
            } else {
                isApply = false;
                isInline = true;
                if (!target.equals("inline")) {
                    int rStartIdx = target.indexOf('r');
                    int cStartIdx = target.indexOf('c');
                    if (rStartIdx != -1 || cStartIdx != -1) {
                        if (rStartIdx == -1) {
                            child = target.substring(cStartIdx + 1);
                        } else if (cStartIdx == -1) {
                            range = target.substring(rStartIdx + 1);
                        } else {
                            if (rStartIdx < cStartIdx) {
                                range = target.substring(rStartIdx + 1, cStartIdx);
                                child = target.substring(cStartIdx + 1);
                            } else {
                                child = target.substring(cStartIdx + 1, rStartIdx);
                                range = target.substring(rStartIdx + 1);
                            }
                        }
                    }
                }
            }

            if (line2.length == 2) {
                direction = line2[1].charAt(0);
                if (direction == '<') direction = 'l';
                if (direction == '>') direction = 'r';
                if (!"rlneswud".contains(String.valueOf(direction))) direction = '*';
            } else direction = '*';

            BlockFace signDir = Util.nearestCartesianDirection(sign.getFacing());
            if (direction == 'r' || direction == 'l') {
                switch (signDir) {
                    case NORTH:
                        direction = (direction == 'r') ? 'w' : 'e';
                        break;
                    case SOUTH:
                        direction = (direction != 'r') ? 'w' : 'e';
                        break;
                    case WEST:
                        direction = (direction == 'r') ? 's' : 'n';
                        break;
                    case EAST:
                        direction = (direction != 'r') ? 's' : 'n';
                        break;
                }
            }
        }

        public YamlConfiguration toSingleModConfig() {
            if (isApply) throw new IllegalStateException("Cannot create Mod Config for apply signs.");
            YamlConfiguration result = new YamlConfiguration();
            result.set("cart", range);
            result.set("child", child);
            return result;
        }

        public boolean matchDirection(Vector movement, BlockFace signDir) {
            if (direction == '*') return true;
            BlockFace movementDir = Util.nearestCartesianDirection(movement);
            return Util.cartesianDirectionCharMap.get(direction).equals(movementDir);
        }
    }
}