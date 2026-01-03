package org.korvin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        Greeting greeting = new Greeting("Hello, World!", Instant.now());
        System.out.println(mapGreeting(mapper, greeting));

        if (args.length == 0) {
            System.out.println("Provide a JSON file path as the first argument to inspect its contents.");
            return;
        }

        Path jsonPath = Path.of(args[0]);
        if (!Files.exists(jsonPath)) {
            System.err.printf("No file found at %s%n", jsonPath.toAbsolutePath());
            return;
        }

        try {
            JsonNode tree = mapper.readTree(jsonPath.toFile());
            System.out.printf("Loaded JSON from %s:%n%s%n", jsonPath.toAbsolutePath(), mapper.writeValueAsString(tree));
        } catch (IOException ioException) {
            System.err.printf("Unable to read JSON from %s: %s%n", jsonPath.toAbsolutePath(), ioException.getMessage());
        }
    }

    private static String mapGreeting(ObjectMapper mapper, Greeting greeting) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", greeting.message());
        payload.put("timestamp", greeting.timestamp().toString());

        try {
            return mapper.writeValueAsString(payload);
        } catch (IOException ioException) {
            return greeting.message();
        }
    }

    private record Greeting(String message, Instant timestamp) {
    }
}
