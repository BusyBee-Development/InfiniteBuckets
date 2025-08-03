package me.djtmk.InfiniteBuckets.commands;

import co.aikar.commands.PaperCommandManager;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.item.InfiniteBucket;
import java.util.stream.Collectors;

public final class CommandManager {

    public CommandManager(Main plugin) {
        PaperCommandManager manager = new PaperCommandManager(plugin);

        manager.getCommandCompletions().registerAsyncCompletion("buckets", c ->
                plugin.getBucketRegistry().getRegisteredBuckets().stream()
                        .map(InfiniteBucket::id)
                        .collect(Collectors.toList())
        );

        manager.getCommandContexts().registerContext(InfiniteBucket.class, c -> {
            String bucketId = c.popFirstArg();
            return plugin.getBucketRegistry().getBucket(bucketId)
                    .orElseThrow(() -> new co.aikar.commands.InvalidCommandArgument("Invalid bucket type specified."));
        });

        manager.registerCommand(new InfiniteBucketsCommand(plugin));
    }
}
