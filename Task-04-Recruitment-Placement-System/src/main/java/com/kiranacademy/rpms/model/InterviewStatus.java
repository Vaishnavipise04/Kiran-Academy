package com.kiranacademy.rpms.model;

public enum InterviewStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    public boolean canTransitionTo(InterviewStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return this == SCHEDULED
                && (target == COMPLETED || target == CANCELLED || target == NO_SHOW);
    }
}
