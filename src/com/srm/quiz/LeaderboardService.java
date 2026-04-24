package com.srm.quiz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LeaderboardService {
    public LeaderboardSummary buildLeaderboard(List<PollResponse> responses) {
        Set<String> seenEvents = new HashSet<>();
        Map<String, Integer> participantScores = new HashMap<>();

        for (PollResponse response : responses) {
            for (QuizEvent event : response.events()) {
                if (!seenEvents.add(event.uniqueKey())) {
                    continue;
                }

                participantScores.merge(event.participant(), event.score(), Integer::sum);
            }
        }

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        int totalScore = 0;

        for (Map.Entry<String, Integer> entry : participantScores.entrySet()) {
            leaderboard.add(new LeaderboardEntry(entry.getKey(), entry.getValue()));
            totalScore += entry.getValue();
        }

        leaderboard.sort(Comparator
                .comparingInt(LeaderboardEntry::totalScore)
                .reversed()
                .thenComparing(LeaderboardEntry::participant));

        return new LeaderboardSummary(leaderboard, totalScore, seenEvents.size());
    }
}
