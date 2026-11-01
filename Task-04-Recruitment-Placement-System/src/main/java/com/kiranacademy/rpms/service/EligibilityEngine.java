package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.JobStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure business rule component. It has no database dependency, which keeps the
 * eligibility logic easy to unit test.
 */
public class EligibilityEngine {

    public EligibilityResult check(Candidate candidate, Job job) {
        List<String> failures = new ArrayList<>();

        if (job.status() != JobStatus.OPEN) {
            failures.add("Job opening is not OPEN (current status: " + job.status() + ")");
        }
        if (job.applicationDeadline() != null
                && job.applicationDeadline().isBefore(LocalDate.now())) {
            failures.add("Application deadline was " + job.applicationDeadline());
        }
        if (candidate.status() != CandidateStatus.ACTIVE) {
            failures.add("Candidate profile is " + candidate.status() + "; only ACTIVE candidates can apply");
        }
        if (job.vacancies() <= 0) {
            failures.add("No vacancies available for this job");
        }
        if (candidate.passingYear() < job.passingYearFrom()) {
            failures.add("Passing year " + candidate.passingYear()
                    + " is below required year " + job.passingYearFrom());
        }
        if (candidate.percentage().compareTo(job.minPercentage()) < 0) {
            failures.add("Percentage " + candidate.percentage()
                    + " is below required minimum " + job.minPercentage());
        }
        if (candidate.backlogs() > job.maxBacklogs()) {
            failures.add("Backlogs " + candidate.backlogs()
                    + " exceed allowed maximum " + job.maxBacklogs());
        }
        if (!qualificationMatches(candidate.qualification(), job.qualificationRequired())) {
            failures.add("Qualification " + candidate.qualification()
                    + " is not accepted for this job (" + job.qualificationRequired() + ")");
        }
        if (!technologyMatches(candidate, job)) {
            failures.add("Candidate technology/skills do not match required technology ("
                    + job.technology() + ")");
        }

        return failures.isEmpty() ? EligibilityResult.pass() : EligibilityResult.fail(failures);
    }

    boolean qualificationMatches(String candidateQualification, String required) {
        if (required == null || required.isBlank() || "ANY".equalsIgnoreCase(required.trim())) {
            return true;
        }
        String candidate = normalize(candidateQualification);
        for (String option : split(required)) {
            if ("ANY".equals(normalize(option)) || candidate.equals(normalize(option))) {
                return true;
            }
        }
        return false;
    }

    boolean technologyMatches(Candidate candidate, Job job) {
        String required = job.technology();
        if (required == null || required.isBlank() || "ANY".equalsIgnoreCase(required.trim())) {
            return true;
        }
        String available = normalize(candidate.technology() + " " + safe(candidate.skills()));
        for (String skill : split(required)) {
            String normalizedSkill = normalize(skill);
            if (!normalizedSkill.isEmpty() && available.contains(normalizedSkill)) {
                return true;
            }
        }
        return false;
    }

    private List<String> split(String csv) {
        List<String> values = new ArrayList<>();
        for (String part : csv.split("[,/|;]")) {
            if (!part.isBlank()) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private String normalize(String value) {
        return safe(value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
