package net.busybee.InfiniteBuckets.inventory.impl;

import fr.mrmicky.fastinv.FastInv;
import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.utils.GUIUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ConfirmDeleteGUI extends FastInv {

    public ConfirmDeleteGUI(BucketTemplate template) {
        this(template, Main.getInstance().getConfigManager().getGuisConfig().getConfigurationSection("guis.confirm-delete"));
    }

    private ConfirmDeleteGUI(BucketTemplate template, ConfigurationSection config) {
        super(config != null ? config.getInt("size", 27) : 27,
                Main.getInstance().getMessageManager().serialize(
                        Main.getInstance().getMessageManager().parse(
                                config != null ? config.getString("title", "Delete?") : "Delete?",
                                Placeholder.parsed("bucket", template.getId()))));

        if (config == null) return;

        ConfigurationSection confirmSec = config.getConfigurationSection("confirm");
        if (confirmSec != null) {
            setItem(confirmSec.getInt("slot"), GUIUtils.createItem(confirmSec), e -> {
                Main.getInstance().getBucketRegistry().unregisterTemplate(template.getId());
                Main.getInstance().getMessageManager().send(e.getWhoClicked(), "gui.status.deleted");
                new BucketListGUI().open((Player) e.getWhoClicked());
            });
        }

        ConfigurationSection cancelSec = config.getConfigurationSection("cancel");
        if (cancelSec != null) {
            setItem(cancelSec.getInt("slot"), GUIUtils.createItem(cancelSec), e -> new BucketListGUI().open((Player) e.getWhoClicked()));
        }
    }
}
