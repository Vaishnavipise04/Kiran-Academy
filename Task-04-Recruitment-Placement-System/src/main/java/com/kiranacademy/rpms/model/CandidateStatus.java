package com.kiranacademy.rpms.model;

public enum CandidateStatus {
    ACTIVE,
    PLACED,
    BLOCKED,
    INACTIVE;

    public boolean canTransitionTo(CandidateStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case ACTIVE -> target == PLACED || target == BLOCKED || target == INACTIVE;
            case BLOCKED, INACTIVE -> target == ACTIVE;
            case PLACED -> false;
        };
    }
}
