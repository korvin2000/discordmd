package org.korvin;

import org.korvin.json.Discord;
import org.korvin.json.Message;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans Discord export JSON files in a directory to find frequently recurring long messages.
 */
public final class MessageFrequencyAnalyzer {

    private static final int MIN_CONTENT_LENGTH = 180;
    private static final int RESULT_LIMIT = 256+64;

    private final DiscordMdExporter exporter;

    public MessageFrequencyAnalyzer() {
        this(new DiscordMdExporter());
    }

    MessageFrequencyAnalyzer(DiscordMdExporter exporter) {
        this.exporter = Objects.requireNonNull(exporter, "exporter");
    }

    /**
     * Parses all JSON files under the provided directory, counting message contents longer than {@value #MIN_CONTENT_LENGTH} characters.
     *
     * @param directory directory containing Discord export JSON files
     * @return a map keyed by message content with its occurrence count as value
     * @throws IOException if any JSON file cannot be read or parsed
     */
    public Map<String, Integer> countLongMessages(Path directory) throws IOException {
        Objects.requireNonNull(directory, "directory");
        if (!Files.isDirectory(directory)) {
            throw new IOException("Provided path is not a directory: " + directory.toAbsolutePath());
        }

        Map<String, Integer> occurrences = new HashMap<>();

        try (Stream<Path> files = Files.walk(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .forEach(path -> accumulate(path, occurrences));
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }

        return occurrences;
    }

    int x = 499;

    /**
     * Displays the most frequent long messages (top {@value #RESULT_LIMIT}) to standard output.
     *
     * @param directory directory containing Discord export JSON files
     * @return ordered list of map entries for the most common messages
     * @throws IOException if any JSON file cannot be read or parsed
     */
    public List<Map.Entry<String, Integer>> printTopMessages(Path directory) throws IOException {
        List<Map.Entry<String, Integer>> topMessages = topMessages(directory);
        try (PrintStream out = new PrintStream(
                new FileOutputStream("a:\\output.txt"),
                false, // autoFlush (only relevant for println/printf on certain calls)
                StandardCharsets.UTF_8
        )) {
            for (Map.Entry<String, Integer> entry : topMessages) {
                x++;
            String s = String.format("%s,%s,%s,phrase,other,safe", x, entry.getKey(), entry.getValue());
//            System.out.println(s);
                out.println(s);
                System.out.printf("%d,%s%n,%d,phrase,other,safe", x, entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, Integer> entry : topMessages) {
            x++;
//            String s = String.format("%s,%s,%s%n,phrase,other,safe", x, entry.getKey(), entry.getValue());
//            System.out.println(s);
            System.out.printf("%d,%s%n,%d,phrase,other,safe", x, entry.getKey(), entry.getValue());
        }
        return topMessages;
    }

    List<Map.Entry<String, Integer>> topMessages(Path directory) throws IOException {
        Map<String, Integer> occurrences = countLongMessages(directory);

        return occurrences.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(RESULT_LIMIT)
                .collect(Collectors.toList());
    }

    private void accumulate(Path jsonFile, Map<String, Integer> occurrences) {
        try {
            Discord discord = exporter.readDiscord(jsonFile);
            Message[] messages = Optional.ofNullable(discord.getMessages()).orElse(new Message[0]);
            Arrays.stream(messages)
                    .filter(Objects::nonNull)
                    .map(Message::getContent)
                    .map(content -> content != null ? content.strip() : null)
                    .filter(content -> content != null && content.length() < MIN_CONTENT_LENGTH)
                    .forEach(content -> occurrences.merge(content, 1, Integer::sum));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed processing " + jsonFile.toAbsolutePath(), exception);
        }
    }

    public static void main(String[] args) {
        Path path = Path.of("C:\\workspace\\codex\\discord\\training\\");
        MessageFrequencyAnalyzer mfa = new MessageFrequencyAnalyzer();
        try {

            List<Map.Entry<String, Integer>> a = mfa.printTopMessages(path);
            System.out.println(a.size());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}