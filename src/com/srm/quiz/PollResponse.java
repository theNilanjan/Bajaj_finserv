package com.srm.quiz;

import java.util.List;

public record PollResponse(String regNo, String setId, int pollIndex, List<QuizEvent> events) {
}
