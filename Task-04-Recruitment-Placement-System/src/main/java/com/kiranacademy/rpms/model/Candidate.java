package com.kiranacademy.rpms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Candidate(
        int id,
        String registrationNo,
        String firstName,
        String lastName,
        String email,
        String phone,
        String qualification,
        int passingYear,
        BigDecimal percentage,
        String technology,
        int backlogs,
        String skills,
        CandidateStatus status,
        LocalDateTime createdAt
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
