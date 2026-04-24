package com.srm.quiz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuizLeaderboardApp {
    private static final String DEFAULT_BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final long DEFAULT_DELAY_MS = 5000L;

    public static void main(String[] args) throws Exception {
        Map<String, String> options = parseArgs(args);
        String regNo = options.get("regNo");

        if (regNo == null || regNo.isBlank()) {
            printUsage();
            return;
        }

        String baseUrl = options.getOrDefault("baseUrl", DEFAULT_BASE_URL);
        long delayMs = Long.parseLong(options.getOrDefault("delayMs", String.valueOf(DEFAULT_DELAY_MS)));
        boolean skipSubmit = Boolean.parseBoolean(options.getOrDefault("skipSubmit", "false"));

        QuizApiClient apiClient = new QuizApiClient(baseUrl);
        LeaderboardService leaderboardService = new LeaderboardService();
        List<PollResponse> responses = new ArrayList<>();

        Path outputDirectory = Path.of("output");
        Files.createDirectories(outputDirectory);
        Path auditFile = outputDirectory.resolve("poll-audit.log");
        Files.deleteIfExists(auditFile);

        for (int pollIndex = 0; pollIndex < 10; pollIndex++) {
            PollResponse response = apiClient.fetchMessages(regNo, pollIndex);
            responses.add(response);
            appendAuditLine(auditFile, "Fetched poll " + pollIndex + " with " + response.events().size() + " events");

            if (pollIndex < 9) {
                Thread.sleep(delayMs);
            }
        }

        LeaderboardSummary summary = leaderboardService.buildLeaderboard(responses);
        printSummary(summary);
        writeLeaderboardFile(outputDirectory.resolve("leaderboard.json"), regNo, summary);

        if (skipSubmit) {
            System.out.println("Submission skipped because --skip-submit=true");
            return;
        }

        SubmissionResult result = apiClient.submitLeaderboard(regNo, summary.leaderboard());
        writeSubmissionFile(outputDirectory.resolve("submission-response.json"), result);
        printSubmissionResult(result);
    }

    private static void writeLeaderboardFile(Path file, String regNo, LeaderboardSummary summary) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("regNo", regNo);
        payload.put("totalScore", summary.totalScore());
        payload.put("uniqueEventCount", summary.uniqueEventCount());

        List<Object> rows = new ArrayList<>();
        for (LeaderboardEntry entry : summary.leaderboard()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("participant", entry.participant());
            row.put("totalScore", entry.totalScore());
            rows.add(row);
        }

        payload.put("leaderboard", rows);
        Files.writeString(file, SimpleJson.stringify(payload));
    }

    private static void writeSubmissionFile(Path file, SubmissionResult result) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("isCorrect", result.isCorrect());
        payload.put("isIdempotent", result.isIdempotent());
        payload.put("submittedTotal", result.submittedTotal());
        payload.put("expectedTotal", result.expectedTotal());
        payload.put("message", result.message());
        Files.writeString(file, SimpleJson.stringify(payload));
    }

    private static void appendAuditLine(Path file, String message) throws IOException {
        String line = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " - "
                + message
                + System.lineSeparator();
        Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void printSummary(LeaderboardSummary summary) {
        System.out.println("Leaderboard");
        for (int index = 0; index < summary.leaderboard().size(); index++) {
            LeaderboardEntry entry = summary.leaderboard().get(index);
            System.out.printf("%d. %s - %d%n", index + 1, entry.participant(), entry.totalScore());
        }
        System.out.println("Unique events counted: " + summary.uniqueEventCount());
        System.out.println("Total score: " + summary.totalScore());
    }

    private static void printSubmissionResult(SubmissionResult result) {
        System.out.println("Submission response");
        System.out.println("isCorrect: " + result.isCorrect());
        System.out.println("isIdempotent: " + result.isIdempotent());
        System.out.println("submittedTotal: " + result.submittedTotal());
        System.out.println("expectedTotal: " + result.expectedTotal());
        System.out.println("message: " + result.message());
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();

        for (String arg : args) {
            if (!arg.startsWith("--") || !arg.contains("=")) {
                continue;
            }
            String[] parts = arg.substring(2).split("=", 2);
            options.put(parts[0], parts[1]);
        }

        return options;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("java -cp out com.srm.quiz.QuizLeaderboardApp --regNo=YOUR_REG_NO");
        System.out.println("Optional arguments:");
        System.out.println("--baseUrl=https://devapigw.vidalhealthtpa.com/srm-quiz-task");
        System.out.println("--delayMs=5000");
        System.out.println("--skipSubmit=true");
    }
}
