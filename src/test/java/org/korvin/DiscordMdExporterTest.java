package org.korvin;

import org.junit.jupiter.api.Test;
import org.korvin.json.Channel;
import org.korvin.json.Discord;
import org.korvin.json.Message;
import org.korvin.json.MessageAuthor;
import org.korvin.json.Reference;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordMdExporterTest {

    private final DiscordMdExporter exporter = new DiscordMdExporter();

    @Test
    void purifiesMessageContentUsingWordList() {
        Message noisyMessage = message("1", "Lol sounds good to me", "2025-10-01T00:00:00Z");
        Discord discord = discordWithMessages(noisyMessage);

        String markdown = exporter.toMarkdown(discord);

        assertTrue(markdown.contains("to me"), "Cleaned content should remain");
        assertFalse(markdown.contains("Lol sounds good"), "Noise phrases from words list should be removed");
    }

    @Test
    void skipsEmptyUnreferencedMessagesButKeepsReferencedOnes() {
        Message removed = message("skip-me", "lol", "2025-10-01T00:00:00Z");
        Message referenced = message("keep-me", "ok", "2025-10-01T00:01:00Z");
        Message reply = message("reply", "Meaningful reply", "2025-10-01T00:02:00Z");
        Reference reference = new Reference();
        reference.setMessageID("keep-me");
        reply.setReference(reference);

        Discord discord = discordWithMessages(removed, referenced, reply);

        String markdown = exporter.toMarkdown(discord);

        assertFalse(markdown.contains(messageId("skip-me")), "Unreferenced empty messages should be omitted");
        assertTrue(markdown.contains(messageId("keep-me")), "Referenced messages should remain even when empty");
        assertTrue(markdown.contains("Meaningful reply"), "Replies should stay intact");
    }

    private Discord discordWithMessages(Message... messages) {
        Discord discord = new Discord();
        Channel channel = new Channel();
        channel.setName("testing");
        channel.setID("channel-1");
        discord.setChannel(channel);
        discord.setExportedAt(OffsetDateTime.now().toString());
        discord.setMessages(messages);
        discord.setMessageCount(messages.length);
        return discord;
    }

    private Message message(String id, String content, String timestamp) {
        Message message = new Message();
        message.setID(id);
        message.setContent(content);
        message.setTimestamp(timestamp);
        MessageAuthor author = new MessageAuthor();
        author.setName("User " + id);
        message.setAuthor(author);
        return message;
    }

    private String messageId(String rawId) {
        return "m:" + Integer.toHexString(rawId.hashCode());
    }
}
