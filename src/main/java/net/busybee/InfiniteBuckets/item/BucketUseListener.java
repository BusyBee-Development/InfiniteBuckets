package net.busybee.InfiniteBuckets.item;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BucketUseListener implements Listener {

    private static final int DRAIN_RADIUS = 3;
    private static final int TARGET_RANGE = 5;

    private final Main plugin;
    private final PlatformScheduler scheduler;
    private final BucketRegistry registry;
    private final MessageManager messages;

    public BucketUseListener(@NotNull Main plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getBucketScheduler().platform();
        this.registry = plugin.getBucketRegistry();
        this.messages = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        EquipmentSlot hand = event.getHand();
        if ((hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND)
                || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) {
            return;
        }

        ItemStack item = event.getItem();
        Optional<BucketTemplate> templateOpt = registry.getTemplate(item);
        if (templateOpt.isEmpty()) return;

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        // Like vanilla, a bucket doesn't stop you opening a chest or door unless you sneak
        if (clicked != null && !player.isSneaking() && isUsableBlock(clicked)) return;

        // The vanilla bucket must never run, or it turns into an empty bucket
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        // The client already predicted the bucket emptying (most visibly in the off hand). Resend the
        // real inventory next tick, after the rest of this click's packets, or it keeps showing an empty bucket.
        scheduler.runAtEntity(player, task -> player.updateInventory());

        BucketTemplate template = templateOpt.get();

        if (template.getPermission() != null && !player.hasPermission(template.getPermission())) {
            messages.send(player, "no-permission-use", Placeholder.parsed("bucket_name", template.getDisplayName()));
            return;
        }

        String denyKey = plugin.getConfigManager().checkWorldRestriction(player.getWorld(), template.getLiquidType());
        if (denyKey != null) {
            messages.send(player, denyKey, Placeholder.parsed("bucket_name", template.getDisplayName()));
            return;
        }

        Block target = template.getMode() == BucketTemplate.BucketMode.DRAIN_AREA
                ? drainCentre(player, clicked)
                : placementTarget(template.getLiquidType(), clicked, event);
        if (target == null) return;

        long generation = plugin.getLifecycle().getGeneration();

        plugin.getDatabaseManager().getCooldown(player.getUniqueId(), template.getId()).thenAccept(expiry -> {
            long now = System.currentTimeMillis();
            if (expiry > now) {
                long remaining = (expiry - now) / 1000 + 1;
                messages.send(player, "cooldown.active",
                        Placeholder.parsed("bucket_name", template.getDisplayName()),
                        Placeholder.unparsed("seconds", String.valueOf(remaining)));
                return;
            }

            scheduler.runAtEntity(player, task -> {
                if (!plugin.getLifecycle().isActive(generation)) return;

                ItemStack current = player.getInventory().getItem(hand);
                if (!isSameTemplate(current, template)) return;

                if (template.getUsageLimit() > 0 && usesLeft(current, template) <= 0) {
                    messages.send(player, "bucket-no-uses", Placeholder.parsed("bucket_name", template.getDisplayName()));
                    return;
                }

                HookManager hooks = plugin.getHookManager();
                scheduler.runAtLocation(target.getLocation(), placeTask -> {
                    boolean done = template.getMode() == BucketTemplate.BucketMode.DRAIN_AREA
                            ? drain(player, template, target, hooks)
                            : place(player, template, target, hooks);
                    if (!done) return;

                    template.getPlaceSound().play(target.getLocation());
                    scheduler.runAtEntity(player, useTask -> consumeUse(player, hand, template, now));
                });
            });
        });
    }

    /** Applies the cooldown and, for limited buckets, takes one use off the bucket in {@code hand}. */
    private void consumeUse(Player player, EquipmentSlot hand, BucketTemplate template, long usedAt) {
        long cooldown = template.getCooldown() > 0 ? template.getCooldown() : plugin.getConfigManager().getGlobalCooldown();
        if (cooldown > 0) {
            plugin.getDatabaseManager().setCooldown(player.getUniqueId(), template.getId(), usedAt + cooldown);
        }

        if (template.getUsageLimit() <= 0) return;

        ItemStack current = player.getInventory().getItem(hand);
        if (!isSameTemplate(current, template)) return;

        int uses = usesLeft(current, template) - 1;
        if (uses <= 0) {
            current.setAmount(current.getAmount() - 1);
            player.getInventory().setItem(hand, current.getAmount() > 0 ? current : null);
            messages.send(player, "bucket-depleted", Placeholder.parsed("bucket_name", template.getDisplayName()));
            return;
        }

        ItemMeta meta = current.getItemMeta();
        meta.getPersistentDataContainer().set(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER, uses);
        current.setItemMeta(meta);
        player.getInventory().setItem(hand, current);
    }

    /** Where the liquid goes: into a waterloggable or replaceable block that was clicked, otherwise against the clicked face. */
    private @Nullable Block placementTarget(Material liquid, @Nullable Block clicked, PlayerInteractEvent event) {
        if (clicked == null) return null;

        if (liquid == Material.WATER && clicked.getBlockData() instanceof Waterlogged waterlogged && !waterlogged.isWaterlogged()) {
            return clicked;
        }
        if (clicked.isReplaceable()) return clicked;
        return clicked.getRelative(event.getBlockFace());
    }

    private @Nullable Block drainCentre(Player player, @Nullable Block clicked) {
        if (clicked != null) return clicked;
        // Clicking a pool surface is an air click; find the liquid the player is looking at
        return player.getTargetBlockExact(TARGET_RANGE, FluidCollisionMode.SOURCE_ONLY);
    }

    private boolean place(Player player, BucketTemplate template, Block target, HookManager hooks) {
        return hooks.canBuild(player, target) && LiquidPlacement.place(target, template.getLiquidType());
    }

    private boolean drain(Player player, BucketTemplate template, Block centre, HookManager hooks) {
        Material liquid = template.getLiquidType();
        int max = plugin.getConfigManager().getMaxDrainBlocks();
        int drained = 0;

        for (int x = -DRAIN_RADIUS; x <= DRAIN_RADIUS && drained < max; x++) {
            for (int y = -DRAIN_RADIUS; y <= DRAIN_RADIUS && drained < max; y++) {
                for (int z = -DRAIN_RADIUS; z <= DRAIN_RADIUS && drained < max; z++) {
                    Block block = centre.getRelative(x, y, z);
                    if (block.getType() != liquid || !Bukkit.isOwnedByCurrentRegion(block)) continue;
                    if (!hooks.canBuild(player, block)) continue;
                    block.setType(Material.AIR);
                    drained++;
                }
            }
        }
        return drained > 0;
    }

    private boolean isSameTemplate(@Nullable ItemStack item, BucketTemplate template) {
        return registry.getTemplate(item).map(t -> t.getId().equals(template.getId())).orElse(false);
    }

    private int usesLeft(ItemStack item, BucketTemplate template) {
        Integer uses = item.getItemMeta().getPersistentDataContainer().get(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER);
        return uses != null ? uses : template.getUsageLimit();
    }

    private static boolean isUsableBlock(Block block) {
        Material type = block.getType();
        return block.getState(false) instanceof InventoryHolder
                || Tag.DOORS.isTagged(type)
                || Tag.TRAPDOORS.isTagged(type) && type != Material.IRON_TRAPDOOR
                || Tag.FENCE_GATES.isTagged(type)
                || Tag.BUTTONS.isTagged(type)
                || Tag.BEDS.isTagged(type)
                || Tag.ANVIL.isTagged(type)
                || type == Material.LEVER
                || type == Material.CRAFTING_TABLE
                || type == Material.ENCHANTING_TABLE;
    }
}
