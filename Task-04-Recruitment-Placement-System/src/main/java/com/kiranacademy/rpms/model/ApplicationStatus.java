package com.kiranacademy.rpms.model;

public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    INTERVIEWED,
    SELECTED,
    OFFERED,
    JOINED,
    REJECTED,
    WITHDRAWN,
    ON_HOLD;

    public boolean canTransitionTo(ApplicationStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case APPLIED -> target == SHORTLISTED || target == REJECTED
                    || target == WITHDRAWN || target == ON_HOLD;
            case SHORTLISTED -> target == INTERVIEW_SCHEDULED || target == REJECTED
                    || target == WITHDRAWN || target == ON_HOLD;
            case INTERVIEW_SCHEDULED -> target == INTERVIEWED || target == REJECTED
                    || target == WITHDRAWN || target == ON_HOLD;
            case INTERVIEWED -> target == INTERVIEW_SCHEDULED || target == SELECTED
                    || target == REJECTED || target == WITHDRAWN || target == ON_HOLD;
            case ON_HOLD -> target == SHORTLISTED || target == INTERVIEW_SCHEDULED
                    || target == REJECTED || target == WITHDRAWN;
            case SELECTED -> target == OFFERED || target == REJECTED
                    || target == WITHDRAWN || target == ON_HOLD;
            case OFFERED -> target == JOINED || target == REJECTED || target == WITHDRAWN;
            case JOINED, REJECTED, WITHDRAWN -> false;
        };
    }

    public boolean isTerminal() {
        return this == JOINED || this == REJECTED || this == WITHDRAWN;
    }
}
