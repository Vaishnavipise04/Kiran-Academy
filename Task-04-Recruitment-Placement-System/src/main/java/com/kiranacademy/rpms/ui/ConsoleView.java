package com.kiranacademy.rpms.ui;

import com.kiranacademy.rpms.dao.ReportDAO;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.Company;
import com.kiranacademy.rpms.model.Interview;
import com.kiranacademy.rpms.model.InterviewFeedback;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.Offer;
import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.service.EligibilityResult;
import com.kiranacademy.rpms.service.ReportService.Dashboard;
import com.kiranacademy.rpms.utility.DateUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * All console presentation lives here so menus stay focused on navigation and input.
 */
public final class ConsoleView {

    private static final String LINE = "=".repeat(120);
    private static final String THIN = "-".repeat(120);

    private ConsoleView() {
    }

    public static void banner() {
        System.out.println();
        System.out.println("  ____  ____  __  __ ____  ");
        System.out.println(" |  _ \\|  _ \\|  \\/  / ___| ");
        System.out.println(" | |_) | |_) | |\\/| \\___ \\ ");
        System.out.println(" |  _ <|  __/| |  | |___) |");
        System.out.println(" |_| \\_\\_|   |_|  |_|____/ ");
        System.out.println();
        System.out.println("  HIRETRACK - RECRUITMENT & PLACEMENT MANAGEMENT SYSTEM");
        System.out.println("  Java + JDBC + MySQL | Layered Architecture | Console Edition");
        System.out.println(LINE);
    }

    public static void title(String title) {
        System.out.println();
        System.out.println(THIN);
        System.out.println("  " + title.toUpperCase());
        System.out.println(THIN);
    }

    public static void section(String title) {
        System.out.println("\n-- " + title + " " + "-".repeat(Math.max(0, 70 - title.length())));
    }

    public static void menuOption(int number, String text) {
        System.out.printf("  %2d. %s%n", number, text);
    }

    public static void success(String message) {
        System.out.println("  [OK] " + message);
    }

    public static void error(String message) {
        System.out.println("  [ERROR] " + message);
    }

    public static void warn(String message) {
        System.out.println("  [WARN] " + message);
    }

    public static void info(String message) {
        System.out.println("  [i] " + message);
    }

    public static void blank() {
        System.out.println();
    }

    public static void dashboard(Dashboard dashboard) {
        section("Placement dashboard");
        System.out.printf("  Candidates: %-5d Active: %-5d Placed: %-5d Companies: %-5d%n",
                dashboard.totalCandidates(), dashboard.activeCandidates(),
                dashboard.placedCandidates(), dashboard.companies());
        System.out.printf("  Open jobs: %-5d Applications: %-5d Offers: %-5d Pending offers: %-5d%n",
                dashboard.openJobs(), dashboard.applications(), dashboard.offers(),
                dashboard.pendingOffers());
        System.out.printf("  Upcoming interviews: %-5d Placements: %-5d%n",
                dashboard.upcomingInterviews(), dashboard.placements());
    }

    public static void candidates(List<Candidate> candidates) {
        if (empty(candidates)) {
            return;
        }
        System.out.printf("%-4s %-12s %-22s %-10s %-5s %-7s %-18s %-4s %-9s%n",
                "ID", "Reg No", "Name", "Qual", "Year", "Pct", "Technology", "BL", "Status");
        System.out.println(THIN);
        for (Candidate c : candidates) {
            System.out.printf("%-4d %-12s %-22s %-10s %-5d %-7s %-18s %-4d %-9s%n",
                    c.id(), cut(c.registrationNo(), 12), cut(c.fullName(), 22),
                    cut(c.qualification(), 10), c.passingYear(),
                    c.percentage().toPlainString(), cut(c.technology(), 18), c.backlogs(),
                    c.status());
        }
        System.out.println("  Total: " + candidates.size());
    }

