package org.korvin;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.korvin.json.Attachment;
import org.korvin.json.Channel;
import org.korvin.json.Converter;
import org.korvin.json.Discord;
import org.korvin.json.Message;
import org.korvin.json.MessageAuthor;
import org.korvin.json.Reaction;
import org.korvin.json.Reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * Utility class for converting Discord export JSON payloads into markdown reports.
 */
public final class DiscordMdExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> REMOVAL_TERMS = loadRemovalTerms();

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

        Message[] messages = Optional.ofNullable(discord.getMessages()).orElseGet(() -> new Message[0]);
        List<Message> sortedMessages = Arrays.stream(messages)
                .filter(Objects::nonNull)
                .filter(msg -> !Optional.ofNullable(msg.getAuthor()).orElse(new MessageAuthor()).getIsBot())
                .sorted(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(String::compareTo)))
                .toList();

        Metadata metadata = Metadata.from(discord, sortedMessages);
        Map<String, String> contentById = buildContentIndex(sortedMessages);
        Set<String> referencedIds = collectReferencedIds(sortedMessages);

        StringBuilder builder = new StringBuilder();
        appendFrontMatter(builder, metadata);
        appendHeader(builder, metadata);
        sortedMessages.forEach(message -> appendMessage(builder, message, contentById, referencedIds));
        return builder.toString();
    }

    private Map<String, String> buildContentIndex(List<Message> messages) {
        Map<String, String> contentById = new HashMap<>();
        messages.forEach(message -> Optional.ofNullable(message.getID())
                .filter(id -> !contentById.containsKey(id))
                .ifPresent(id -> contentById.put(id, normalizedContent(message))));
        return contentById;
    }

    private void appendFrontMatter(StringBuilder builder, Metadata metadata) {
        builder.append("---\n")
                .append("schema: chatlog-md-v1\n")
                .append("platform: discord\n")
                .append("channel: \"").append(metadata.channel()).append("\"\n")
//                .append("channel_id: \"").append(metadata.channelId()).append("\"\n")
                .append("conversation_from: ").append(metadata.conversationFrom()).append('\n')
                .append("conversation_to: ").append(metadata.conversationTo()).append('\n')
//                .append("timezone: \"").append(metadata.timezone()).append("\"\n")
                .append("exported_at: ").append(metadata.exportedAt()).append('\n')
//                .append("id_namespace: \"discord\"\n")
                .append("---\n\n");
    }

    private void appendHeader(StringBuilder builder, Metadata metadata) {
        builder.append("# ").append(metadata.channel()).append("\n\n")
                .append("**Platform:** Discord\n")
                .append("**Date range:** ")
                .append(metadata.conversationFrom()).append(" — ")
                .append(metadata.conversationTo()).append(' ')
                .append('(').append(metadata.timezone()).append(')')
                .append("\n\n");
    }

    private void appendMessage(StringBuilder builder, Message message, Map<String, String> contentById, Set<String> referencedIds) {
        String content = normalizedContent(message);
        boolean hasContent = !content.isEmpty();
        boolean hasAttachments = message.getAttachments() != null && message.getAttachments().length > 0;
        boolean isReferenced = referencedIds.contains(message.getID());
        if (!hasContent && !hasAttachments && !isReferenced) {
            return;
        }

        List<String> headingParts = new ArrayList<>();
        String messageId = formatMessageId(message.getID());
        headingParts.add(messageId);
        headingParts.add("ts=" + formatTimestamp(message.getTimestamp()));
        headingParts.add("user=" + sanitizeHeadingValue(resolveAuthorName(message)));
        //resolveAuthorId(message).ifPresent(authorId -> headingParts.add("author_id=" + authorId));

        boolean isReply = message.getReference() != null;
        //headingParts.add("type=" + (isReply ? "reply" : "message"));
        resolveReplyTarget(message).ifPresent(replyId -> headingParts.add("reply_to=" + replyId));
        //resolveEditedTimestamp(message).ifPresent(edited -> headingParts.add("edited=" + edited));

        builder.append("### ").append(String.join("|", headingParts)).append("\n");

        String replyTargetId = resolveReplyTarget(message).orElse(null);
        if (replyTargetId != null) {
            appendReplyContext(builder, replyTargetId, contentById);
        }

        if (hasContent) {
            builder.append(content);
        }
        builder.append("\n");
        appendAttachments(builder, message.getAttachments());
        //appendReactions(builder, message.getReactions());
        builder.append('\n');
    }

    private void appendReplyContext(StringBuilder builder, String replyId, Map<String, String> contentById) {
        builder.append("> **Replying to:** ").append(replyId).append("\n");
        Optional.ofNullable(contentById.get(stripMessagePrefix(replyId)))
                .map(this::excerpt)
                .filter(excerpt -> !excerpt.isEmpty())
                .ifPresent(excerpt -> builder.append("> **Quote:**\" ")
                        .append(excerpt)
                        .append("\"\n"));
        builder.append('\n');
    }

    private void appendAttachments(StringBuilder builder, Attachment[] attachments) {
        if (attachments == null || attachments.length == 0) {
            return;
        }

        for (Attachment attachment : attachments) {
            if (checkAttachment(attachment)) {
                continue;
            }
            builder.append("- attachment: name=")
                    .append(Optional.ofNullable(attachment.getFileName()).orElse("<unknown>"))
                    .append(" size=").append(humanReadableSize(attachment.getFileSizeBytes()));
            Optional.ofNullable(attachment.getURL()).ifPresent(url -> builder.append(" url=").append(url));
            builder.append("\n");
        }
    }

    private boolean checkAttachment(Attachment attachment) {
        if (null == attachment) return true;
        String name = attachment.getFileName().toLowerCase();
        if (name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(".jpg")
           || name.endsWith(".jpeg") || name.endsWith(".mp4") || name.endsWith(".wmv")
           || name.endsWith(".avi") || name.endsWith(".mkv") || name.endsWith(".webm")
           || name.endsWith(".asf") || name.endsWith(".avif")|| name.endsWith(".jxl")) {
            return true;
        }
        return false;
    }

    private void appendReactions(StringBuilder builder, Reaction[] reactions) {
        if (reactions == null || reactions.length == 0) {
            return;
        }

        String formatted = Arrays.stream(reactions)
                .filter(Objects::nonNull)
                .map(this::formatReaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(" "));

        if (!formatted.isEmpty()) {
            builder.append("- reactions: ").append(formatted).append("\n");
        }
    }

    private Optional<String> formatReaction(Reaction reaction) {
        if (reaction.getEmoji() == null || reaction.getCount() <= 0) {
            return Optional.empty();
        }
        String emojiLabel = Optional.ofNullable(reaction.getEmoji().getName())
                .orElse(Optional.ofNullable(reaction.getEmoji().getCode()).orElse("?"));
        return Optional.of('"' + emojiLabel + '"' + '=' + reaction.getCount());
    }

    private String resolveAuthorName(Message message) {
        MessageAuthor author = message.getAuthor();
        if (null == author) return "";
        String name = StringUtils.defaultIfBlank(author.getName(), "");
        String nick = StringUtils.defaultIfBlank(author.getNickname(), "");
        if (name.length() < nick.length() && name.length() > 0) return name;
        if (nick.length() < name.length() && nick.length() > 0) return nick;
        if (nick.length()>0) return nick;
        if (name.length()>0) return name;
        return "any";
    }

    private Optional<String> resolveAuthorId(Message message) {
        return Optional.ofNullable(message.getAuthor())
                .map(MessageAuthor::getID)
                .filter(id -> !id.isBlank())
                .map(id -> "u:" + id);
    }

    private Optional<String> resolveReplyTarget(Message message) {
        return Optional.ofNullable(message.getReference())
                .map(Reference::getMessageID)
                .map(this::formatMessageId);
    }

    private Optional<String> resolveEditedTimestamp(Message message) {
        return Optional.ofNullable(message.getTimestampEdited())
                .map(this::formatTimestamp);
    }

    private String normalizedContent(Message message) {
        String rawContent = Optional.ofNullable(message.getContent()).orElse("");
        String purified = purifyContent(rawContent);
        return purified.isBlank() ? "" : purified.strip();
    }

    private String purifyContent(String content) {
        String cleaned = content;
        for (String term : REMOVAL_TERMS) {
            if (term.isEmpty()) {
                continue;
            }
            cleaned = cleaned.replaceAll("(?i)" + removalPattern(term), "");
        }

        cleaned = cleaned.replaceAll("[\\t ]{2,}", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s+$", "");
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        return cleaned.strip();
    }

    private String removalPattern(String term) {
        String escaped = Pattern.quote(term);
        boolean startsWithWord = Character.isLetterOrDigit(term.codePointAt(0));
        boolean endsWithWord = Character.isLetterOrDigit(term.codePointBefore(term.length()));

        if (startsWithWord) {
            escaped = "\\b" + escaped;
        }
        if (endsWithWord) {
            escaped = escaped + "\\b";
        }
        return escaped;
    }

    private String formatTimestamp(String rawTimestamp) {
        return parseTimestamp(rawTimestamp)
                .map(DATE_TIME_FORMATTER::format)
                .orElse(rawTimestamp);
    }

    private String formatMessageId(String rawId) {
        return "m:" + shortId(Optional.ofNullable(rawId).orElse("unknown"));
    }

    private String shortId(String x) {
        return Integer.toHexString(x.hashCode());
    }

    private Set<String> collectReferencedIds(List<Message> messages) {
        return messages.stream()
                .map(Message::getReference)
                .filter(Objects::nonNull)
                .map(Reference::getMessageID)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String stripMessagePrefix(String messageId) {
        if (messageId == null) {
            return null;
        }
        return messageId.startsWith("m:") ? messageId.substring(2) : messageId;
    }

    private String excerpt(String content) {
        String singleLine = Arrays.stream(content.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining(" "));
        return singleLine.length() <= 70 ? singleLine : singleLine.substring(0, 65) + "..";
    }

    private String humanReadableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f%s", bytes / Math.pow(1024, exp), unit);
    }

    private String sanitizeHeadingValue(String value) {
        return value.replace("|", "/");
    }

    private static List<String> loadRemovalTerms() {
        try (InputStream inputStream = DiscordMdExporter.class.getResourceAsStream("/words.txt")) {
            if (inputStream == null) {
                return List.of();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
            }
        } catch (IOException ignored) {
            return List.of();
        }
    }

    private record Metadata(String channel, String channelId, String conversationFrom, String conversationTo, String timezone,
                            String exportedAt) {
        private static Metadata from(Discord discord, List<Message> messages) {
            String channelName = Optional.ofNullable(discord.getChannel())
                    .map(Channel::getName)
                    .filter(name -> !name.isBlank())
                    .map(name -> "#" + name)
                    .orElse("#<unknown>");
            String channelId = Optional.ofNullable(discord.getChannel())
                    .map(Channel::getID)
                    .orElse("<unknown>");

            OffsetDateTime from = resolveDate(discord.getDateRange() != null ? discord.getDateRange().getAfter() : null)
                    .orElseGet(() -> earliest(messages));
            OffsetDateTime to = resolveDate(discord.getDateRange() != null ? discord.getDateRange().getBefore() : null)
                    .orElseGet(() -> latest(messages, from));
            String timezone = Optional.ofNullable(from)
                    .or(() -> Optional.ofNullable(to))
                    .map(OffsetDateTime::getOffset)
                    .map(Object::toString)
                    .orElse("UTC");
            String exportedAt = resolveDate(discord.getExportedAt()).map(DATE_TIME_FORMATTER::format)
                    .orElse(Optional.ofNullable(discord.getExportedAt()).orElse("<unknown>"));

            String fromText = Optional.ofNullable(from).map(value -> value.toLocalDate().toString()).orElse("<unknown>");
            String toText = Optional.ofNullable(to).map(value -> value.toLocalDate().toString()).orElse("<unknown>");
            return new Metadata(channelName, channelId, fromText, toText, timezone, exportedAt);
        }

        private static OffsetDateTime earliest(List<Message> messages) {
            return messages.stream()
                    .map(Message::getTimestamp)
                    .map(Metadata::resolveDate)
                    .flatMap(Optional::stream)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
        }

        private static OffsetDateTime latest(List<Message> messages, OffsetDateTime fallback) {
            return messages.stream()
                    .map(Message::getTimestamp)
                    .map(Metadata::resolveDate)
                    .flatMap(Optional::stream)
                    .max(Comparator.naturalOrder())
                    .orElse(fallback);
        }

        private static Optional<OffsetDateTime> resolveDate(String raw) {
            return parseTimestamp(raw);
        }
    }

    private static Optional<OffsetDateTime> parseTimestamp(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(OffsetDateTime.parse(raw));
        } catch (RuntimeException ignored) {
            // fallback to permissive parser below
        }

        try {
            return Optional.of(Converter.parseDateTimeString(raw));
        } catch (RuntimeException parsingException) {
            return Optional.empty();
        }
    }
}
