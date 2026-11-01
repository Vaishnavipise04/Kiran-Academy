package com.kiranacademy.rpms.model;

public enum OfferStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    REVOKED,
    EXPIRED;

    public boolean canTransitionTo(OfferStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return this == PENDING && (target == ACCEPTED || target == DECLINED
                || target == REVOKED || target == EXPIRED);
    }
}