    public static void companies(List<Company> companies) {
        if (empty(companies)) {
            return;
        }
        System.out.printf("%-4s %-24s %-22s %-14s %-18s %-27s %-8s%n",
                "ID", "Company", "Industry", "Location", "Contact", "Email", "Status");
        System.out.println(THIN);
        for (Company c : companies) {
            System.out.printf("%-4d %-24s %-22s %-14s %-18s %-27s %-8s%n",
                    c.id(), cut(c.name(), 24), cut(c.industry(), 22), cut(c.location(), 14),
                    cut(c.contactPerson(), 18), cut(c.contactEmail(), 27), c.status());
        }
        System.out.println("  Total: " + companies.size());
    }

    public static void jobs(List<Job> jobs) {
        if (empty(jobs)) {
            return;
        }
        System.out.printf("%-4s %-20s %-24s %-22s %-6s %-5s %-4s %-4s %-7s %-9s %-10s%n",
                "ID", "Company", "Title", "Technology", "Min%", "From", "BL", "Vac", "LPA",
                "Status", "Deadline");
        System.out.println(THIN);
        for (Job job : jobs) {
            System.out.printf("%-4d %-20s %-24s %-22s %-6s %-5d %-4d %-4d %-7s %-9s %-10s%n",
                    job.id(), cut(job.companyName(), 20), cut(job.title(), 24),
                    cut(job.technology(), 22), job.minPercentage().toPlainString(),
                    job.passingYearFrom(), job.maxBacklogs(), job.vacancies(),
                    job.packageLpa().toPlainString(), job.status(),
                    DateUtil.format(job.applicationDeadline()));
        }
        System.out.println("  Total: " + jobs.size());
    }

    public static void applications(List<Application> applications) {
        if (empty(applications)) {
            return;
        }
        System.out.printf("%-5s %-22s %-24s %-20s %-20s %-16s%n",
                "ID", "Candidate", "Job", "Company", "Status", "Applied");
        System.out.println(THIN);
        for (Application a : applications) {
            System.out.printf("%-5d %-22s %-24s %-20s %-20s %-16s%n",
                    a.id(), cut(a.candidateName(), 22), cut(a.jobTitle(), 24),
                    cut(a.companyName(), 20), a.status(), DateUtil.format(a.appliedAt()));
        }
        System.out.println("  Total: " + applications.size());
    }

    public static void interviews(List<Interview> interviews) {
        if (empty(interviews)) {
            return;
        }
        System.out.printf("%-5s %-5s %-20s %-18s %-16s %-18s %-10s %-10s%n",
                "ID", "App", "Candidate", "Round", "Schedule", "Interviewer", "Mode", "Status");
        System.out.println(THIN);
        for (Interview i : interviews) {
            System.out.printf("%-5d %-5d %-20s %-18s %-16s %-18s %-10s %-10s%n",
                    i.id(), i.applicationId(), cut(i.candidateName(), 20),
                    cut("R" + i.roundNo() + " " + i.roundType(), 18),
                    DateUtil.format(i.scheduledAt()), cut(i.interviewer(), 18),
                    i.mode(), i.status());
        }
        System.out.println("  Total: " + interviews.size());
    }

    public static void feedbackList(List<InterviewFeedback> feedbacks) {
        if (empty(feedbacks)) {
            return;
        }
        System.out.printf("%-5s %-9s %-5s %-5s %-5s %-9s %-40s %-16s%n",
                "ID", "Interview", "Tech", "Comm", "Att", "Result", "Comments", "By");
        System.out.println(THIN);
        for (InterviewFeedback f : feedbacks) {
            System.out.printf("%-5d %-9d %-5d %-5d %-5d %-9s %-40s %-16s%n",
                    f.id(), f.interviewId(), f.technicalRating(), f.communicationRating(),
                    f.attitudeRating(), f.result(), cut(f.comments(), 40), cut(f.createdBy(), 16));
        }
    }

