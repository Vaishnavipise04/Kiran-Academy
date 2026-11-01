package com.kiranacademy.rpms.ui;

import com.kiranacademy.rpms.config.ApplicationContext;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.Offer;
import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.model.User;
import com.kiranacademy.rpms.service.CandidateService;
import com.kiranacademy.rpms.utility.InputUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CandidateMenu {

    private final ApplicationContext context;
    private final User user;
    private final InputUtil input;
    private final int candidateId;

    public CandidateMenu(ApplicationContext context, User user, InputUtil input) {
        this.context = context;
        this.user = user;
        this.input = input;
        this.candidateId = user.candidateId() == null ? -1 : user.candidateId();
    }

    public void show() {
        if (candidateId <= 0) {
            ConsoleView.error("This candidate account is not linked to a candidate profile.");
            input.pause();
            return;
        }
        while (true) {
            ConsoleView.title("Candidate portal - " + user.fullName());
            ConsoleView.menuOption(1, "Browse open job openings");
            ConsoleView.menuOption(2, "Check eligibility for a job");
            ConsoleView.menuOption(3, "Apply for a job");
            ConsoleView.menuOption(4, "My applications");
            ConsoleView.menuOption(5, "Withdraw an application");
            ConsoleView.menuOption(6, "My interviews");
            ConsoleView.menuOption(7, "My interview feedback");
            ConsoleView.menuOption(8, "My offers");
            ConsoleView.menuOption(9, "Respond to an offer");
            ConsoleView.menuOption(10, "My placement");
            ConsoleView.menuOption(11, "Update my profile");
            ConsoleView.menuOption(12, "Change password");
            ConsoleView.menuOption(0, "Logout");
            int choice = input.readInt("Select option: ", 0, 12);
            try {
                switch (choice) {
                    case 1 -> ConsoleView.jobs(context.jobService.findAll(true));
                    case 2 -> checkEligibility();
                    case 3 -> apply();
                    case 4 -> ConsoleView.applications(
                            context.applicationService.findForCandidate(candidateId));
                    case 5 -> withdraw();
                    case 6 -> ConsoleView.interviews(context.interviewService.findForCandidate(candidateId));
                    case 7 -> viewFeedback();
                    case 8 -> ConsoleView.offers(context.offerService.findForCandidate(candidateId));
                    case 9 -> respondToOffer();
                    case 10 -> viewPlacement();
                    case 11 -> updateProfile();
                    case 12 -> changePassword();
                    case 0 -> {
                        return;
                    }
                    default -> ConsoleView.warn("Invalid option");
                }
            } catch (RPMSException e) {
                ConsoleView.error(e.getMessage());
            }
            input.pause();
        }
    }

    private void checkEligibility() throws RPMSException {
        int jobId = input.readInt("Job id: ", 1, Integer.MAX_VALUE);
        ConsoleView.jobDetails(context.jobService.findById(jobId));
        ConsoleView.eligibility(context.applicationService.checkEligibility(candidateId, jobId));
    }

    private void apply() throws RPMSException {
        int jobId = input.readInt("Job id: ", 1, Integer.MAX_VALUE);
        ConsoleView.jobDetails(context.jobService.findById(jobId));
        ConsoleView.eligibility(context.applicationService.checkEligibility(candidateId, jobId));
        if (!input.readYesNo("Proceed with the application?")) {
            return;
        }
        Application application = context.applicationService.apply(candidateId, jobId, user.username());
        ConsoleView.success("Application #" + application.id() + " submitted with status "
                + application.status());
        ConsoleView.applicationDetails(application);
    }

    private void withdraw() throws RPMSException {
        ConsoleView.applications(context.applicationService.findForCandidate(candidateId));
        int applicationId = input.readInt("Application id to withdraw: ", 1, Integer.MAX_VALUE);
        Application application = ownApplication(applicationId);
        ConsoleView.applicationDetails(application);
        if (!input.readYesNo("Withdraw this application?")) {
            return;
        }
        Application withdrawn = context.applicationService.withdraw(applicationId, candidateId,
                user.username());
        ConsoleView.success("Application #" + withdrawn.id() + " is now " + withdrawn.status());
    }

    private void viewFeedback() throws RPMSException {
        List<Application> applications = context.applicationService.findForCandidate(candidateId);
        ConsoleView.applications(applications);
        int applicationId = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
        ownApplication(applicationId);
        ConsoleView.feedbackList(context.interviewService.findFeedbackForApplication(applicationId));
    }

    private void respondToOffer() throws RPMSException {
        List<Offer> offers = context.offerService.findForCandidate(candidateId);
        ConsoleView.offers(offers);
        if (offers.isEmpty()) {
            return;
        }
        int offerId = input.readInt("Offer id: ", 1, Integer.MAX_VALUE);
        Offer offer = context.offerService.findById(offerId);
        ConsoleView.offerDetails(offer);
        boolean accept = input.readYesNo("Accept this offer? (n = decline)");
        Offer updated = context.offerService.respond(offerId, accept, candidateId, user.username());
        ConsoleView.success("Offer #" + updated.id() + " is now " + updated.status());
        if (accept) {
            ConsoleView.info("Joining date: " + updated.joiningDate()
                    + ". The recruiter will record your joining to complete the placement.");
        }
    }

    private void viewPlacement() throws RPMSException {
        try {
            Placement placement = context.placementService.findByCandidate(candidateId);
            ConsoleView.placements(List.of(placement));
        } catch (EntityNotFoundException e) {
            ConsoleView.info("No placement has been recorded for your profile yet.");
        }
    }

    private void updateProfile() throws RPMSException {
        Candidate existing = context.candidateService.findById(candidateId);
        ConsoleView.candidateDetails(existing);
        String first = input.readOptional("First name", existing.firstName());
        String last = input.readOptional("Last name", existing.lastName());
        String email = input.readOptional("Email", existing.email());
        String phone = input.readOptional("Phone", existing.phone());
        String qualification = input.readOptional("Qualification", existing.qualification());
        int passingYear = input.readInt("Passing year", 1980, LocalDate.now().getYear() + 1,
                existing.passingYear());
        BigDecimal percentage = input.readDecimal("Percentage", 0, 100, existing.percentage());
        String technology = input.readOptional("Primary technology", existing.technology());
        int backlogs = input.readInt("Active backlogs", 0, 100, existing.backlogs());
        String skills = input.readOptional("Skills", existing.skills());

        CandidateService.CandidateInput candidateInput = new CandidateService.CandidateInput(
                first, last, email, phone, qualification, passingYear, percentage, technology,
                backlogs, skills);
        Candidate updated = context.candidateService.update(candidateId, candidateInput,
                user.username());
        ConsoleView.success("Profile updated for " + updated.registrationNo());
    }

    private void changePassword() throws RPMSException {
        ConsoleView.section("Change password");
        String current = input.readRequired("Current password: ");
        String next = input.readRequired("New password (min 6 characters): ");
        String confirm = input.readRequired("Confirm new password: ");
        if (!next.equals(confirm)) {
            ConsoleView.error("New password and confirmation do not match");
            return;
        }
        context.authService.changePassword(user, current, next);
        ConsoleView.success("Password changed successfully");
    }

    private Application ownApplication(int applicationId) throws RPMSException {
        Application application = context.applicationService.findById(applicationId);
        if (application.candidateId() != candidateId) {
            throw new EntityNotFoundException("Application for this candidate", applicationId);
        }
        return application;
    }
}
