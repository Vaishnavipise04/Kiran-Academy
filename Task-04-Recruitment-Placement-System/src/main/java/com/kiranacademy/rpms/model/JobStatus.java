package com.kiranacademy.rpms.model;

public enum JobStatus {
    OPEN,
    ON_HOLD,
    CLOSED;

    public boolean canTransitionTo(JobStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case OPEN -> target == ON_HOLD || target == CLOSED;
            case ON_HOLD -> target == OPEN || target == CLOSED;
            case CLOSED -> false;
        };
    }
}
