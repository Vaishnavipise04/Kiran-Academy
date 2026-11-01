package com.kiranacademy.rpms.ui;

import com.kiranacademy.rpms.config.ApplicationContext;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.ApplicationStatus;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.model.Company;
import com.kiranacademy.rpms.model.CompanyStatus;
import com.kiranacademy.rpms.model.Interview;
import com.kiranacademy.rpms.model.InterviewMode;
import com.kiranacademy.rpms.model.InterviewResult;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.JobStatus;
import com.kiranacademy.rpms.model.Offer;
import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.model.PlacementStatus;
import com.kiranacademy.rpms.model.User;
import com.kiranacademy.rpms.service.CandidateService;
import com.kiranacademy.rpms.service.CompanyService;
import com.kiranacademy.rpms.service.JobService;
import com.kiranacademy.rpms.utility.DateUtil;
import com.kiranacademy.rpms.utility.InputUtil;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Menu used by ADMIN and RECRUITER accounts. Destructive/master-data actions are
 * restricted to ADMIN; recruiters focus on the recruitment pipeline.
 */
public class StaffMenu {

    private final ApplicationContext context;
    private final User user;
    private final InputUtil input;
    private final boolean admin;

    public StaffMenu(ApplicationContext context, User user, InputUtil input) {
        this.context = context;
        this.user = user;
        this.input = input;
        this.admin = user.isAdmin();
    }

