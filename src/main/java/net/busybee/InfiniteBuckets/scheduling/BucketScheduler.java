package net.busybee.InfiniteBuckets.scheduling;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.busybee.InfiniteBuckets.Main;
import org.jetbrains.annotations.NotNull;

public final class BucketScheduler {

    private final PlatformScheduler platformScheduler;
    public BucketScheduler(@NotNull Main plugin) {
        this.platformScheduler = new FoliaLib(plugin).getScheduler();
    }
    public PlatformScheduler platform() {
        return platformScheduler;
    }
}
