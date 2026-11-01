package com.kiranacademy.rpms.service;

import java.util.List;

public record EligibilityResult(boolean eligible, List<String> failures) {

    public EligibilityResult {
        failures = List.copyOf(failures);
    }

    public static EligibilityResult pass() {
        return new EligibilityResult(true, List.of());
    }

    public static EligibilityResult fail(List<String> failures) {
        return new EligibilityResult(false, failures);
    }

    public String summary() {
        return eligible ? "ELIGIBLE" : "NOT ELIGIBLE: " + String.join("; ", failures);
    }
}
