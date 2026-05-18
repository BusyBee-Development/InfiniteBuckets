package net.busybee.InfiniteBuckets.item;

import com.cryptomorin.xseries.XSound;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.database.DatabaseManager;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.utils.DebugLogger;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class ItemEvents implements Listener {

    private final Main plugin;
    private final PlatformScheduler scheduler;
    private final BucketRegistry registry;
    private final MessageManager messages;
    private final DebugLogger debugLogger;
    private final HookManager hookManager;

    public ItemEvents(@NotNull Main plugin) {
        this.plugin = plugin;
        this.scheduler = Main.scheduler();
        this.registry = plugin.getBucketRegistry();
        this.messages = plugin.getMessageManager();
        this.debugLogger = plugin.getDebugLogger();
        this.hookManager = plugin.getHookManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        if (registry.getTemplate(event.getMainHandItem()).isPresent() || registry.getTemplate(event.getOffHandItem()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !(event.getClickedInventory() instanceof PlayerInventory)) return;
        if (event.getSlot() == 40 && registry.getTemplate(event.getCursor()).isPresent()) {
            event.setCancelled(true);
            return;
        }
        if (event.isShiftClick() && registry.getTemplate(event.getCurrentItem()).isPresent()) {
            if (player.getInventory().getItemInOffHand().getType() == Material.AIR) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) return;

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
                ItemStack currentItem = player.getInventory().getItemInMainHand();
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

                boolean success;
                if (template.getMode() == BucketTemplate.BucketMode.DRAIN_AREA) {
                    success = handleDrainArea(player, template, event);
                } else {
                    success = handleVanillaLike(player, template, event);
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
                            messages.send(player, "bucket-depleted", Placeholder.parsed("bucket_name", template.getDisplayName()));
                        } else {
                            meta.getPersistentDataContainer().set(BucketFactory.USES_REMAINING_KEY, PersistentDataType.INTEGER, uses);
                            currentItem.setItemMeta(meta);
                        }
                    }
                }
            });
        });
    }

    private boolean handleVanillaLike(Player player, BucketTemplate template, PlayerInteractEvent event) {
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

    private boolean handleDrainArea(Player player, BucketTemplate template, PlayerInteractEvent event) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFill(PlayerBucketFillEvent event) {
        if (registry.getTemplate(event.getItemStack()).isPresent()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEmpty(PlayerBucketEmptyEvent event) {
        if (registry.getTemplate(event.getItemStack()).isPresent()) event.setCancelled(true);
    }
}
