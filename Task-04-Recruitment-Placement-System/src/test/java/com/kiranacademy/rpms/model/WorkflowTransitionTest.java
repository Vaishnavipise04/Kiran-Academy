package com.kiranacademy.rpms.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowTransitionTest {

    @Test
    void applicationHappyPathTransitionsAreAllowed() {
        assertTrue(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.SHORTLISTED));
        assertTrue(ApplicationStatus.SHORTLISTED.canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED));
        assertTrue(ApplicationStatus.INTERVIEW_SCHEDULED.canTransitionTo(ApplicationStatus.INTERVIEWED));
        assertTrue(ApplicationStatus.INTERVIEWED.canTransitionTo(ApplicationStatus.SELECTED));
        assertTrue(ApplicationStatus.SELECTED.canTransitionTo(ApplicationStatus.OFFERED));
        assertTrue(ApplicationStatus.OFFERED.canTransitionTo(ApplicationStatus.JOINED));
    }

    @Test
    void applicationCannotSkipMandatoryStages() {
        assertFalse(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.INTERVIEWED));
        assertFalse(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.JOINED));
        assertFalse(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.SELECTED));
        assertFalse(ApplicationStatus.SHORTLISTED.canTransitionTo(ApplicationStatus.SELECTED));
        assertFalse(ApplicationStatus.SELECTED.canTransitionTo(ApplicationStatus.JOINED));
    }

    @Test
    void terminalApplicationStatusesCannotChange() {
        assertFalse(ApplicationStatus.JOINED.canTransitionTo(ApplicationStatus.REJECTED));
        assertFalse(ApplicationStatus.REJECTED.canTransitionTo(ApplicationStatus.SHORTLISTED));
        assertFalse(ApplicationStatus.WITHDRAWN.canTransitionTo(ApplicationStatus.APPLIED));
        assertTrue(ApplicationStatus.JOINED.isTerminal());
    }

    @Test
    void interviewCanHaveAnotherRoundAfterFeedback() {
        assertTrue(ApplicationStatus.INTERVIEWED.canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED));
    }

    @Test
    void offerTransitionsAreRestricted() {
        assertTrue(OfferStatus.PENDING.canTransitionTo(OfferStatus.ACCEPTED));
        assertTrue(OfferStatus.PENDING.canTransitionTo(OfferStatus.DECLINED));
        assertTrue(OfferStatus.PENDING.canTransitionTo(OfferStatus.REVOKED));
        assertFalse(OfferStatus.ACCEPTED.canTransitionTo(OfferStatus.DECLINED));
        assertFalse(OfferStatus.REVOKED.canTransitionTo(OfferStatus.ACCEPTED));
    }

    @Test
    void interviewTransitionsAreRestricted() {
        assertTrue(InterviewStatus.SCHEDULED.canTransitionTo(InterviewStatus.COMPLETED));
        assertTrue(InterviewStatus.SCHEDULED.canTransitionTo(InterviewStatus.CANCELLED));
        assertFalse(InterviewStatus.COMPLETED.canTransitionTo(InterviewStatus.CANCELLED));
    }

    @Test
    void jobAndCandidateTransitionsAreRestricted() {
        assertTrue(JobStatus.OPEN.canTransitionTo(JobStatus.CLOSED));
        assertTrue(JobStatus.ON_HOLD.canTransitionTo(JobStatus.OPEN));
        assertFalse(JobStatus.CLOSED.canTransitionTo(JobStatus.OPEN));

        assertTrue(CandidateStatus.ACTIVE.canTransitionTo(CandidateStatus.PLACED));
        assertTrue(CandidateStatus.BLOCKED.canTransitionTo(CandidateStatus.ACTIVE));
        assertFalse(CandidateStatus.PLACED.canTransitionTo(CandidateStatus.ACTIVE));
    }
}
