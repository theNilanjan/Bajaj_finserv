package com.srm.quiz;

import java.util.List;

public class LocalSmokeTest {
    public static void main(String[] args) {
        List<PollResponse> responses = List.of(
                new PollResponse("2024CS101", "SET_1", 0, List.of(
                        new QuizEvent("R1", "Alice", 10),
                        new QuizEvent("R1", "Bob", 20)
                )),
                new PollResponse("2024CS101", "SET_1", 1, List.of(
                        new QuizEvent("R1", "Alice", 10),
                        new QuizEvent("R2", "Alice", 30),
                        new QuizEvent("R2", "Bob", 40)
                ))
        );

        LeaderboardSummary summary = new LeaderboardService().buildLeaderboard(responses);

        if (summary.totalScore() != 100) {
            throw new AssertionError("Expected total score 100 but found " + summary.totalScore());
        }

        if (summary.uniqueEventCount() != 4) {
            throw new AssertionError("Expected 4 unique events but found " + summary.uniqueEventCount());
        }

        if (!"Bob".equals(summary.leaderboard().get(0).participant())
                || summary.leaderboard().get(0).totalScore() != 60) {
            throw new AssertionError("Expected Bob to lead with 60 points");
        }

        System.out.println("Local smoke test passed.");
    }
}
