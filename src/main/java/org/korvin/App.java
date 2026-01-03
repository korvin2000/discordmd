package org.korvin;

import org.korvin.json.Discord;
import org.korvin.json.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class App {
    private static final int EXPECTED_ARGUMENT_COUNT = 2;

    private App() {
    }

    public static void main(String[] args) {
        CliArguments cliArguments = CliArguments.parse(args);
        if (cliArguments == null) {
            printUsage();
            return;
        }

        DiscordMdExporter exporter = new DiscordMdExporter();

        try {
            Discord discord = exporter.readDiscord(cliArguments.source());
            describe(discord);
            exporter.writeMarkdown(discord, cliArguments.destination());
            System.out.printf("Markdown report written to %s%n", cliArguments.destination().toAbsolutePath());
        } catch (IOException exception) {
            System.err.printf("%s%n", exception.getMessage());
            System.exit(1);
        }
    }

    private static void describe(Discord discord) {
        Objects.requireNonNull(discord, "discord");
        System.out.println("Parsed Discord export");
        System.out.printf("Guild: %s%n", Optional.ofNullable(discord.getGuild()).map(guild -> guild.getName()).orElse("<unknown>"));
        System.out.printf("Channel: %s%n", Optional.ofNullable(discord.getChannel()).map(channel -> channel.getName()).orElse("<unknown>"));
        System.out.printf("Messages: %d%n", Optional.ofNullable(discord.getMessages()).map(messages -> messages.length).orElse(0));

        Message[] messages = Optional.ofNullable(discord.getMessages()).orElse(new Message[0]);
        if (messages.length == 0) {
            System.out.println("No messages found.");
            return;
        }

        System.out.println("\nMessages:");
        Arrays.stream(messages)
                .filter(Objects::nonNull)
                .forEach(App::printMessageSummary);
    }

    private static void printMessageSummary(Message message) {
        String id = Optional.ofNullable(message.getID()).orElse("<no id>");
        String content = Optional.ofNullable(message.getContent()).map(String::trim).filter(value -> !value.isEmpty()).orElse("(no content)");
        System.out.printf("- %s | %s%n", id, content);
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar discordmd.jar <source-json> <destination-markdown>");
    }

    private record CliArguments(Path source, Path destination) {
        private static CliArguments parse(String[] args) {
            if (args.length != EXPECTED_ARGUMENT_COUNT) {
                return null;
            }

            Path source = Path.of(args[0]);
            if (!Files.exists(source)) {
                System.err.printf("Source file %s does not exist.%n", source.toAbsolutePath());
                return null;
            }

            Path destination = Path.of(args[1]);
            return new CliArguments(source, destination);
        }
    }
}