    public static void offers(List<Offer> offers) {
        if (empty(offers)) {
            return;
        }
        System.out.printf("%-5s %-22s %-20s %-22s %-7s %-12s %-10s %-12s%n",
                "ID", "Candidate", "Company", "Job", "CTC", "Offer Date", "Status", "Joining");
        System.out.println(THIN);
        for (Offer o : offers) {
            System.out.printf("%-5d %-22s %-20s %-22s %-7s %-12s %-10s %-12s%n",
                    o.id(), cut(o.candidateName(), 22), cut(o.companyName(), 20),
                    cut(o.jobTitle(), 22), o.offeredCtc().toPlainString(),
                    DateUtil.format(o.offerDate()), o.status(), DateUtil.format(o.joiningDate()));
        }
        System.out.println("  Total: " + offers.size());
    }

    public static void placements(List<Placement> placements) {
        if (empty(placements)) {
            return;
        }
        System.out.printf("%-5s %-22s %-20s %-24s %-12s %-7s %-10s%n",
                "ID", "Candidate", "Company", "Job", "Date", "LPA", "Status");
        System.out.println(THIN);
        for (Placement p : placements) {
            System.out.printf("%-5d %-22s %-20s %-24s %-12s %-7s %-10s%n",
                    p.id(), cut(p.candidateName(), 22), cut(p.companyName(), 20),
                    cut(p.jobTitle(), 24), DateUtil.format(p.placementDate()),
                    p.packageLpa().toPlainString(), p.status());
        }
        System.out.println("  Total: " + placements.size());
    }

    public static void auditLogs(List<AuditLog> logs) {
        if (empty(logs)) {
            return;
        }
        System.out.printf("%-5s %-12s %-6s %-18s %-14s %-14s %-16s %-16s %-30s%n",
                "ID", "Entity", "Ref", "Action", "Old", "New", "By", "When", "Details");
        System.out.println(THIN);
        for (AuditLog log : logs) {
            System.out.printf("%-5d %-12s %-6d %-18s %-14s %-14s %-16s %-16s %-30s%n",
                    log.id(), cut(log.entityName(), 12), log.entityId(), cut(log.action(), 18),
                    cut(log.oldStatus(), 14), cut(log.newStatus(), 14),
                    cut(log.performedBy(), 16), DateUtil.format(log.createdAt()),
                    cut(log.details(), 30));
        }
        System.out.println("  Showing " + logs.size() + " entries");
    }

    public static void pipeline(List<ReportDAO.StatusCount> statuses) {
        if (empty(statuses)) {
            return;
        }
        System.out.printf("%-24s %-8s %s%n", "Application Status", "Count", "Bar");
        System.out.println(THIN);
        for (ReportDAO.StatusCount status : statuses) {
            int bars = (int) Math.min(40, status.count());
            System.out.printf("%-24s %-8d %s%n", status.status(), status.count(), "#".repeat(bars));
        }
    }

    public static void jobStats(List<ReportDAO.JobStat> stats) {
        if (empty(stats)) {
            return;
        }
        System.out.printf("%-5s %-26s %-20s %-6s %-6s %-6s %-6s%n",
                "Job", "Title", "Company", "Apps", "Short", "Select", "Joined");
        System.out.println(THIN);
        for (ReportDAO.JobStat stat : stats) {
            System.out.printf("%-5d %-26s %-20s %-6d %-6d %-6d %-6d%n",
                    stat.jobId(), cut(stat.title(), 26), cut(stat.company(), 20),
                    stat.applications(), stat.shortlisted(), stat.selected(), stat.joined());
        }
    }

    public static void companyStats(List<ReportDAO.CompanyStat> stats) {
        if (empty(stats)) {
            return;
        }
        System.out.printf("%-26s %-6s %-8s %-8s %-8s %-10s%n",
                "Company", "Jobs", "Apps", "Offers", "Joined", "Avg LPA");
        System.out.println(THIN);
        for (ReportDAO.CompanyStat stat : stats) {
            System.out.printf("%-26s %-6d %-8d %-8d %-8d %-10s%n",
                    cut(stat.company(), 26), stat.jobs(), stat.applications(), stat.offers(),
                    stat.joined(), money(stat.averagePackage()));
        }
    }

