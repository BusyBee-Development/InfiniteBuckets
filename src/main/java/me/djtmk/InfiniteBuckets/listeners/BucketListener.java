package me.djtmk.InfiniteBuckets.listeners;

import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.Query;
import com.hypixel.hytale.component.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.HytaleServer;
import me.djtmk.InfiniteBuckets.InfiniteBuckets;

public class BucketListener extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    private final InfiniteBuckets plugin;

    public BucketListener(InfiniteBuckets plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                       CommandBuffer<EntityStore> commandBuffer, PlaceBlockEvent event) {

        ItemStack item = event.getItemInHand();
        if (item == null) return;

        // Check metadata for the infinite flag
        Boolean isInfinite = item.getFromMetadataOrNull("inf_bucket", HytaleServer.get().getCodecRegistry());

        if (isInfinite != null && isInfinite) {
            // Reset count to 1 so the item is never consumed
            item.setStackCount(1);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        // Query for entities with an inventory
        return Query.builder(EntityStore.class).required(Inventory.class).build();
    }
}
