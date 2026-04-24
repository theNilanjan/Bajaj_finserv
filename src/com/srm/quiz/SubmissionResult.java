package com.srm.quiz;

public record SubmissionResult(
        boolean isCorrect,
        boolean isIdempotent,
        int submittedTotal,
        int expectedTotal,
        String message
) {
}