    public static void placementStats(List<ReportDAO.PlacementStat> stats) {
        if (empty(stats)) {
            return;
        }
        System.out.printf("%-22s %-20s %-24s %-12s %-7s %-10s%n",
                "Candidate", "Company", "Job", "Placed On", "LPA", "Status");
        System.out.println(THIN);
        for (ReportDAO.PlacementStat stat : stats) {
            System.out.printf("%-22s %-20s %-24s %-12s %-7s %-10s%n",
                    cut(stat.candidate(), 22), cut(stat.company(), 20), cut(stat.job(), 24),
                    DateUtil.format(stat.placementDate()), stat.packageLpa().toPlainString(),
                    stat.status());
        }
    }

    public static void eligibility(EligibilityResult result) {
        if (result.eligible()) {
            success("ELIGIBLE - candidate satisfies all job requirements.");
        } else {
            error("NOT ELIGIBLE");
            result.failures().forEach(reason -> System.out.println("      - " + reason));
        }
    }

    public static void candidateDetails(Candidate c) {
        section("Candidate #" + c.id() + " - " + c.registrationNo());
        System.out.println("  Name         : " + c.fullName());
        System.out.println("  Email/Phone  : " + c.email() + " / " + c.phone());
        System.out.println("  Qualification: " + c.qualification() + " (" + c.passingYear() + ")");
        System.out.println("  Percentage   : " + c.percentage() + "% | Backlogs: " + c.backlogs());
        System.out.println("  Technology   : " + c.technology());
        System.out.println("  Skills       : " + (c.skills() == null ? "-" : c.skills()));
        System.out.println("  Status       : " + c.status() + " | Registered: "
                + DateUtil.format(c.createdAt()));
    }

    public static void jobDetails(Job job) {
        section("Job #" + job.id() + " - " + job.title());
        System.out.println("  Company      : " + job.companyName() + " (" + job.location() + ")");
        System.out.println("  Technology   : " + job.technology());
        System.out.println("  Qualification: " + job.qualificationRequired());
        System.out.println("  Criteria     : Min " + job.minPercentage() + "%, passing year from "
                + job.passingYearFrom() + ", max " + job.maxBacklogs() + " backlog(s)");
        System.out.println("  Package/Vac  : " + job.packageLpa() + " LPA / " + job.vacancies());
        System.out.println("  Status       : " + job.status() + " | Deadline: "
                + DateUtil.format(job.applicationDeadline()));
    }

    public static void companyDetails(Company company) {
        section("Company #" + company.id() + " - " + company.name());
        System.out.println("  Industry     : " + company.industry());
        System.out.println("  Location     : " + company.location());
        System.out.println("  Website      : " + (company.website() == null ? "-" : company.website()));
        System.out.println("  Contact      : " + company.contactPerson()
                + " <" + company.contactEmail() + ">");
        System.out.println("  Status       : " + company.status());
    }

    public static void applicationDetails(Application application) {
        section("Application #" + application.id());
        System.out.println("  Candidate : " + application.candidateName());
        System.out.println("  Job       : " + application.jobTitle()
                + " at " + application.companyName());
        System.out.println("  Status    : " + application.status());
        System.out.println("  Applied   : " + DateUtil.format(application.appliedAt()));
    }

    public static void offerDetails(Offer offer) {
        section("Offer #" + offer.id());
        System.out.println("  Candidate : " + offer.candidateName());
        System.out.println("  Job       : " + offer.jobTitle() + " at " + offer.companyName());
        System.out.println("  CTC       : " + offer.offeredCtc() + " LPA");
        System.out.println("  Offer date: " + DateUtil.format(offer.offerDate()));
        System.out.println("  Joining   : " + DateUtil.format(offer.joiningDate()));
        System.out.println("  Status    : " + offer.status());
    }

    public static void footer() {
        System.out.println(THIN);
    }

    private static boolean empty(List<?> values) {
        if (values == null || values.isEmpty()) {
            warn("No records found.");
            return true;
        }
        return false;
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return "-";
        }
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "~";
    }

    private static String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
