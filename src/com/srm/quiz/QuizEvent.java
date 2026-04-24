package com.srm.quiz;

public record QuizEvent(String roundId, String participant, int score) {
    public String uniqueKey() {
        return roundId + "|" + participant;
    }
}
