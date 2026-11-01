package com.kiranacademy.rpms.config;

import com.kiranacademy.rpms.dao.ApplicationDAO;
import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CandidateDAO;
import com.kiranacademy.rpms.dao.CompanyDAO;
import com.kiranacademy.rpms.dao.FeedbackDAO;
import com.kiranacademy.rpms.dao.InterviewDAO;
import com.kiranacademy.rpms.dao.JobDAO;
import com.kiranacademy.rpms.dao.OfferDAO;
import com.kiranacademy.rpms.dao.PlacementDAO;
import com.kiranacademy.rpms.dao.ReportDAO;
import com.kiranacademy.rpms.dao.UserDAO;
import com.kiranacademy.rpms.service.ApplicationService;
import com.kiranacademy.rpms.service.AuditService;
import com.kiranacademy.rpms.service.AuthService;
import com.kiranacademy.rpms.service.CandidateService;
import com.kiranacademy.rpms.service.CompanyService;
import com.kiranacademy.rpms.service.EligibilityEngine;
import com.kiranacademy.rpms.service.InterviewService;
import com.kiranacademy.rpms.service.JobService;
import com.kiranacademy.rpms.service.OfferService;
import com.kiranacademy.rpms.service.PlacementService;
import com.kiranacademy.rpms.service.ReportService;

/**
 * Manual dependency wiring for the application. Keeping construction here makes
 * the layering explicit without introducing a framework.
 */
public final class ApplicationContext {

    public final AuthService authService;
    public final CandidateService candidateService;
    public final CompanyService companyService;
    public final JobService jobService;
    public final ApplicationService applicationService;
    public final InterviewService interviewService;
    public final OfferService offerService;
    public final PlacementService placementService;
    public final ReportService reportService;
    public final AuditService auditService;

    public ApplicationContext() {
        AuditDAO auditDAO = new AuditDAO();
        UserDAO userDAO = new UserDAO();
        CandidateDAO candidateDAO = new CandidateDAO();
        CompanyDAO companyDAO = new CompanyDAO();
        JobDAO jobDAO = new JobDAO();
        ApplicationDAO applicationDAO = new ApplicationDAO();
        InterviewDAO interviewDAO = new InterviewDAO();
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        OfferDAO offerDAO = new OfferDAO();
        PlacementDAO placementDAO = new PlacementDAO();
        ReportDAO reportDAO = new ReportDAO();
        EligibilityEngine eligibilityEngine = new EligibilityEngine();

        authService = new AuthService(userDAO, candidateDAO);
        candidateService = new CandidateService(candidateDAO, auditDAO);
        companyService = new CompanyService(companyDAO, auditDAO);
        jobService = new JobService(jobDAO, companyDAO, auditDAO);
        applicationService = new ApplicationService(applicationDAO, candidateDAO, jobDAO,
                auditDAO, eligibilityEngine);
        interviewService = new InterviewService(interviewDAO, feedbackDAO, applicationDAO, auditDAO);
        offerService = new OfferService(offerDAO, applicationDAO, candidateDAO, jobDAO,
                placementDAO, auditDAO);
        placementService = new PlacementService(placementDAO, auditDAO);
        reportService = new ReportService(reportDAO, candidateDAO, companyDAO, jobDAO,
                applicationDAO, interviewDAO, offerDAO, placementDAO, auditDAO);
        auditService = new AuditService(auditDAO);
    }
}
