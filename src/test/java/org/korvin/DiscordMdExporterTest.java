package org.korvin;

import org.junit.jupiter.api.Test;
import org.korvin.json.Discord;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DiscordMdExporterTest {

    private final DiscordMdExporter exporter = new DiscordMdExporter();

    @Test
    void readsDiscordExportAndBuildsMarkdown() throws Exception {
        Path source = Path.of("src/test/resources/test.json");

        Discord discord = exporter.readDiscord(source);

        assertNotNull(discord, "Discord export should be parsed");
        assertNotNull(discord.getMessages(), "Messages should be present");
        assertTrue(discord.getMessages().length > 0, "At least one message should be parsed");

        String markdown = exporter.toMarkdown(discord);
        assertTrue(markdown.startsWith("---\nschema: chatlog-md-v1"));
        assertTrue(markdown.contains("channel: \"#training\""));
        assertTrue(markdown.contains("channel_id: \"549520096612188181\""));
        assertTrue(markdown.contains("conversation_from: 2025-10-01"));
        assertTrue(markdown.contains("**Platform:** Discord"));

        String expectedHeading = "### m:1422761920947294208 | ts=2025-10-01T03:47:55.592+02:00 | author=Kim | author_id=u:410198342300205066 | type=message";
        assertTrue(markdown.contains(expectedHeading));
        assertTrue(markdown.contains("@umzi i fixed it"));
    }
}
