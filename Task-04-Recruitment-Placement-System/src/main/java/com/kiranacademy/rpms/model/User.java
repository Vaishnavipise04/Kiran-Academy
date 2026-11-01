package com.kiranacademy.rpms.model;

import java.time.LocalDateTime;

public record User(
        int id,
        String username,
        String passwordHash,
        String fullName,
        Role role,
        Integer candidateId,
        boolean active,
        LocalDateTime createdAt
) {
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isStaff() {
        return role == Role.ADMIN || role == Role.RECRUITER;
    }
}
