package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.ApplicationDAO;
import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CandidateDAO;
import com.kiranacademy.rpms.dao.CompanyDAO;
import com.kiranacademy.rpms.dao.InterviewDAO;
import com.kiranacademy.rpms.dao.JobDAO;
import com.kiranacademy.rpms.dao.OfferDAO;
import com.kiranacademy.rpms.dao.PlacementDAO;
import com.kiranacademy.rpms.dao.ReportDAO;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.model.Interview;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.Offer;
import com.kiranacademy.rpms.model.OfferStatus;
import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.utility.CsvUtil;
import com.kiranacademy.rpms.utility.DateUtil;
import com.kiranacademy.rpms.utility.Db;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public record Dashboard(long totalCandidates, long activeCandidates, long placedCandidates,
                            long companies, long openJobs, long applications, long offers,
                            long pendingOffers, long upcomingInterviews, long placements) {
    }

    private final ReportDAO reportDAO;
    private final CandidateDAO candidateDAO;
    private final CompanyDAO companyDAO;
    private final JobDAO jobDAO;
    private final ApplicationDAO applicationDAO;
    private final InterviewDAO interviewDAO;
    private final OfferDAO offerDAO;
    private final PlacementDAO placementDAO;
    private final AuditDAO auditDAO;

    public ReportService(ReportDAO reportDAO, CandidateDAO candidateDAO, CompanyDAO companyDAO,
                         JobDAO jobDAO, ApplicationDAO applicationDAO, InterviewDAO interviewDAO,
                         OfferDAO offerDAO, PlacementDAO placementDAO, AuditDAO auditDAO) {
        this.reportDAO = reportDAO;
        this.candidateDAO = candidateDAO;
        this.companyDAO = companyDAO;
        this.jobDAO = jobDAO;
        this.applicationDAO = applicationDAO;
        this.interviewDAO = interviewDAO;
        this.offerDAO = offerDAO;
        this.placementDAO = placementDAO;
        this.auditDAO = auditDAO;
    }

    public Dashboard dashboard() throws RPMSException {
        return Db.read(connection -> new Dashboard(
                candidateDAO.count(connection),
                candidateDAO.countByStatus(connection, CandidateStatus.ACTIVE),
                candidateDAO.countByStatus(connection, CandidateStatus.PLACED),
                companyDAO.count(connection),
                jobDAO.countOpen(connection),
                applicationDAO.countAll(connection),
                offerDAO.count(connection),
                offerDAO.countByStatus(connection, OfferStatus.PENDING),
                interviewDAO.findUpcoming(connection).size(),
                placementDAO.count(connection)
        ));
    }

    public List<ReportDAO.StatusCount> pipeline() throws RPMSException {
        return Db.read(reportDAO::applicationPipeline);
    }

    public List<ReportDAO.JobStat> jobWise() throws RPMSException {
        return Db.read(reportDAO::jobWiseStats);
    }

    public List<ReportDAO.CompanyStat> companyWise() throws RPMSException {
        return Db.read(reportDAO::companyHiringStats);
    }

    public List<ReportDAO.PlacementStat> placementStats() throws RPMSException {
        return Db.read(reportDAO::placementStats);
    }

    public List<Path> exportAll() throws RPMSException {
        List<Path> files = new ArrayList<>();

        List<Candidate> candidates = Db.read(candidateDAO::findAll);
        List<List<String>> candidateRows = new ArrayList<>();
        for (Candidate candidate : candidates) {
            candidateRows.add(List.of(
                    String.valueOf(candidate.id()), candidate.registrationNo(),
                    candidate.fullName(), candidate.email(), candidate.phone(),
                    candidate.qualification(), String.valueOf(candidate.passingYear()),
                    candidate.percentage().toPlainString(), candidate.technology(),
                    String.valueOf(candidate.backlogs()),
                    candidate.skills() == null ? "" : candidate.skills(),
                    candidate.status().name(), DateUtil.format(candidate.createdAt())));
        }
        files.add(CsvUtil.write("candidates.csv",
                List.of("id", "registration_no", "name", "email", "phone", "qualification",
                        "passing_year", "percentage", "technology", "backlogs", "skills",
                        "status", "created_at"), candidateRows));

        List<Job> jobs = Db.read(connection -> jobDAO.findAll(connection, false));
        List<List<String>> jobRows = new ArrayList<>();
        for (Job job : jobs) {
            jobRows.add(List.of(
                    String.valueOf(job.id()), job.companyName(), job.title(), job.technology(),
                    job.qualificationRequired(), job.minPercentage().toPlainString(),
                    String.valueOf(job.passingYearFrom()), String.valueOf(job.maxBacklogs()),
                    String.valueOf(job.vacancies()), job.packageLpa().toPlainString(),
                    job.location(), job.status().name(), DateUtil.format(job.applicationDeadline())));
        }
        files.add(CsvUtil.write("jobs.csv",
                List.of("id", "company", "title", "technology", "qualification",
                        "min_percentage", "passing_year_from", "max_backlogs", "vacancies",
                        "package_lpa", "location", "status", "deadline"), jobRows));

        List<Application> applications = Db.read(applicationDAO::findAll);
        List<List<String>> applicationRows = new ArrayList<>();
        for (Application application : applications) {
            applicationRows.add(List.of(
                    String.valueOf(application.id()), application.candidateName(),
                    application.jobTitle(), application.companyName(), application.status().name(),
                    DateUtil.format(application.appliedAt())));
        }
        files.add(CsvUtil.write("applications.csv",
                List.of("id", "candidate", "job", "company", "status", "applied_at"),
                applicationRows));

        List<Interview> interviews = Db.read(interviewDAO::findAll);
        List<List<String>> interviewRows = new ArrayList<>();
        for (Interview interview : interviews) {
            interviewRows.add(List.of(
                    String.valueOf(interview.id()), String.valueOf(interview.applicationId()),
                    interview.candidateName(), interview.jobTitle(), interview.companyName(),
                    String.valueOf(interview.roundNo()), interview.roundType(),
                    DateUtil.format(interview.scheduledAt()), interview.interviewer(),
                    interview.mode().name(), interview.status().name()));
        }
        files.add(CsvUtil.write("interviews.csv",
                List.of("id", "application_id", "candidate", "job", "company", "round_no",
                        "round_type", "scheduled_at", "interviewer", "mode", "status"),
                interviewRows));

        List<Offer> offers = Db.read(offerDAO::findAll);
        List<List<String>> offerRows = new ArrayList<>();
        for (Offer offer : offers) {
            offerRows.add(List.of(
                    String.valueOf(offer.id()), offer.candidateName(), offer.jobTitle(),
                    offer.companyName(), offer.offeredCtc().toPlainString(),
                    DateUtil.format(offer.offerDate()), offer.status().name(),
                    DateUtil.format(offer.joiningDate())));
        }
        files.add(CsvUtil.write("offers.csv",
                List.of("id", "candidate", "job", "company", "offered_ctc", "offer_date",
                        "status", "joining_date"), offerRows));

        List<Placement> placements = Db.read(placementDAO::findAll);
        List<List<String>> placementRows = new ArrayList<>();
        for (Placement placement : placements) {
            placementRows.add(List.of(
                    String.valueOf(placement.id()), placement.candidateName(),
                    placement.companyName(), placement.jobTitle(),
                    DateUtil.format(placement.placementDate()),
                    placement.packageLpa().toPlainString(), placement.status().name()));
        }
        files.add(CsvUtil.write("placements.csv",
                List.of("id", "candidate", "company", "job", "placement_date", "package_lpa",
                        "status"), placementRows));

        List<List<String>> pipelineRows = new ArrayList<>();
        for (ReportDAO.StatusCount status : pipeline()) {
            pipelineRows.add(List.of(status.status(), String.valueOf(status.count())));
        }
        files.add(CsvUtil.write("application_pipeline.csv",
                List.of("status", "applications"), pipelineRows));

        List<AuditLog> auditLogs = Db.read(connection -> auditDAO.findRecent(connection, 500));
        List<List<String>> auditRows = new ArrayList<>();
        for (AuditLog log : auditLogs) {
            auditRows.add(List.of(
                    String.valueOf(log.id()), log.entityName(), String.valueOf(log.entityId()),
                    log.action(), log.oldStatus() == null ? "" : log.oldStatus(),
                    log.newStatus() == null ? "" : log.newStatus(), log.performedBy(),
                    log.details() == null ? "" : log.details(),
                    DateUtil.format(log.createdAt())));
        }
        files.add(CsvUtil.write("audit_log.csv",
                List.of("id", "entity", "entity_id", "action", "old_status", "new_status",
                        "performed_by", "details", "created_at"), auditRows));

        return files;
    }
}
