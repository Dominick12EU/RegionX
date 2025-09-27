package it.dominick.regionx.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String placeholder(String message, String... placeholders) {
        String modifiedMessage = message;
        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                String placeholder = placeholders[i];
                String replacement = placeholders[i + 1];
                modifiedMessage = modifiedMessage.replace(placeholder, replacement);
            }
        } else {
            return null;
        }

        return modifiedMessage;
    }

    public static void send(Player player, String message, String... placeholders) {
        String modifiedMessage = placeholder(message, placeholders);
        send(player, modifiedMessage);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static List<String> color(List<String> messages) {
        return messages.stream()
                .map(ChatUtils::color)
                .collect(Collectors.toList());
    }

    public static String color(final String message) {
        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher hexMatcher = HEX_PATTERN.matcher(message);
        final StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);

        while (hexMatcher.find()) {
            final String group = hexMatcher.group(1);

            hexMatcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        final String partiallyTranslated = hexMatcher.appendTail(buffer).toString();

        return ChatColor.translateAlternateColorCodes('&', partiallyTranslated);
    }

    public static void error(Throwable throwable, String... customMessage) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        String errorMessage = sw.toString();

        String customMsg = (customMessage.length > 0) ? customMessage[0] : "No additional info provided.";

        String formattedError = "=========== ERROR ===========\n" +
                "Message: " + throwable.getMessage() + "\n" +
                "Cause: " + (throwable.getCause() != null ? throwable.getCause().toString() : "None") + "\n" +
                "Info: " + customMsg + "\n" +
                "=============================\n" +
                errorMessage;

        Bukkit.getLogger().severe(formattedError);
    }
}
