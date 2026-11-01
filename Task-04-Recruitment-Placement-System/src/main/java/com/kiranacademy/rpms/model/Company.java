package com.kiranacademy.rpms.model;

import java.time.LocalDateTime;

public record Company(
        int id,
        String name,
        String industry,
        String location,
        String website,
        String contactPerson,
        String contactEmail,
        CompanyStatus status,
        LocalDateTime createdAt
) {
}
