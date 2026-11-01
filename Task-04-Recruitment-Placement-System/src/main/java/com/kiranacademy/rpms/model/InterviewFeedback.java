package com.kiranacademy.rpms.model;

import java.time.LocalDateTime;

public record InterviewFeedback(
        int id,
        int interviewId,
        int technicalRating,
        int communicationRating,
        int attitudeRating,
        String comments,
        InterviewResult result,
        String createdBy,
        LocalDateTime createdAt
) {
}
