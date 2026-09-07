package net.busybee.InfiniteBuckets.utils;

import net.busybee.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MessageManager {

    private final Main plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration messagesConfig;
    private Component prefix;

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }

    public MessageManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.loadMessages();
    }

    public void reload() {
        this.loadMessages();
    }

    private void loadMessages() {
        this.messagesConfig = plugin.getConfigManager().getMessagesConfig();

        String prefixString = messagesConfig.getString("plugin-prefix", "<gold><b>InfiniteBuckets</b></gold> <dark_gray>»</dark_gray> ");
        this.prefix = miniMessage.deserialize(legacyToMiniMessage(prefixString));
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull TagResolver... placeholders) {
        String messageStr = messagesConfig.getString(key, "<red>Unknown message key: " + key + "</red>");
        if (messageStr == null || messageStr.trim().isEmpty()) {
            return;
        }
        Component message = miniMessage.deserialize(legacyToMiniMessage(messageStr), placeholders);
        sender.sendMessage(prefix.append(message));
    }

    public void sendRaw(@NotNull CommandSender sender, @NotNull String key) {
        List<String> messageLines = messagesConfig.getStringList(key);
        for (String line : messageLines) {
            sender.sendMessage(miniMessage.deserialize(legacyToMiniMessage(line)));
        }
    }

    public Component parse(@NotNull String text, @NotNull TagResolver... placeholders) {
        return miniMessage.deserialize(legacyToMiniMessage(text), placeholders);
    }

    public List<Component> parse(@NotNull List<String> lines, @NotNull TagResolver... placeholders) {
        return lines.stream().map(line -> miniMessage.deserialize(legacyToMiniMessage(line), placeholders)).toList();
    }

    public static String legacyToMiniMessage(String text) {
        if (text == null) return "";
        text = text.replace("§", "&");
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = chars[i + 1];

                // Hex support: &#RRGGBB
                if (code == '#' && i + 7 < chars.length) {
                    String hex = text.substring(i + 2, i + 8);
                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        result.append("<color:#").append(hex).append(">");
                        i += 7;
                        continue;
                    }
                }

                // Standard legacy codes mapping
                String mini = switch (Character.toLowerCase(code)) {
                    case '0' -> "<black>"; case '1' -> "<dark_blue>"; case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>"; case '4' -> "<dark_red>"; case '5' -> "<dark_purple>";
                    case '6' -> "<gold>"; case '7' -> "<gray>"; case '8' -> "<dark_gray>";
                    case '9' -> "<blue>"; case 'a' -> "<green>"; case 'b' -> "<aqua>";
                    case 'c' -> "<red>"; case 'd' -> "<light_purple>"; case 'e' -> "<yellow>";
                    case 'f' -> "<white>"; case 'k' -> "<obfuscated>"; case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>"; case 'n' -> "<underlined>"; case 'o' -> "<italic>";
                    case 'r' -> "<reset>"; default -> null;
                };
                if (mini != null) {
                    result.append(mini);
                    i++;
                    continue;
                }
            }
            result.append(chars[i]);
        }
        return result.toString();
    }

    public String serialize(@NotNull Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
