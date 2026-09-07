package net.busybee.InfiniteBuckets.item;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class BucketUseListener implements Listener {

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

        event.setCancelled(true);
        Player player = event.getPlayer();
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

                ItemStack currentItem = player.getInventory().getItem(hand);
                if (!registry.getTemplate(currentItem).map(t -> t.getId().equals(template.getId())).orElse(false)) return;

                ItemMeta meta = currentItem.getItemMeta();
                Integer uses = null;
                if (template.getUsageLimit() > 0) {
                    uses = meta.getPersistentDataContainer().get(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER);
                    if (uses == null) uses = template.getUsageLimit();
                    if (uses <= 0) {
                        messages.send(player, "bucket-no-uses", Placeholder.parsed("bucket_name", template.getDisplayName()));
                        return;
                    }
                }

                HookManager hookManager = plugin.getHookManager();
                boolean success;
                if (template.getMode() == BucketTemplate.BucketMode.DRAIN_AREA) {
                    success = handleDrainArea(player, template, event, hookManager);
                } else {
                    success = handleVanillaLike(player, template, event, hookManager);
                }

                if (success) {
                    long bucketCooldown = template.getCooldown();
                    long cooldownToApply = bucketCooldown > 0 ? bucketCooldown : plugin.getConfigManager().getGlobalCooldown();

                    if (cooldownToApply > 0) {
                        plugin.getDatabaseManager().setCooldown(player.getUniqueId(), template.getId(), now + cooldownToApply);
                    }

                    if (uses != null) {
                        uses--;
                        if (uses <= 0) {
                            currentItem.setAmount(currentItem.getAmount() - 1);
                            player.getInventory().setItem(hand, currentItem);
                            messages.send(player, "bucket-depleted", Placeholder.parsed("bucket_name", template.getDisplayName()));
                        } else {
                            meta.getPersistentDataContainer().set(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER, uses);
                            currentItem.setItemMeta(meta);
                            player.getInventory().setItem(hand, currentItem);
                        }
                    }
                }
            });
        });
    }

    private boolean handleVanillaLike(Player player, BucketTemplate template, PlayerInteractEvent event, HookManager hookManager) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return false;

        Block targetBlock = clickedBlock.getRelative(event.getBlockFace());
        if (!hookManager.canBuild(player, targetBlock)) return false;

        scheduler.runAtLocation(targetBlock.getLocation(), task -> {
            targetBlock.setType(template.getLiquidType());
            template.getPlaceSound().play(player);
        });
        return true;
    }

    private boolean handleDrainArea(Player player, BucketTemplate template, PlayerInteractEvent event, HookManager hookManager) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return false;

        int radius = 3;
        scheduler.runAtLocation(clickedBlock.getLocation(), task -> {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block b = clickedBlock.getRelative(x, y, z);
                        if (b.getType() == template.getLiquidType() && hookManager.canBuild(player, b)) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
            template.getPlaceSound().play(player);
        });
        return true;
    }
}
