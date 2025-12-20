package me.djtmk.InfiniteBuckets.hooks.lands;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public final class LandsHook implements ProtectionHook {

    private final LandsIntegration api;
    private Object blockPlaceFlag;
    private Method hasFlagMethod;
    private boolean isHooked = false;

    public LandsHook() {
        this.api = LandsIntegration.of(Main.getInstance());
        setupReflection();
    }

    private void setupReflection() {
        try {
            try {
                Class<?> v7Flags = Class.forName("me.angeschossen.lands.api.flags.type.Flags");
                this.blockPlaceFlag = v7Flags.getField("BLOCK_PLACE").get(null);

                Class<?> v7RoleFlag = Class.forName("me.angeschossen.lands.api.flags.type.RoleFlag");
                this.hasFlagMethod = Area.class.getMethod("hasFlag", UUID.class, v7RoleFlag);

                this.isHooked = true;
                Main.getInstance().getLogger().info("Lands v7 detected.");
                return;
            } catch (Exception ignored) {}
            try {
                Class<?> v6Flags = Class.forName("me.angeschossen.lands.api.flags.Flags");
                this.blockPlaceFlag = v6Flags.getField("BLOCK_PLACE").get(null);
                Class<?> v6RoleFlag = Class.forName("me.angeschossen.lands.api.flags.types.RoleFlag");
                try {
                    this.hasFlagMethod = Area.class.getMethod("hasRoleFlag", UUID.class, v6RoleFlag);
                } catch (NoSuchMethodException e) {
                    this.hasFlagMethod = Area.class.getMethod("hasFlag", UUID.class, v6RoleFlag);
                }

                this.isHooked = true;
                Main.getInstance().getLogger().info("Lands v6 detected.");
                return;
            } catch (Exception ignored) {}

        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Failed to hook into Lands: " + e.getMessage());
            this.isHooked = false;
        }
    }

    @Override
    public boolean canBuild(Player player, Block block) {
        if (!isHooked || api == null) return true;

        Area area = api.getArea(block.getLocation());
        if (area == null) return true;

        try {
            return (boolean) hasFlagMethod.invoke(area, player.getUniqueId(), blockPlaceFlag);
        } catch (Exception e) {
            return false;
        }
    }
}