    public void show() {
        while (true) {
            ConsoleView.title("Staff console - " + user.role());
            ConsoleView.menuOption(1, "Candidate management");
            ConsoleView.menuOption(2, "Company management");
            ConsoleView.menuOption(3, "Job openings");
            ConsoleView.menuOption(4, "Applications and shortlisting");
            ConsoleView.menuOption(5, "Interview scheduling and feedback");
            ConsoleView.menuOption(6, "Offers and placements");
            ConsoleView.menuOption(7, "Reports and CSV export");
            ConsoleView.menuOption(8, "Audit trail" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(9, "Change password");
            ConsoleView.menuOption(0, "Logout");
            int choice = input.readInt("Select option: ", 0, 9);
            try {
                switch (choice) {
                    case 1 -> candidateMenu();
                    case 2 -> companyMenu();
                    case 3 -> jobMenu();
                    case 4 -> applicationMenu();
                    case 5 -> interviewMenu();
                    case 6 -> offerMenu();
                    case 7 -> reportMenu();
                    case 8 -> auditMenu();
                    case 9 -> changePassword();
                    case 0 -> {
                        return;
                    }
                    default -> ConsoleView.warn("Invalid option");
                }
            } catch (RPMSException e) {
                ConsoleView.error(e.getMessage());
                input.pause();
            }
        }
    }

    private void candidateMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Candidate management");
            ConsoleView.menuOption(1, "Register candidate" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(2, "View all candidates");
            ConsoleView.menuOption(3, "Search candidates");
            ConsoleView.menuOption(4, "View candidate details");
            ConsoleView.menuOption(5, "Update candidate profile" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(6, "Block / activate candidate" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 6);
            switch (choice) {
                case 1 -> {
                    if (!requireAdmin()) {
                        break;
                    }
                    registerCandidate();
                }
                case 2 -> ConsoleView.candidates(context.candidateService.findAll());
                case 3 -> {
                    String keyword = input.readRequired("Search name/email/technology: ");
                    ConsoleView.candidates(context.candidateService.search(keyword));
                }
                case 4 -> {
                    int id = input.readInt("Candidate id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.candidateDetails(context.candidateService.findById(id));
                }
                case 5 -> {
                    if (!requireAdmin()) {
                        break;
                    }
                    updateCandidate();
                }
                case 6 -> {
                    if (!requireAdmin()) {
                        break;
                    }
                    changeCandidateStatus();
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void registerCandidate() throws RPMSException {
        ConsoleView.section("New candidate registration");
        CandidateService.CandidateInput candidateInput = readCandidateInput(null);
        Candidate candidate = context.candidateService.register(candidateInput, user.username());
        ConsoleView.success("Candidate registered with number " + candidate.registrationNo());
        ConsoleView.candidateDetails(candidate);
    }

    private void updateCandidate() throws RPMSException {
        int id = input.readInt("Candidate id: ", 1, Integer.MAX_VALUE);
        Candidate existing = context.candidateService.findById(id);
        ConsoleView.candidateDetails(existing);
        CandidateService.CandidateInput candidateInput = readCandidateInput(existing);
        Candidate updated = context.candidateService.update(id, candidateInput, user.username());
        ConsoleView.success("Candidate updated: " + updated.fullName());
    }

    private CandidateService.CandidateInput readCandidateInput(Candidate existing) {
        String first = existing == null ? input.readRequired("First name: ")
                : input.readOptional("First name", existing.firstName());
        String last = existing == null ? input.readRequired("Last name: ")
                : input.readOptional("Last name", existing.lastName());
        String email = existing == null ? input.readRequired("Email: ")
                : input.readOptional("Email", existing.email());
        String phone = existing == null ? input.readRequired("Phone: ")
                : input.readOptional("Phone", existing.phone());
        String qualification = existing == null ? input.readRequired("Qualification (e.g. B.Tech): ")
                : input.readOptional("Qualification", existing.qualification());
        int passingYear = existing == null
                ? input.readInt("Passing year: ", 1980, LocalDate.now().getYear() + 1)
                : input.readInt("Passing year", 1980, LocalDate.now().getYear() + 1,
                existing.passingYear());
        BigDecimal percentage = existing == null
                ? input.readDecimal("Percentage: ", 0, 100)
                : input.readDecimal("Percentage", 0, 100, existing.percentage());
        String technology = existing == null ? input.readRequired("Primary technology: ")
                : input.readOptional("Primary technology", existing.technology());
        int backlogs = existing == null ? input.readInt("Active backlogs: ", 0, 100)
                : input.readInt("Active backlogs", 0, 100, existing.backlogs());
        String skills = existing == null ? input.readLine("Skills (comma separated, optional): ")
                : input.readOptional("Skills", existing.skills());
        return new CandidateService.CandidateInput(first, last, email, phone, qualification,
                passingYear, percentage, technology, backlogs, skills);
    }

    private void changeCandidateStatus() throws RPMSException {
        int id = input.readInt("Candidate id: ", 1, Integer.MAX_VALUE);
        Candidate candidate = context.candidateService.findById(id);
        ConsoleView.candidateDetails(candidate);
        CandidateStatus target = input.readEnum("New status: ", CandidateStatus.class);
        Candidate updated = context.candidateService.setStatus(id, target, user.username());
        ConsoleView.success("Candidate status changed to " + updated.status());
    }

    private void companyMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Company management");
            ConsoleView.menuOption(1, "Add company" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(2, "View all companies");
            ConsoleView.menuOption(3, "View company details");
            ConsoleView.menuOption(4, "Update company" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(5, "Activate / deactivate company" + (admin ? "" : " (ADMIN only)"));
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 5);
            switch (choice) {
                case 1 -> {
                    if (!requireAdmin()) {
                        break;
                    }
                    addCompany();
                }
                case 2 -> ConsoleView.companies(context.companyService.findAll());
                case 3 -> {
                    int id = input.readInt("Company id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.companyDetails(context.companyService.findById(id));
                }
                case 4 -> {
                    if (!requireAdmin()) {
                        break;
                    }
                    updateCompany();
                }
                case 5 -> {
                    if (!requireAdmin()) {
                        break;
                    }
                    changeCompanyStatus();
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void addCompany() throws RPMSException {
        CompanyService.CompanyInput companyInput = readCompanyInput(null);
        Company company = context.companyService.add(companyInput, user.username());
        ConsoleView.success("Company added: " + company.name() + " (#" + company.id() + ")");
    }

    private void updateCompany() throws RPMSException {
        int id = input.readInt("Company id: ", 1, Integer.MAX_VALUE);
        Company existing = context.companyService.findById(id);
        ConsoleView.companyDetails(existing);
        CompanyService.CompanyInput companyInput = readCompanyInput(existing);
        Company updated = context.companyService.update(id, companyInput, user.username());
        ConsoleView.success("Company updated: " + updated.name());
    }

    private CompanyService.CompanyInput readCompanyInput(Company existing) {
        String name = existing == null ? input.readRequired("Company name: ")
                : input.readOptional("Company name", existing.name());
        String industry = existing == null ? input.readRequired("Industry: ")
                : input.readOptional("Industry", existing.industry());
        String location = existing == null ? input.readRequired("Location: ")
                : input.readOptional("Location", existing.location());
        String website = existing == null ? input.readLine("Website (optional): ")
                : input.readOptional("Website", existing.website());
        String contact = existing == null ? input.readRequired("Contact person: ")
                : input.readOptional("Contact person", existing.contactPerson());
        String email = existing == null ? input.readRequired("Contact email: ")
                : input.readOptional("Contact email", existing.contactEmail());
        return new CompanyService.CompanyInput(name, industry, location, website, contact, email);
    }

    private void changeCompanyStatus() throws RPMSException {
        int id = input.readInt("Company id: ", 1, Integer.MAX_VALUE);
        ConsoleView.companyDetails(context.companyService.findById(id));
        CompanyStatus target = input.readEnum("New status: ", CompanyStatus.class);
        Company updated = context.companyService.setStatus(id, target, user.username());
        ConsoleView.success("Company status changed to " + updated.status());
    }

    private void jobMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Job openings");
            ConsoleView.menuOption(1, "Create job opening");
            ConsoleView.menuOption(2, "View open jobs");
            ConsoleView.menuOption(3, "View all jobs");
            ConsoleView.menuOption(4, "View job details");
            ConsoleView.menuOption(5, "Change job status");
            ConsoleView.menuOption(6, "Show eligible candidates for a job");
            ConsoleView.menuOption(7, "Search open jobs by technology/title");
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 7);
            switch (choice) {
                case 1 -> createJob();
                case 2 -> ConsoleView.jobs(context.jobService.findAll(true));
                case 3 -> ConsoleView.jobs(context.jobService.findAll(false));
                case 4 -> {
                    int id = input.readInt("Job id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.jobDetails(context.jobService.findById(id));
                }
                case 5 -> changeJobStatus();
                case 6 -> {
                    int id = input.readInt("Job id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.jobDetails(context.jobService.findById(id));
                    ConsoleView.candidates(context.applicationService.eligibleCandidates(id));
                }
                case 7 -> {
                    String keyword = input.readRequired("Technology or title keyword: ");
                    ConsoleView.jobs(context.jobService.searchByTechnology(keyword));
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void createJob() throws RPMSException {
        List<Company> companies = context.companyService.findAll();
        ConsoleView.companies(companies);
        int companyId = input.readInt("Company id for this job: ", 1, Integer.MAX_VALUE);
        String title = input.readRequired("Job title: ");
        String technology = input.readRequired("Required technology (comma separated): ");
        String qualification = input.readRequired("Accepted qualifications (comma separated): ");
        BigDecimal minPercentage = input.readDecimal("Minimum percentage: ", 0, 100);
        int passingYear = input.readInt("Passing year from: ", 1980, LocalDate.now().getYear() + 1);
        int maxBacklogs = input.readInt("Maximum allowed backlogs: ", 0, 100);
        int vacancies = input.readInt("Vacancies: ", 1, 10000);
        BigDecimal packageLpa = input.readDecimal("Package (LPA): ", 0.1, 1000);
        String location = input.readRequired("Work location: ");
        LocalDate defaultDeadline = LocalDate.now().plusDays(30);
        LocalDate deadline = input.readDate("Application deadline (yyyy-MM-dd) ["
                + DateUtil.format(defaultDeadline) + "]: ", defaultDeadline);

        Job job = context.jobService.create(new JobService.JobInput(companyId, title, technology,
                qualification, minPercentage, passingYear, maxBacklogs, vacancies, packageLpa,
                location, deadline), user.username());
        ConsoleView.success("Job #" + job.id() + " created: " + job.title()
                + " at " + job.companyName());
    }

    private void changeJobStatus() throws RPMSException {
        int id = input.readInt("Job id: ", 1, Integer.MAX_VALUE);
        ConsoleView.jobDetails(context.jobService.findById(id));
        JobStatus target = input.readEnum("New status: ", JobStatus.class);
        Job updated = context.jobService.setStatus(id, target, user.username());
        ConsoleView.success("Job status changed to " + updated.status());
    }

    private void applicationMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Applications and shortlisting");
            ConsoleView.menuOption(1, "View all applications");
            ConsoleView.menuOption(2, "View applications for a job");
            ConsoleView.menuOption(3, "View applications for a candidate");
            ConsoleView.menuOption(4, "Shortlist application");
            ConsoleView.menuOption(5, "Select candidate (after interviews)");
            ConsoleView.menuOption(6, "Reject application");
            ConsoleView.menuOption(7, "Put application on hold");
            ConsoleView.menuOption(8, "View application audit history");
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 8);
            switch (choice) {
                case 1 -> ConsoleView.applications(context.applicationService.findAll());
                case 2 -> {
                    int jobId = input.readInt("Job id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.applications(context.applicationService.findForJob(jobId));
                }
                case 3 -> {
                    int candidateId = input.readInt("Candidate id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.applications(context.applicationService.findForCandidate(candidateId));
                }
                case 4 -> {
                    int id = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    Application application = context.applicationService.shortlist(id, user.username());
                    ConsoleView.success("Application #" + application.id() + " " + application.status());
                }
                case 5 -> {
                    int id = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    Application application = context.applicationService.select(id, user.username());
                    ConsoleView.success("Application #" + application.id() + " " + application.status());
                }
                case 6 -> {
                    int id = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    String reason = input.readLine("Rejection reason: ");
                    Application application = context.applicationService.reject(id, reason, user.username());
                    ConsoleView.success("Application #" + application.id() + " " + application.status());
                }
                case 7 -> {
                    int id = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    String reason = input.readLine("Hold reason: ");
                    Application application = context.applicationService.hold(id, reason, user.username());
                    ConsoleView.success("Application #" + application.id() + " " + application.status());
                }
                case 8 -> {
                    int id = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.auditLogs(context.auditService.forEntity("APPLICATION", id));
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void interviewMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Interview scheduling and feedback");
            ConsoleView.menuOption(1, "Schedule interview round");
            ConsoleView.menuOption(2, "View upcoming interviews");
            ConsoleView.menuOption(3, "View all interviews");
            ConsoleView.menuOption(4, "View interviews for an application");
            ConsoleView.menuOption(5, "Submit interview feedback");
            ConsoleView.menuOption(6, "Cancel interview");
            ConsoleView.menuOption(7, "View feedback for an application");
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 7);
            switch (choice) {
                case 1 -> scheduleInterview();
                case 2 -> ConsoleView.interviews(context.interviewService.findUpcoming());
                case 3 -> ConsoleView.interviews(context.interviewService.findAll());
                case 4 -> {
                    int applicationId = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.interviews(context.interviewService.findForApplication(applicationId));
                }
                case 5 -> submitFeedback();
                case 6 -> cancelInterview();
                case 7 -> {
                    int applicationId = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.feedbackList(
                            context.interviewService.findFeedbackForApplication(applicationId));
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void scheduleInterview() throws RPMSException {
        int applicationId = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
        ConsoleView.applicationDetails(context.applicationService.findById(applicationId));
        String roundType = input.readOptional("Round type", "Technical Round");
        LocalDateTime scheduledAt = input.readDateTime(
                "Interview date/time (yyyy-MM-dd HH:mm): ");
        String interviewer = input.readRequired("Interviewer name: ");
        InterviewMode mode = input.readEnum("Mode: ", InterviewMode.class);
        Interview interview = context.interviewService.schedule(applicationId, roundType,
                scheduledAt, interviewer, mode, user.username());
        ConsoleView.success("Interview #" + interview.id() + " round " + interview.roundNo()
                + " scheduled at " + DateUtil.format(interview.scheduledAt()));
    }

    private void submitFeedback() throws RPMSException {
        int interviewId = input.readInt("Interview id: ", 1, Integer.MAX_VALUE);
        int technical = input.readInt("Technical rating (1-5): ", 1, 5);
        int communication = input.readInt("Communication rating (1-5): ", 1, 5);
        int attitude = input.readInt("Attitude rating (1-5): ", 1, 5);
        String comments = input.readLine("Comments: ");
        InterviewResult result = input.readEnum("Result: ", InterviewResult.class);
        var feedback = context.interviewService.submitFeedback(interviewId, technical,
                communication, attitude, comments, result, user.username());
        ConsoleView.success("Feedback recorded. Interview result: " + feedback.result());
    }

    private void cancelInterview() throws RPMSException {
        int interviewId = input.readInt("Interview id: ", 1, Integer.MAX_VALUE);
        String reason = input.readLine("Cancellation reason: ");
        Interview interview = context.interviewService.cancel(interviewId, reason, user.username());
        ConsoleView.success("Interview #" + interview.id() + " " + interview.status());
    }

    private void offerMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Offers and placements");
            ConsoleView.menuOption(1, "Create offer for selected application");
            ConsoleView.menuOption(2, "View all offers");
            ConsoleView.menuOption(3, "Revoke pending offer");
            ConsoleView.menuOption(4, "Record candidate joining and placement");
            ConsoleView.menuOption(5, "View all placements");
            ConsoleView.menuOption(6, "Mark placement completed");
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 6);
            switch (choice) {
                case 1 -> createOffer();
                case 2 -> ConsoleView.offers(context.offerService.findAll());
                case 3 -> revokeOffer();
                case 4 -> recordJoining();
                case 5 -> ConsoleView.placements(context.placementService.findAll());
                case 6 -> completePlacement();
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void createOffer() throws RPMSException {
        int applicationId = input.readInt("Application id: ", 1, Integer.MAX_VALUE);
        ConsoleView.applicationDetails(context.applicationService.findById(applicationId));
        BigDecimal ctc = input.readDecimal("Offered CTC (LPA): ", 0.1, 1000);
        LocalDate offerDate = input.readDate("Offer date (yyyy-MM-dd) ["
                + DateUtil.format(LocalDate.now()) + "]: ", LocalDate.now());
        LocalDate defaultJoining = offerDate.plusDays(30);
        LocalDate joiningDate = input.readDate("Joining date (yyyy-MM-dd) ["
                + DateUtil.format(defaultJoining) + "]: ", defaultJoining);
        Offer offer = context.offerService.createOffer(applicationId, ctc, offerDate,
                joiningDate, user.username());
        ConsoleView.success("Offer #" + offer.id() + " issued to " + offer.candidateName());
        ConsoleView.offerDetails(offer);
    }

    private void revokeOffer() throws RPMSException {
        int offerId = input.readInt("Offer id: ", 1, Integer.MAX_VALUE);
        ConsoleView.offerDetails(context.offerService.findById(offerId));
        if (!input.readYesNo("Revoke this offer?")) {
            return;
        }
        String reason = input.readLine("Reason: ");
        Offer offer = context.offerService.revoke(offerId, reason, user.username());
        ConsoleView.success("Offer #" + offer.id() + " is now " + offer.status());
    }

    private void recordJoining() throws RPMSException {
        int offerId = input.readInt("Offer id: ", 1, Integer.MAX_VALUE);
        Offer offer = context.offerService.findById(offerId);
        ConsoleView.offerDetails(offer);
        LocalDate defaultDate = offer.joiningDate() == null ? LocalDate.now() : offer.joiningDate();
        LocalDate placementDate = input.readDate("Placement/joining date (yyyy-MM-dd) ["
                + DateUtil.format(defaultDate) + "]: ", defaultDate);
        Placement placement = context.offerService.recordJoining(offerId, placementDate,
                user.username());
        ConsoleView.success("Joining recorded. Placement #" + placement.id()
                + " created for " + placement.candidateName());
        ConsoleView.placements(List.of(placement));
    }

    private void completePlacement() throws RPMSException {
        int placementId = input.readInt("Placement id: ", 1, Integer.MAX_VALUE);
        ConsoleView.placements(List.of(context.placementService.findById(placementId)));
        if (!input.readYesNo("Mark this placement as completed?")) {
            return;
        }
        Placement placement = context.placementService.markCompleted(placementId, user.username());
        ConsoleView.success("Placement #" + placement.id() + " " + placement.status());
    }

    private void reportMenu() throws RPMSException {
        while (true) {
            ConsoleView.title("Reports and CSV export");
            ConsoleView.menuOption(1, "Placement dashboard");
            ConsoleView.menuOption(2, "Application pipeline by status");
            ConsoleView.menuOption(3, "Job-wise application statistics");
            ConsoleView.menuOption(4, "Company-wise hiring statistics");
            ConsoleView.menuOption(5, "Placement report");
            ConsoleView.menuOption(6, "Export all reports to CSV (exports folder)");
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 6);
            switch (choice) {
                case 1 -> ConsoleView.dashboard(context.reportService.dashboard());
                case 2 -> ConsoleView.pipeline(context.reportService.pipeline());
                case 3 -> ConsoleView.jobStats(context.reportService.jobWise());
                case 4 -> ConsoleView.companyStats(context.reportService.companyWise());
                case 5 -> ConsoleView.placementStats(context.reportService.placementStats());
                case 6 -> {
                    List<Path> files = context.reportService.exportAll();
                    ConsoleView.success("Exported " + files.size() + " CSV files:");
                    for (Path file : files) {
                        System.out.println("      " + file.toAbsolutePath());
                    }
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
    }

    private void auditMenu() throws RPMSException {
        if (!requireAdmin()) {
            return;
        }
        while (true) {
            ConsoleView.title("Audit trail");
            ConsoleView.menuOption(1, "Recent activity (last 100)");
            ConsoleView.menuOption(2, "History for a specific record");
            ConsoleView.menuOption(0, "Back");
            int choice = input.readInt("Select option: ", 0, 2);
            switch (choice) {
                case 1 -> ConsoleView.auditLogs(context.auditService.recent(100));
                case 2 -> {
                    String entity = input.readRequired(
                            "Entity (APPLICATION/CANDIDATE/JOB/OFFER/INTERVIEW/PLACEMENT/COMPANY): ");
                    int entityId = input.readInt("Record id: ", 1, Integer.MAX_VALUE);
                    ConsoleView.auditLogs(context.auditService.forEntity(entity, entityId));
                }
                case 0 -> {
                    return;
                }
                default -> ConsoleView.warn("Invalid option");
            }
            input.pause();
        }
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

    private boolean requireAdmin() {
        if (!admin) {
            ConsoleView.error("This operation is restricted to ADMIN users");
            return false;
        }
        return true;
    }
}
