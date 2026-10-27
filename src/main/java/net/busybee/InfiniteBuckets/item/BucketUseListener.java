package net.busybee.InfiniteBuckets.item;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.hooks.protectionhook.ProtectionHook;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BucketUseListener implements Listener {

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

        boolean drainMode = template.getMode() == BucketTemplate.BucketMode.DRAIN_AREA;
        boolean placeBlock = drainMode && template.isPlaceBlock();
        Block target = placeBlock ? blockTarget(player, clicked, event)
                : drainMode ? drainCentre(player, clicked)
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

                // Placing a block counts as one use; the block keeps the rest and gives them back when broken
                int usesAfterPlace = template.getUsageLimit() > 0 ? usesLeft(current, template) - 1 : -1;

                HookManager hooks = plugin.getHookManager();
                scheduler.runAtLocation(target.getLocation(), placeTask -> {
                    boolean done = placeBlock ? placeBlock(player, template, target, hooks, usesAfterPlace)
                            : drainMode ? drain(player, template, target, hooks)
                            : place(player, template, target, hooks);
                    if (!done) return;

                    template.getPlaceSound().play(target.getLocation());
                    scheduler.runAtEntity(player, useTask -> {
                        if (placeBlock) consumeItem(player, hand, template, now, usesAfterPlace == 0);
                        else consumeUse(player, hand, template, now);
                    });
                });
            });
        });
    }

    /** Applies the cooldown and, for limited buckets, takes one use off the bucket in {@code hand}. */
    private void consumeUse(Player player, EquipmentSlot hand, BucketTemplate template, long usedAt) {
        applyCooldown(player, template, usedAt);

        if (template.getUsageLimit() <= 0) return;

        ItemStack current = player.getInventory().getItem(hand);
        if (!isSameTemplate(current, template)) return;

        int uses = usesLeft(current, template) - 1;
        if (uses <= 0) {
            // Only the item that was used runs out; the rest of a stack keeps its own uses
            current.setAmount(current.getAmount() - 1);
            player.getInventory().setItem(hand, current.getAmount() > 0 ? current : null);
            messages.send(player, "bucket-depleted", Placeholder.parsed("bucket_name", template.getDisplayName()));
            return;
        }

        // Every item in a stack shares the stack's counter, so split the used one off before counting down
        ItemStack rest = null;
        if (current.getAmount() > 1) {
            rest = current.clone();
            rest.setAmount(current.getAmount() - 1);
            current.setAmount(1);
        }

        ItemMeta meta = current.getItemMeta();
        meta.getPersistentDataContainer().set(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER, uses);
        current.setItemMeta(meta);
        player.getInventory().setItem(hand, current);

        // The used one stays in the hand so the next click keeps using it; the rest moves into the inventory
        if (rest != null) {
            for (ItemStack overflow : player.getInventory().addItem(rest).values()) {
                player.getWorld().dropItem(player.getLocation(), overflow);
            }
        }
    }

    /** Applies the cooldown and takes the placed item out of {@code hand}, like placing a vanilla sponge. */
    private void consumeItem(Player player, EquipmentSlot hand, BucketTemplate template, long usedAt, boolean lastUse) {
        applyCooldown(player, template, usedAt);

        ItemStack current = player.getInventory().getItem(hand);
        if (!isSameTemplate(current, template)) return;
        current.setAmount(current.getAmount() - 1);
        player.getInventory().setItem(hand, current.getAmount() > 0 ? current : null);
        // The block breaks into a plain one after its last use
        if (lastUse) messages.send(player, "bucket-depleted", Placeholder.parsed("bucket_name", template.getDisplayName()));
    }

    private void applyCooldown(Player player, BucketTemplate template, long usedAt) {
        long cooldown = template.getCooldown() > 0 ? template.getCooldown() : plugin.getConfigManager().getGlobalCooldown();
        if (cooldown > 0) {
            plugin.getDatabaseManager().setCooldown(player.getUniqueId(), template.getId(), usedAt + cooldown);
        }
    }

    /** Where a placed block goes: into a replaceable block that was clicked, otherwise against the clicked face. */
    private @Nullable Block blockTarget(Player player, @Nullable Block clicked, PlayerInteractEvent event) {
        // Clicking a lava or water surface is an air click; put the block into the source being looked at
        if (clicked == null) return player.getTargetBlockExact(TARGET_RANGE, FluidCollisionMode.SOURCE_ONLY);
        return clicked.isReplaceable() ? clicked : clicked.getRelative(event.getBlockFace());
    }

    /** Puts the bucket's block down like a vanilla sponge, then drains the liquid around it. */
    private boolean placeBlock(Player player, BucketTemplate template, Block target, HookManager hooks, int usesAfterPlace) {
        if (!target.isReplaceable() || !hooks.canBuild(player, target, ProtectionHook.Action.PLACE)) return false;
        // Like vanilla, never place a block inside a mob or player
        if (!target.getWorld().getNearbyEntities(BoundingBox.of(target), e -> e instanceof LivingEntity).isEmpty()) return false;

        // Drain before placing: each drained block updates its neighbours, and a vanilla sponge already
        // in place would wake up, soak up the water itself (skipping the protection checks) and turn wet
        drain(player, template, target, hooks);

        Material blockType = placedBlockType(template);
        Material replacedType = target.getType();
        BlockData replacedData = target.getBlockData();
        // No physics, for the same reason
        target.setType(blockType, false);
        hooks.logLiquidPlaced(player.getName(), target, replacedType, replacedData, blockType);
        // Remember it so breaking it gives the bucket back; with no uses left it breaks into a plain block
        if (usesAfterPlace != 0) plugin.getPlacedBucketBlocks().mark(target, template.getId(), usesAfterPlace);
        else plugin.getPlacedBucketBlocks().clear(target);
        return true;
    }

    /** The icon if it is a full block (a sponge, say), otherwise a sponge. */
    private static Material placedBlockType(BucketTemplate template) {
        Material icon = template.getIcon() != null ? template.getIcon().get() : null;
        return icon != null && icon.isBlock() && icon.isSolid() ? icon : Material.SPONGE;
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
        if (!hooks.canBuild(player, target, ProtectionHook.Action.PLACE)) return false;

        Material replacedType = target.getType();
        BlockData replacedData = target.getBlockData();
        if (!LiquidPlacement.place(target, template.getLiquidType())) return false;

        hooks.logLiquidPlaced(player.getName(), target, replacedType, replacedData, template.getLiquidType());
        return true;
    }

    private boolean drain(Player player, BucketTemplate template, Block centre, HookManager hooks) {
        Material liquid = template.getLiquidType();
        int radius = template.getDrainRadius();
        int limit = plugin.getConfigManager().getDrainBlockLimit();
        int max = limit > 0 ? limit : Integer.MAX_VALUE;
        int drained = 0;

        // Work outward one shell at a time, so when drain-block-limit cuts it short the blocks nearest the click go first
        for (int d = 0; d <= radius && drained < max; d++) {
            for (int x = -d; x <= d && drained < max; x++) {
                for (int y = -d; y <= d && drained < max; y++) {
                    for (int z = -d; z <= d && drained < max; z++) {
                        if (Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))) != d) continue;
                        Block block = centre.getRelative(x, y, z);
                        if (block.getType() != liquid || !Bukkit.isOwnedByCurrentRegion(block)) continue;
                        if (!hooks.canBuild(player, block, ProtectionHook.Action.BREAK)) continue;
                        hooks.logLiquidRemoved(player.getName(), block);
                        block.setType(Material.AIR);
                        drained++;
                    }
                }
            }
        }
        return drained > 0;
    }

    private boolean isSameTemplate(@Nullable ItemStack item, BucketTemplate template) {
        return registry.getTemplate(item).map(t -> t.getId().equals(template.getId())).orElse(false);
    }

    private int usesLeft(ItemStack item, BucketTemplate template) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        Integer uses = container.get(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER);
        if (uses == null) {
            // Old items count down in their own key; -1 there meant unlimited
            Integer legacy = container.get(BucketFactory.LEGACY_USES_KEY, PersistentDataType.INTEGER);
            if (legacy != null && legacy >= 0) uses = legacy;
        }
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
