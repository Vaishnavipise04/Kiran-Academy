package com.kiranacademy.rpms.model;

import java.time.LocalDateTime;

public record Application(
        int id,
        int candidateId,
        int jobId,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        String candidateName,
        String jobTitle,
        String companyName
) {
}
