package me.djtmk.InfiniteBuckets.item;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;

public final class ItemEvents implements Listener {

    private final Main plugin;
    private final BucketRegistry registry;
    private final MessageManager messages;
    private final boolean isSuperiorSkyblockEnabled;

    public ItemEvents(Main plugin) {
        this.plugin = plugin;
        this.registry = plugin.getBucketRegistry();
        this.messages = plugin.getMessageManager();
        this.isSuperiorSkyblockEnabled = plugin.getServer().getPluginManager().isPluginEnabled("SuperiorSkyblock2");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) {
            return;
        }

        Optional<InfiniteBucket> bucketOptional = registry.getBucket(event.getItem());
        if (bucketOptional.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        InfiniteBucket bucket = bucketOptional.get();

        if (!player.hasPermission(bucket.permission())) {
            messages.send(player, "no-permission-use", Placeholder.component("bucket_name", bucket.displayName()));
            return;
        }

        if (isSuperiorSkyblockEnabled && !hasIslandPermission(player)) {
            messages.send(player, "no-island-permission");
            return;
        }

        if (player.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER && !bucket.worksInNether()) {
            messages.send(player, "nether-disabled");
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Material placeMaterial = (bucket.material() == Material.WATER_BUCKET) ? Material.WATER : Material.LAVA;

        if (clickedBlock != null && placeMaterial == Material.WATER && clickedBlock.getBlockData() instanceof Waterlogged waterlogged && !waterlogged.isWaterlogged()) {
            waterlogged.setWaterlogged(true);
            clickedBlock.setBlockData(waterlogged);
            return;
        }

        Block blockToPlaceIn = null;
        if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            blockToPlaceIn = clickedBlock.getRelative(event.getBlockFace());
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            blockToPlaceIn = player.getTargetBlockExact(5);
        }

        if (blockToPlaceIn != null && (blockToPlaceIn.isPassable() || blockToPlaceIn.isLiquid())) {
            if (placeMaterial == Material.WATER && blockToPlaceIn.getBlockData() instanceof Waterlogged waterlogged) {
                waterlogged.setWaterlogged(true);
                blockToPlaceIn.setBlockData(waterlogged);
            } else {
                blockToPlaceIn.setType(placeMaterial);
            }
        }
    }

    private boolean hasIslandPermission(Player player) {
        Island island = SuperiorSkyblockAPI.getIslandAt(player.getLocation());
        if (island == null) return true;
        return island.hasPermission(player, IslandPrivilege.getByName("BUILD"));
    }
}
