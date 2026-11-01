package com.kiranacademy.rpms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Offer(
        int id,
        int applicationId,
        BigDecimal offeredCtc,
        LocalDate offerDate,
        OfferStatus status,
        LocalDate joiningDate,
        LocalDateTime createdAt,
        String candidateName,
        String jobTitle,
        String companyName
) {
}
