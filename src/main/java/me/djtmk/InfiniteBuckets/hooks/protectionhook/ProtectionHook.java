package me.djtmk.InfiniteBuckets.hooks.protectionhook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ProtectionHook {
    boolean canBuild(Player player, Block block);
}
