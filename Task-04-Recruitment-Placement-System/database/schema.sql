-- ============================================================
-- HireTrack - Recruitment & Placement Management System
-- MySQL 8.x schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS rpms
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE rpms;

-- ------------------------------------------------------------
-- Candidates
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS candidates (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    registration_no   VARCHAR(20)  NOT NULL,
    first_name        VARCHAR(60)  NOT NULL,
    last_name         VARCHAR(60)  NOT NULL,
    email             VARCHAR(120) NOT NULL,
    phone             VARCHAR(20)  NOT NULL,
    qualification     VARCHAR(60)  NOT NULL,
    passing_year      INT          NOT NULL,
    percentage        DECIMAL(5,2) NOT NULL,
    technology        VARCHAR(120) NOT NULL,
    backlogs          INT          NOT NULL DEFAULT 0,
    skills            VARCHAR(500),
    status            ENUM('ACTIVE','PLACED','BLOCKED','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_candidates_registration UNIQUE (registration_no),
    CONSTRAINT uk_candidates_email UNIQUE (email),
    CONSTRAINT chk_candidates_percentage CHECK (percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_candidates_backlogs CHECK (backlogs >= 0),
    KEY idx_candidates_status (status),
    KEY idx_candidates_technology (technology)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Companies
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS companies (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    industry        VARCHAR(80)  NOT NULL,
    location        VARCHAR(100) NOT NULL,
    website         VARCHAR(150),
    contact_person  VARCHAR(100) NOT NULL,
    contact_email   VARCHAR(120) NOT NULL,
    status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_companies_name UNIQUE (name),
    KEY idx_companies_status (status)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Job openings
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS jobs (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    company_id              INT           NOT NULL,
    title                   VARCHAR(120)  NOT NULL,
    technology              VARCHAR(150)  NOT NULL,
    qualification_required  VARCHAR(120)  NOT NULL,
    min_percentage          DECIMAL(5,2)  NOT NULL DEFAULT 0,
    passing_year_from       INT           NOT NULL,
    max_backlogs            INT           NOT NULL DEFAULT 0,
    vacancies               INT           NOT NULL DEFAULT 1,
    package_lpa             DECIMAL(6,2)  NOT NULL,
    location                VARCHAR(100)  NOT NULL,
    status                  ENUM('OPEN','ON_HOLD','CLOSED') NOT NULL DEFAULT 'OPEN',
    application_deadline    DATE          NOT NULL,
    created_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT uk_jobs_company_title UNIQUE (company_id, title),
    CONSTRAINT chk_jobs_percentage CHECK (min_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_jobs_vacancies CHECK (vacancies > 0),
    CONSTRAINT chk_jobs_backlogs CHECK (max_backlogs >= 0),
    KEY idx_jobs_status (status),
    KEY idx_jobs_deadline (application_deadline)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Application users (role based access)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    role           ENUM('ADMIN','RECRUITER','CANDIDATE') NOT NULL,
    candidate_id   INT,
    active         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT fk_users_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    KEY idx_users_role (role)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Applications
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS applications (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id  INT NOT NULL,
    job_id        INT NOT NULL,
    status        ENUM('APPLIED','SHORTLISTED','INTERVIEW_SCHEDULED','INTERVIEWED',
                       'SELECTED','OFFERED','JOINED','REJECTED','WITHDRAWN','ON_HOLD')
                  NOT NULL DEFAULT 'APPLIED',
    applied_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT uk_applications_candidate_job UNIQUE (candidate_id, job_id),
    KEY idx_applications_status (status),
    KEY idx_applications_job (job_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Interview rounds
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interviews (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    application_id  INT NOT NULL,
    round_no        INT NOT NULL,
    round_type      VARCHAR(60) NOT NULL,
    scheduled_at    DATETIME NOT NULL,
    interviewer     VARCHAR(100) NOT NULL,
    mode            ENUM('IN_PERSON','VIDEO','TELEPHONIC') NOT NULL DEFAULT 'VIDEO',
    status          ENUM('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT uk_interviews_round UNIQUE (application_id, round_no),
    KEY idx_interviews_status (status),
    KEY idx_interviews_schedule (scheduled_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Interview feedback
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interview_feedback (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    interview_id            INT NOT NULL,
    technical_rating        INT NOT NULL,
    communication_rating    INT NOT NULL,
    attitude_rating         INT NOT NULL,
    comments                TEXT,
    result                  ENUM('PASS','FAIL','ON_HOLD') NOT NULL,
    created_by              VARCHAR(100) NOT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_interview FOREIGN KEY (interview_id) REFERENCES interviews(id),
    CONSTRAINT uk_feedback_interview UNIQUE (interview_id),
    CONSTRAINT chk_feedback_technical CHECK (technical_rating BETWEEN 1 AND 5),
    CONSTRAINT chk_feedback_communication CHECK (communication_rating BETWEEN 1 AND 5),
    CONSTRAINT chk_feedback_attitude CHECK (attitude_rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Offers
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS offers (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    offered_ctc   DECIMAL(6,2) NOT NULL,
    offer_date    DATE NOT NULL,
    status        ENUM('PENDING','ACCEPTED','DECLINED','REVOKED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    joining_date  DATE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offers_application FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT uk_offers_application UNIQUE (application_id),
    KEY idx_offers_status (status)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Placements
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS placements (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id    INT NOT NULL,
    company_id      INT NOT NULL,
    job_id          INT NOT NULL,
    offer_id        INT NOT NULL,
    placement_date  DATE NOT NULL,
    package_lpa     DECIMAL(6,2) NOT NULL,
    status          ENUM('ACTIVE','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_placements_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    CONSTRAINT fk_placements_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_placements_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT fk_placements_offer FOREIGN KEY (offer_id) REFERENCES offers(id),
    CONSTRAINT uk_placements_candidate UNIQUE (candidate_id),
    CONSTRAINT uk_placements_offer UNIQUE (offer_id),
    KEY idx_placements_status (status),
    KEY idx_placements_date (placement_date)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Audit history
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    entity_name   VARCHAR(40) NOT NULL,
    entity_id     INT NOT NULL,
    action        VARCHAR(60) NOT NULL,
    old_status    VARCHAR(40),
    new_status    VARCHAR(40),
    performed_by  VARCHAR(100) NOT NULL,
    details       VARCHAR(500),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_entity (entity_name, entity_id),
    KEY idx_audit_created (created_at)
) ENGINE=InnoDB;
