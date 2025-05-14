package me.djtmk.InfiniteBuckets.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String format(String string) {
        if (string == null) return "";
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(string);
        while (match.find()) {
            String color = string.substring(match.start(), match.end());
            string = string.replace(color, ChatColor.of(color) + "");
            match = pattern.matcher(string);
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', string);
    }
}
