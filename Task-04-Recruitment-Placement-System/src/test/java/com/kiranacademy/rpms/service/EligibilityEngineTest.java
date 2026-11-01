package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.JobStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityEngineTest {

    private final EligibilityEngine engine = new EligibilityEngine();

    @Test
    void candidateMeetingAllCriteriaIsEligible() {
        EligibilityResult result = engine.check(candidate(2025, "78.50", 0, "Java",
                        "Java, Spring Boot, MySQL", CandidateStatus.ACTIVE),
                job(65, 2024, 0, "B.Tech, BE, MCA", "Java, Spring Boot",
                        LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertTrue(result.eligible());
        assertTrue(result.failures().isEmpty());
    }

    @Test
    void lowPercentageFailsEligibility() {
        EligibilityResult result = engine.check(candidate(2025, "55.00", 0, "Java",
                        "Java", CandidateStatus.ACTIVE),
                job(65, 2024, 0, "B.Tech", "Java", LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertFalse(result.eligible());
        assertTrue(result.failures().stream().anyMatch(reason -> reason.contains("Percentage")));
    }

    @Test
    void excessBacklogsFailEligibility() {
        EligibilityResult result = engine.check(candidate(2025, "75.00", 2, "Java",
                        "Java", CandidateStatus.ACTIVE),
                job(60, 2024, 0, "B.Tech", "Java", LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertFalse(result.eligible());
        assertTrue(result.failures().stream().anyMatch(reason -> reason.contains("Backlogs")));
    }

    @Test
    void qualificationIsMatchedCaseInsensitively() {
        EligibilityResult result = engine.check(candidate(2025, "75.00", 0, "Java",
                        "Java", CandidateStatus.ACTIVE),
                job(60, 2024, 0, "B.Tech/be, mca", "Java",
                        LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertTrue(result.eligible());
    }

    @Test
    void skillOverlapSatisfiesTechnologyRequirement() {
        EligibilityResult result = engine.check(candidate(2025, "75.00", 0, "Testing",
                        "Selenium, Java, JIRA", CandidateStatus.ACTIVE),
                job(60, 2024, 0, "B.Tech", "Java, Selenium",
                        LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertTrue(result.eligible());
    }

    @Test
    void expiredDeadlineFailsEligibility() {
        EligibilityResult result = engine.check(candidate(2025, "75.00", 0, "Java",
                        "Java", CandidateStatus.ACTIVE),
                job(60, 2024, 0, "B.Tech", "Java",
                        LocalDate.now().minusDays(1), JobStatus.OPEN));

        assertFalse(result.eligible());
        assertTrue(result.failures().stream().anyMatch(reason -> reason.contains("deadline")));
    }

    @Test
    void placedCandidateCannotApply() {
        EligibilityResult result = engine.check(candidate(2025, "75.00", 0, "Java",
                        "Java", CandidateStatus.PLACED),
                job(60, 2024, 0, "B.Tech", "Java", LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertFalse(result.eligible());
        assertTrue(result.failures().stream().anyMatch(reason -> reason.contains("PLACED")));
    }

    @Test
    void closedJobFailsEligibility() {
        EligibilityResult result = engine.check(candidate(2025, "75.00", 0, "Java",
                        "Java", CandidateStatus.ACTIVE),
                job(60, 2024, 0, "B.Tech", "Java", LocalDate.now().plusDays(10), JobStatus.CLOSED));

        assertFalse(result.eligible());
        assertTrue(result.failures().stream().anyMatch(reason -> reason.contains("not OPEN")));
    }

    @Test
    void passingYearBelowRequirementFails() {
        EligibilityResult result = engine.check(candidate(2022, "75.00", 0, "Java",
                        "Java", CandidateStatus.ACTIVE),
                job(60, 2024, 0, "B.Tech", "Java", LocalDate.now().plusDays(10), JobStatus.OPEN));

        assertFalse(result.eligible());
        assertTrue(result.failures().stream().anyMatch(reason -> reason.contains("Passing year")));
    }

    private Candidate candidate(int passingYear, String percentage, int backlogs,
                                String technology, String skills, CandidateStatus status) {
        return new Candidate(1, "KA2026001", "Test", "Candidate", "test@example.com",
                "9876543210", "B.Tech", passingYear, new BigDecimal(percentage), technology,
                backlogs, skills, status, LocalDateTime.now());
    }

    private Job job(double minPercentage, int passingYearFrom, int maxBacklogs,
                    String qualification, String technology, LocalDate deadline, JobStatus status) {
        return new Job(1, 1, "Test Company", "Developer", technology, qualification,
                BigDecimal.valueOf(minPercentage), passingYearFrom, maxBacklogs, 3,
                new BigDecimal("7.50"), "Bengaluru", status, deadline, LocalDateTime.now());
    }
}
