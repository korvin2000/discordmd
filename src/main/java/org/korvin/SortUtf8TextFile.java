package org.korvin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Collator;
import java.util.*;


public final class SortUtf8TextFile {
    public static void main(String[] args) throws Exception {


        //Path in = Paths.get("C:\\workspace\\codex\\discordmd\\src\\main\\resources\\filterplus.txt");
        Path out = Paths.get("C:\\workspace\\codex\\discordmd\\src\\main\\resources\\filter-all.txt");

        Collator collator = Collator.getInstance(Locale.ROOT);
        collator.setStrength(Collator.PRIMARY);

        Comparator<String> byLenThenName = (a, b) -> {
            int la = a.length();
            int lb = b.length();
            if (la != lb) return Integer.compare(la, lb);
            int c = collator.compare(a, b);
            if (c != 0) return c;
            return a.compareTo(b);
        };

        NavigableSet<String> lines = new TreeSet<>(byLenThenName);

        readFile(Paths.get("C:\\workspace\\codex\\discordmd\\src\\main\\resources\\filter.txt"), lines);
        readFile(Paths.get("C:\\workspace\\codex\\discordmd\\src\\main\\resources\\filterplus2.txt"), lines);


        // Collections.sort(lines); // alphabetic (lexicographic). For case-insensitive: Collections.sort(lines, String.CASE_INSENSITIVE_ORDER);


        try (BufferedWriter bw = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (String s : lines) {
                bw.write(s);
                bw.newLine();
            }
        }
    }

    private static void readFile(Path in, NavigableSet<String> lines) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(in, StandardCharsets.UTF_8)) {
            for (String line; (line = br.readLine()) != null; ) {
                String s = line.trim(); // remove surrounding whitespace; drop this if you want exact preservation
                if (!s.isEmpty()) lines.add(s); // skip empty lines
            }
        }
    }
}