package net.busybee.InfiniteBuckets.item;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Gives a placed drain bucket (place-block) back as the bucket when it is broken, and keeps track of it when moved. */
public final class PlacedBucketBlockListener implements Listener {

    private final PlacedBucketBlocks placed;

    public PlacedBucketBlockListener(@NotNull PlacedBucketBlocks placed) {
        this.placed = placed;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(@NotNull BlockDropItemEvent event) {
        PlacedBucketBlocks.Placed mark = placed.take(event.getBlock());
        if (mark == null) return;
        ItemStack bucket = placed.itemFor(mark);
        if (bucket == null) return;

        // Swap the plain block drop (a sponge, or a wet sponge if it soaked up water since) for the bucket
        for (Item drop : event.getItems()) {
            if (isPlacedType(drop.getItemStack().getType(), event.getBlockState().getType())) {
                drop.setItemStack(bucket);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(@NotNull BlockBreakEvent event) {
        // No drop event follows these breaks, so forget the block here
        if (!event.isDropItems() || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            placed.clear(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        // A block placed where a marked one went missing (rollback, WorldEdit) must not inherit the mark
        placed.clear(event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        explode(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        explode(event.blockList());
    }

    /** Blown-up placed buckets always drop the bucket, whatever the explosion's drop chance. */
    private void explode(List<Block> blocks) {
        for (Iterator<Block> it = blocks.iterator(); it.hasNext(); ) {
            Block block = it.next();
            PlacedBucketBlocks.Placed mark = placed.take(block);
            if (mark == null) continue;
            ItemStack bucket = placed.itemFor(mark);
            if (bucket == null) continue;
            it.remove();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), bucket);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        move(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(@NotNull BlockPistonRetractEvent event) {
        move(event.getBlocks(), event.getDirection());
    }

    /** Moves the marks along with the blocks a piston pushes or pulls ({@code direction} is where they go). */
    private void move(List<Block> blocks, BlockFace direction) {
        List<Block> to = new ArrayList<>();
        List<PlacedBucketBlocks.Placed> marks = new ArrayList<>();
        // Take every mark first: blocks in a row move onto each other's old spots
        for (Block block : blocks) {
            PlacedBucketBlocks.Placed mark = placed.take(block);
            if (mark == null) continue;
            to.add(block.getRelative(direction));
            marks.add(mark);
        }
        for (int i = 0; i < to.size(); i++) {
            placed.mark(to.get(i), marks.get(i).bucketId(), marks.get(i).uses());
        }
    }

    private static boolean isPlacedType(Material drop, Material block) {
        if (drop == block) return true;
        return (drop == Material.SPONGE || drop == Material.WET_SPONGE)
                && (block == Material.SPONGE || block == Material.WET_SPONGE);
    }
}
