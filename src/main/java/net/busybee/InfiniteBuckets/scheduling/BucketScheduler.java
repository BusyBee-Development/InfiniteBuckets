package net.busybee.InfiniteBuckets.scheduling;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.Main;
import org.jetbrains.annotations.NotNull;

/**
 * Single owner of Folia/Paper region-scheduler access for the plugin.
 * Every listener that needs to hop onto the right thread for an entity or
 * location goes through this class instead of touching FoliaLib directly,
 * so a future scheduler API change only needs to be made here.
 */
public final class BucketScheduler {

    private final PlatformScheduler platformScheduler;

    public BucketScheduler(@NotNull Main plugin) {
        this.platformScheduler = new FoliaLib(plugin).getScheduler();
    }

    public PlatformScheduler platform() {
        return platformScheduler;
    }
}
