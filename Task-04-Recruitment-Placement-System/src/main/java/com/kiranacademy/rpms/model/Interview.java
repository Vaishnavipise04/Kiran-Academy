package com.kiranacademy.rpms.model;

import java.time.LocalDateTime;

public record Interview(
        int id,
        int applicationId,
        int roundNo,
        String roundType,
        LocalDateTime scheduledAt,
        String interviewer,
        InterviewMode mode,
        InterviewStatus status,
        LocalDateTime createdAt,
        String candidateName,
        String jobTitle,
        String companyName
) {
}
