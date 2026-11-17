package net.busybee.InfiniteBuckets.hooks.griefprevention;

import com.griefprevention.protection.ProtectionHelper;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.logging.Level;

/**
 * Asks GriefPrevention whether a player may build or break at a block. Its own checks cover
 * claim trust, admin claims, /ignoreclaims and its wilderness rules; a non-null reply is the
 * reason it would refuse.
 */
public final class GriefPreventionHook implements ProtectionHook {

    /** GriefPrevention 17+ has ProtectionHelper; 16.x only has the older allowBuild/allowBreak. */
    private final boolean modernApi = classExists("com.griefprevention.protection.ProtectionHelper");
    private boolean warned;

    @Override
    public boolean canBuild(Player player, Block block) {
        return canBuild(player, block, Action.PLACE);
    }

    @Override
    public boolean canBuild(Player player, Block block, Action action) {
        if (GriefPrevention.instance == null) return true;

        try {
            return modernApi ? Modern.allowed(player, block, action) : allowedLegacy(player, block, action);
        } catch (RuntimeException | LinkageError e) {
            if (!warned) {
                warned = true;
                Main.getInstance().getLogger().log(Level.WARNING, "GriefPrevention could not check a bucket use; blocking it to be safe.", e);
            }
            // Fail closed: if GriefPrevention can't answer, don't let liquid into a claim
            return false;
        }
    }

    /** GriefPrevention 16.x. */
    @SuppressWarnings("removal")
    private static boolean allowedLegacy(Player player, Block block, Action action) {
        GriefPrevention gp = GriefPrevention.instance;
        String denial = action == Action.BREAK
                ? gp.allowBreak(player, block, block.getLocation())
                : gp.allowBuild(player, block.getLocation(), block.getType());
        return denial == null;
    }

    /** Kept in its own class so 16.x never loads ProtectionHelper. */
    private static final class Modern {
        static boolean allowed(Player player, Block block, Action action) {
            // A break event lets GriefPrevention add-ons (PreventBlockBreakEvent) have their say, as for a mined block
            return ProtectionHelper.checkPermission(player, block.getLocation(), ClaimPermission.Build,
                    action == Action.BREAK ? new BlockBreakEvent(block, player) : null) == null;
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, GriefPreventionHook.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
