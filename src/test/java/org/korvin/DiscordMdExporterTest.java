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

        assertTrue(markdown.startsWith("# Discord export"));
        assertTrue(markdown.contains("## Messages"));
        assertTrue(markdown.contains(discord.getMessages()[0].getID()), "First message id should appear in markdown");
    }
}
