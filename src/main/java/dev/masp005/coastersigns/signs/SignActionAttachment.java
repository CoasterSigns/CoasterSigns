package dev.masp005.coastersigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.rails.direction.RailEnterDirection;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import dev.masp005.coastersigns.CoasterSigns;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;

public class SignActionAttachment extends SignAction {
    private final CoasterSigns pl;

    public SignActionAttachment(CoasterSigns plugin) {
        pl = plugin;
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
                    pl.apply(config, info.getMember());
                if (info.isTrainSign() && info.getAction() == SignActionType.GROUP_ENTER) {
                    Bukkit.broadcastMessage(info.getCartEnterDirection().toString());
                    Bukkit.broadcastMessage(info.getFacing().name());
                    pl.apply(config, info.getGroup());
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
}
