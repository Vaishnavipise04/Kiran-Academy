package com.kiranacademy.rpms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Placement(
        int id,
        int candidateId,
        int companyId,
        int jobId,
        int offerId,
        LocalDate placementDate,
        BigDecimal packageLpa,
        PlacementStatus status,
        LocalDateTime createdAt,
        String candidateName,
        String companyName,
        String jobTitle
) {
}
