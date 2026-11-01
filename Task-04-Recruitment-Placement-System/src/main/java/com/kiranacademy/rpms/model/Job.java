package com.kiranacademy.rpms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Job(
        int id,
        int companyId,
        String companyName,
        String title,
        String technology,
        String qualificationRequired,
        BigDecimal minPercentage,
        int passingYearFrom,
        int maxBacklogs,
        int vacancies,
        BigDecimal packageLpa,
        String location,
        JobStatus status,
        LocalDate applicationDeadline,
        LocalDateTime createdAt
) {
}
