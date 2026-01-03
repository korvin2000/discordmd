package org.korvin;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.korvin.json.Converter;
import org.korvin.json.Discord;
import org.korvin.json.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for converting Discord export JSON payloads into markdown reports.
 */
public final class DiscordMdExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public Discord readDiscord(Path sourcePath) throws IOException {
        String json = Files.readString(sourcePath, StandardCharsets.UTF_8);
        try {
            return Converter.fromJsonString(json);
        } catch (JsonProcessingException parsingException) {
            throw new IOException("Unable to parse Discord export", parsingException);
        }
    }

    public void writeMarkdown(Discord discord, Path destination) throws IOException {
        String markdown = toMarkdown(discord);
        Path parent = destination.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(destination, markdown, StandardCharsets.UTF_8);
    }

    public String toMarkdown(Discord discord) {
        Objects.requireNonNull(discord, "discord");

        String guildName = Optional.ofNullable(discord.getGuild()).map(guild -> guild.getName()).orElse("<unknown>");
        String channelName = Optional.ofNullable(discord.getChannel()).map(channel -> channel.getName()).orElse("<unknown>");
        String exportedAt = Optional.ofNullable(discord.getExportedAt()).orElse("<unknown>");

        StringBuilder builder = new StringBuilder();
        builder.append("# Discord export\n\n");
        builder.append("- **Guild:** ").append(guildName).append('\n');
        builder.append("- **Channel:** ").append(channelName).append('\n');
        builder.append("- **Exported at:** ").append(exportedAt).append("\n\n");

        builder.append("## Messages\n");
        Message[] messages = Optional.ofNullable(discord.getMessages()).orElseGet(() -> new Message[0]);
        Arrays.stream(messages)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(String::compareTo)))
                .forEach(message -> appendMessage(builder, message));
        return builder.toString();
    }

    private void appendMessage(StringBuilder builder, Message message) {
        String id = Optional.ofNullable(message.getID()).orElse("<no id>");
        String timestamp = Optional.ofNullable(message.getTimestamp())
                .map(value -> formatTimestamp(value))
                .orElse("<no timestamp>");
        String author = Optional.ofNullable(message.getAuthor())
                .map(authorValue -> authorValue.getName())
                .orElse("<unknown author>");
        String content = Optional.ofNullable(message.getContent())
                .map(value -> value.strip())
                .filter(value -> !value.isEmpty())
                .orElse("(no content)");

        builder.append("- **ID:** ").append(id)
                .append(" | **Author:** ").append(author)
                .append(" | **Timestamp:** ").append(timestamp)
                .append("\n  \n  > ")
                .append(escapeContent(content))
                .append("\n");
    }

    private String formatTimestamp(String rawTimestamp) {
        try {
            return DATE_TIME_FORMATTER.format(Converter.parseDateTimeString(rawTimestamp));
        } catch (RuntimeException parsingException) {
            return rawTimestamp;
        }
    }

    private String escapeContent(String content) {
        return Arrays.stream(content.split("\n"))
                .map(line -> line.replace("|", "\\|"))
                .collect(Collectors.joining("\n  > "));
    }
}
