package me.djtmk.InfiniteBuckets.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for string operations, particularly for color code formatting.
 */
public class StringUtils {

    /**
     * Pattern for matching hex color codes in the format #RRGGBB
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    /**
     * Formats a string by converting color codes.
     * Supports both traditional Minecraft color codes with '&' prefix
     * and hex color codes in the format #RRGGBB.
     *
     * @param string The string to format
     * @return The formatted string with color codes converted
     */
    public static String format(String string) {
        if (string == null) return "";

        Matcher match = HEX_PATTERN.matcher(string);
        while (match.find()) {
            String color = string.substring(match.start(), match.end());
            string = string.replace(color, ChatColor.of(color) + "");
            match = HEX_PATTERN.matcher(string);
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Formats a colored message using color codes.
     * This is a convenience method for formatting messages with color codes.
     * Example: color("&cThis is red text")
     *
     * @param message The message to format with color codes
     * @return The formatted message with color codes converted
     */
    public static String color(String message) {
        return format(message);
    }
}
