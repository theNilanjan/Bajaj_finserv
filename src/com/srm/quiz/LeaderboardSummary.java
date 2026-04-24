package com.srm.quiz;

import java.util.List;

public record LeaderboardSummary(List<LeaderboardEntry> leaderboard, int totalScore, int uniqueEventCount) {
}
