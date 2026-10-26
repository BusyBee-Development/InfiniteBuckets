package net.busybee.InfiniteBuckets.item;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.hooks.HookManager;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public final class BucketProtectionListener implements Listener {

    // Only blocks that can fire a bucket. Hoppers just carry items, so shop chests and storage can still be fed.
    private static final Set<InventoryType> AUTOMATION_INVENTORIES = EnumSet.of(
            InventoryType.DISPENSER, InventoryType.DROPPER);

    private final Main plugin;
    private final BucketRegistry registry;
    private final MessageManager messages;

    public BucketProtectionListener(@NotNull Main plugin) {
        this.plugin = plugin;
        this.registry = plugin.getBucketRegistry();
        this.messages = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDispense(@NotNull BlockDispenseEvent event) {
        registry.getTemplate(event.getItem()).ifPresent(template -> {
            // Always cancel: the vanilla dispenser would swap the infinite bucket for an empty one
            event.setCancelled(true);
            if (!isAutomationAllowed(template) || template.getMode() != BucketTemplate.BucketMode.VANILLA_LIKE) return;

            Block dispenser = event.getBlock();
            Block target = dispenser.getBlockData() instanceof Directional directional
                    ? dispenser.getRelative(directional.getFacing())
                    : dispenser;

            String denyKey = plugin.getConfigManager().checkWorldRestriction(dispenser.getWorld(), template.getLiquidType());
            HookManager hookManager = plugin.getHookManager();
            if (denyKey != null || !hookManager.canBuild(target)) return;

            plugin.getBucketScheduler().platform().runAtLocation(target.getLocation(), task -> {
                Material replacedType = target.getType();
                BlockData replacedData = target.getBlockData();
                if (LiquidPlacement.place(target, template.getLiquidType())) {
                    // CoreProtect's own name for dispenser actions
                    hookManager.logLiquidPlaced("#dispenser", target, replacedType, replacedData, template.getLiquidType());
                    template.getPlaceSound().play(target.getLocation());
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHopperMove(@NotNull InventoryMoveItemEvent event) {
        if (!AUTOMATION_INVENTORIES.contains(event.getDestination().getType())) return;
        registry.getTemplate(event.getItem()).ifPresent(template -> {
            if (!isAutomationAllowed(template)) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clicked = event.getClickedInventory();

        if (clicked != null && AUTOMATION_INVENTORIES.contains(clicked.getType())) {
            registry.getTemplate(event.getCursor()).ifPresent(template -> denyAutomationTransfer(event, player, template));
            return;
        }

        if (event.isShiftClick() && clicked instanceof PlayerInventory) {
            Inventory top = event.getView().getTopInventory();
            if (AUTOMATION_INVENTORIES.contains(top.getType())) {
                registry.getTemplate(event.getCurrentItem()).ifPresent(template -> denyAutomationTransfer(event, player, template));
            }
        }
    }

    private void denyAutomationTransfer(InventoryClickEvent event, Player player, BucketTemplate template) {
        if (isAutomationAllowed(template)) return;

        event.setCancelled(true);
        if (template.getUsageLimit() > 0) {
            messages.send(player, "dispenser-no-limited-uses");
        }
    }

    private boolean isAutomationAllowed(BucketTemplate template) {
        return template.isAllowAutomation() && template.getUsageLimit() <= 0;
    }
}
