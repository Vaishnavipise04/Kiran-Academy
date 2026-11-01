package com.kiranacademy.rpms.exception;

import java.util.List;

public class EligibilityException extends RPMSException {

    private final List<String> reasons;

    public EligibilityException(List<String> reasons) {
        super("Candidate is not eligible for this job: " + String.join("; ", reasons));
        this.reasons = List.copyOf(reasons);
    }

    public List<String> getReasons() {
        return reasons;
    }
}
