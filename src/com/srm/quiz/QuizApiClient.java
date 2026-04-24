package com.srm.quiz;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuizApiClient {
    private final HttpClient httpClient;
    private final String baseUrl;

    public QuizApiClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = stripTrailingSlash(baseUrl);
    }

    public PollResponse fetchMessages(String regNo, int pollIndex) throws IOException, InterruptedException {
        String url = baseUrl + "/quiz/messages?regNo="
                + URLEncoder.encode(regNo, StandardCharsets.UTF_8)
                + "&poll="
                + pollIndex;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response.statusCode(), response.body());
        return parsePollResponse(response.body());
    }

    public SubmissionResult submitLeaderboard(String regNo, List<LeaderboardEntry> leaderboard)
            throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("regNo", regNo);

        List<Object> leaderboardPayload = new ArrayList<>();
        for (LeaderboardEntry entry : leaderboard) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("participant", entry.participant());
            row.put("totalScore", entry.totalScore());
            leaderboardPayload.add(row);
        }

        payload.put("leaderboard", leaderboardPayload);

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/quiz/submit"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(SimpleJson.stringify(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response.statusCode(), response.body());
        return parseSubmissionResult(response.body());
    }

    private PollResponse parsePollResponse(String jsonText) {
        Map<String, Object> data = SimpleJson.asObject(SimpleJson.parse(jsonText));
        List<QuizEvent> events = new ArrayList<>();

        for (Object item : SimpleJson.asArray(data.get("events"))) {
            Map<String, Object> eventObject = SimpleJson.asObject(item);
            events.add(new QuizEvent(
                    String.valueOf(eventObject.get("roundId")),
                    String.valueOf(eventObject.get("participant")),
                    toInt(eventObject.get("score"))
            ));
        }

        return new PollResponse(
                String.valueOf(data.get("regNo")),
                String.valueOf(data.get("setId")),
                toInt(data.get("pollIndex")),
                events
        );
    }

    private SubmissionResult parseSubmissionResult(String jsonText) {
        Map<String, Object> data = SimpleJson.asObject(SimpleJson.parse(jsonText));
        return new SubmissionResult(
                toBoolean(data.get("isCorrect")),
                toBoolean(data.get("isIdempotent")),
                toOptionalInt(data.get("submittedTotal")),
                toOptionalInt(data.get("expectedTotal")),
                data.get("message") == null ? "" : String.valueOf(data.get("message"))
        );
    }

    private void ensureSuccess(int statusCode, String body) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        throw new IllegalStateException("API call failed with status " + statusCode + ": " + body);
    }

    private String stripTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private int toOptionalInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value);
        if (text.isBlank() || "null".equalsIgnoreCase(text)) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
