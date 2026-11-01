# HireTrack — Recruitment & Placement Management System

![Java 17](https://img.shields.io/badge/Java-17-orange)
![MySQL 8](https://img.shields.io/badge/MySQL-8.x-blue)
![Maven](https://img.shields.io/badge/Build-Maven-red)
![JUnit 5](https://img.shields.io/badge/Tests-24%20passing-brightgreen)

**Student:** Vaishnavi Pise  
**Program:** Kiran Academy Internship — Task 4  
**Project type:** Industry-oriented backend business application with an HTML/CSS UI

**Abstract:** HireTrack manages the complete recruitment and placement lifecycle for a training institute or placement cell. It keeps candidates, companies, job openings, applications, eligibility criteria, interview rounds, feedback, offers, joining status and final placements in one structured, database-driven system. The application is built with Java, OOP, JDBC, collections, exception handling, file handling and MySQL, using a layered Model → DAO → Service → UI architecture. A rule-based eligibility engine validates qualification, passing year, percentage, technology, backlog and deadline requirements before an application proceeds.

A Java + MySQL application for a college placement cell. It manages the full recruitment lifecycle: candidate registration, eligibility checks, job applications, shortlisting, interview rounds, feedback, offers, joining and placement reporting.

The project includes:

- A layered Java console application with a MySQL schema and sample data.
- A rule-based eligibility engine and guarded workflow state transitions.
- Role-based access for administrators, recruiters and candidates.
- CSV reporting for candidates, jobs, applications, interviews, offers, placements, pipeline and audit history.
- Static HTML/CSS UI pages in `web/` that preview the intended screens.

## Contents

- [1. Tech stack](#1-tech-stack)
- [2. Features](#2-features)
- [3. Architecture](#3-architecture)
- [4. Database design](#4-database-design)
- [5. Setup](#5-setup)
- [6. Demo accounts](#6-demo-accounts)
- [7. Sample data](#7-sample-data)
- [8. Demo walkthrough](#8-demo-walkthrough)
- [9. Console menus](#9-console-menus)
- [10. Build and test](#10-build-and-test)
- [11. HTML/CSS UI prototypes](#11-htmlcss-ui-prototypes)
- [12. Error handling and edge cases](#12-error-handling-and-edge-cases)
- [13. Known limitations and future work](#13-known-limitations-and-future-work)
- [14. Troubleshooting](#14-troubleshooting)

---

## 1. Tech stack

| Area | Choice |
| --- | --- |
| Language | Java 17 (compiled with `--release 17`) |
| Build | Maven 3.9+ |
| Database | MySQL 8.x |
| JDBC driver | `com.mysql:mysql-connector-j:8.4.0` |
| Testing | JUnit 5.10.2 |
| Packaging | Maven Shade plugin, executable `target/rpms.jar` |
| UI prototypes | Plain HTML5 + CSS3 (no framework, no backend calls) |

---

## 2. Features

### Candidate management
- Register, view, search and update candidates.
- Block, activate or mark candidates as placed.
- Unique registration number and email, validated phone and percentage.
- Skills and primary technology stored for eligibility matching.

### Company and job management
- Maintain company profiles, contacts and active status.
- Create job openings with eligibility criteria, vacancies, package and deadline.
- Change job status: `OPEN`, `ON_HOLD`, `CLOSED`.
- Search open jobs by title or technology.
- Show all candidates eligible for a specific job.

### Eligibility engine
Before an application is accepted, every rule is evaluated:

1. Job status must be `OPEN`.
2. Application deadline must not have passed.
3. Candidate status must be `ACTIVE`.
4. The job must have at least one vacancy.
5. Candidate passing year must be greater than or equal to the job requirement.
6. Candidate percentage must be greater than or equal to the minimum percentage.
7. Candidate backlogs must not exceed the allowed maximum.
8. Candidate qualification must match one of the accepted qualifications.
9. Candidate technology or skills must overlap with the job technology.

Qualification and technology lists support comma, slash, pipe and semicolon separators. The token `ANY` accepts all qualifications. Matching is case-insensitive after normalization.

### Application workflow
Applications follow a guarded state machine:

```
APPLIED
  -> SHORTLISTED -> INTERVIEW_SCHEDULED -> INTERVIEWED
  -> SELECTED -> OFFERED -> JOINED
```

`REJECTED` and `WITHDRAWN` are terminal. `ON_HOLD` is a temporary branch that can return to `SHORTLISTED` or `INTERVIEW_SCHEDULED`. Invalid transitions throw `InvalidStateTransitionException`.

Duplicate applications for the same candidate and job are prevented by both a service check and a database unique constraint.

### Interviews
- Schedule rounds with round number, type, date/time, interviewer and mode.
- Modes: `IN_PERSON`, `VIDEO`, `TELEPHONIC`.
- Statuses: `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`.
- Submit one structured feedback record per interview with technical, communication and attitude ratings (1-5).
- Feedback result: `PASS`, `FAIL`, `ON_HOLD`.
- Scheduling moves the application to `INTERVIEW_SCHEDULED`; recording a result moves it to `INTERVIEWED`.

### Offers and placements
- Create an offer only for a `SELECTED` application.
- Candidate can accept or decline an offer.
- Staff can revoke a pending offer with a reason.
- Record joining for an accepted offer; this creates a placement, marks the application `JOINED` and the candidate `PLACED`.
- Mark a placement as `COMPLETED`.

### Reports
- Placement dashboard counts.
- Application pipeline by status.
- Job-wise application statistics.
- Company-wise hiring statistics.
- Placement report with package statistics.
- One-click export of all reports to the `exports/` directory as CSV.

### Security and audit
- Passwords hashed with PBKDF2 (salt + iterations, stored as a single encoded string).
- Role-based menu access: destructive master-data operations are restricted to `ADMIN`.
- Every workflow change writes an `audit_log` row with entity, old status, new status, actor and details.
- Audit history is visible to administrators and exportable to CSV.

---

## 3. Architecture

```
Main
 └── ui (ConsoleApplication, StaffMenu, CandidateMenu, ConsoleView)
      └── service (business rules, workflow, eligibility, reports)
           └── dao (JDBC persistence)
                └── model (records and enums)
                     └── MySQL

config   -> ApplicationContext (manual dependency wiring)
utility  -> DBConnection, Db, Jdbc, PasswordUtil, ValidationUtil, CsvUtil, ...
db       -> DatabaseInitializer, SqlScriptRunner
exception-> domain-specific unchecked exceptions
```

Design notes:

- Models are Java records, which keeps DTO-style classes immutable and concise.
- `Db.read(...)` and `Db.transaction(...)` centralize connection handling, commit/rollback and exception translation.
- Services hold business rules; DAOs contain only SQL and row mapping.
- Enums own their allowed transitions, so workflow rules are defined in one place.
- Exceptions are unchecked and extend `RPMSException`, keeping service signatures clean.
- Dependencies are wired explicitly in `ApplicationContext`, avoiding a framework for a console application.

### Package layout

```
src/main/java/com/kiranacademy/rpms/
  Main.java
  config/      AppConfig, ApplicationContext
  dao/         UserDAO, CandidateDAO, CompanyDAO, JobDAO, ApplicationDAO,
               InterviewDAO, FeedbackDAO, OfferDAO, PlacementDAO,
               AuditDAO, ReportDAO
  db/          DatabaseInitializer, SqlScriptRunner
  exception/   RPMSException and specific subclasses
  model/       Records and status enums
  service/     AuthService, CandidateService, CompanyService, JobService,
               ApplicationService, InterviewService, OfferService,
               PlacementService, EligibilityEngine, EligibilityResult,
               AuditService, ReportService
  ui/          ConsoleApplication, StaffMenu, CandidateMenu, ConsoleView
  utility/     DBConnection, Db, Jdbc, AppConfig, PasswordUtil, DateUtil,
               ValidationUtil, CsvUtil, InputUtil, AppLogger
src/test/java/com/kiranacademy/rpms/
  EligibilityEngineTest, WorkflowTransitionTest,
  PasswordUtilTest, ValidationUtilTest
database/
  schema.sql, sample_data.sql
web/
  index.html, login.html, candidates.html, companies.html, jobs.html,
  applications.html, interviews.html, placements.html, css/styles.css
```

---

## 4. Database design

Database name: `rpms` (UTF-8, InnoDB, foreign keys and check constraints).

| Table | Purpose |
| --- | --- |
| `candidates` | Candidate profiles, academics, skills and status |
| `companies` | Partner companies and hiring contacts |
| `jobs` | Openings with eligibility criteria and deadlines |
| `users` | Login accounts with roles and optional candidate link |
| `applications` | Candidate-to-job applications and workflow status |
| `interviews` | Interview rounds per application |
| `interview_feedback` | One scorecard per interview |
| `offers` | Offers per selected application |
| `placements` | Final placement record per candidate |
| `audit_log` | Status-change and action history |

Key integrity rules:

- Unique candidate registration number and email.
- Unique company name and unique job title per company.
- Unique application per candidate and job.
- Unique round number per application.
- Unique feedback per interview.
- Unique offer per application.
- Unique placement per candidate and per offer.
- Foreign keys from applications, interviews, feedback, offers, placements and users.

---

## 5. Setup

### Prerequisites

- JDK 17 or newer (`java -version`).
- Maven 3.9 or newer (`mvn -version`).
- MySQL 8.x running locally on port `3306`.

### Database configuration

Defaults are stored in `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/rpms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8
db.user=root
db.password=root
db.driver=com.mysql.cj.jdbc.Driver
```

You can override these without editing the file:

```bat
set RPMS_DB_URL=jdbc:mysql://localhost:3306/rpms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
set RPMS_DB_USER=root
set RPMS_DB_PASSWORD=your_password
```

On macOS/Linux use `export` instead of `set`.

### Initialize the database

The initializer connects to the MySQL server, creates the `rpms` database, runs `schema.sql` and then loads sample data if requested:

```bat
mvn -q package
java -jar target/rpms.jar --init-db
```

Schema only, no sample data:

```bat
java -jar target/rpms.jar --init-db --no-sample
```

You can also run the scripts manually:

```bat
mysql -u root -p < database\schema.sql
mysql -u root -p < database\sample_data.sql
```

After initialization, start the application:

```bat
java -jar target/rpms.jar
```

> The console UI uses plain text menus. When running from an IDE, enable UTF-8 console output if you want box-drawing characters to render correctly.

---

## 6. Demo accounts

`--init-db` creates these accounts automatically (passwords are PBKDF2-hashed in the database):

| Role | Username | Password |
| --- | --- | --- |
| Administrator | `admin` | `Admin@123` |
| Recruiter | `recruiter` | `Recruit@123` |
| Candidate | `candidate` | `Candidate@123` |

The candidate account is linked to the first candidate row in the database (with sample data, Aarav Sharma, `KA2026001`). Change the password from the menu after first login if needed.

---

## 7. Sample data

The sample script loads:

- 4 companies: TechNova Solutions, DataBridge Systems, CloudSprint Labs, FinEdge Technologies.
- 6 candidates across B.Tech, BCA, MCA and B.Sc backgrounds.
- 6 job openings, including one role on hold.
- 5 applications spread across `APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED` and `OFFERED`.
- 1 scheduled interview for Rohan Verma.
- 1 pending offer for Meera Joshi.
- No seed audit rows; audit history is created by real workflow actions.

The script uses `INSERT IGNORE` and relative `CURDATE()` deadlines, so it can be re-run safely.

---

## 8. Demo walkthrough

1. Sign in as `admin` or `recruiter`.
2. Open **Candidate management -> View all candidates** and note a candidate id.
3. Open **Job openings -> View open jobs** and note a job id.
4. Open **Job openings -> Show eligible candidates for a job** to see the eligibility engine in action.
5. Sign in as `candidate` (or stay as staff for pipeline actions).
6. As the candidate, use **Check eligibility for a job**, then **Apply for a job**.
7. As staff, open **Applications and shortlisting** and shortlist the application.
8. Open **Interview scheduling and feedback**, schedule a round, then submit feedback with result `PASS`.
9. Select the application, then open **Offers and placements** and create an offer.
10. As the candidate, use **My offers -> Respond to an offer** and accept.
11. As staff, use **Record candidate joining and placement** to create the placement.
12. Open **Reports and CSV export** and run the dashboard, pipeline and CSV export.

---

## 9. Console menus

### Staff (ADMIN / RECRUITER)

1. Candidate management
2. Company management
3. Job openings
4. Applications and shortlisting
5. Interview scheduling and feedback
6. Offers and placements
7. Reports and CSV export
8. Audit trail (ADMIN only)
9. Change password
0. Logout

### Candidate portal

1. Browse open job openings
2. Check eligibility for a job
3. Apply for a job
4. My applications
5. Withdraw an application
6. My interviews
7. My interview feedback
8. My offers
9. Respond to an offer
10. My placement
11. Update my profile
12. Change password
0. Logout

---

## 10. Build and test

Compile:

```bat
mvn -q -DskipTests compile
```

Run tests:

```bat
mvn -q test
```

Package the executable jar:

```bat
mvn -q package
```

Tests cover:

- Eligibility rule combinations and boundary values.
- Application workflow transitions, including invalid transitions.
- PBKDF2 hashing and password verification.
- Input validation helpers.

---

## 11. HTML/CSS UI prototypes

Open any file in `web/` directly in a browser:

- `web/login.html` - sign-in screen.
- `web/index.html` - placement dashboard.
- `web/candidates.html` - candidate directory and profile details.
- `web/companies.html` - partner companies and recruiting drives.
- `web/jobs.html` - job opening cards with eligibility criteria.
- `web/applications.html` - kanban recruitment pipeline.
- `web/interviews.html` - schedule, feedback scorecard and interview register.
- `web/placements.html` - placement outcomes and package distribution.

These pages are static prototypes for presentation. They do not call the Java backend. `web/css/styles.css` is shared by all pages.

---

## 12. Error handling and edge cases

- Invalid eligibility attempts return a reason list instead of creating an application.
- Duplicate applications are rejected before the unique constraint is hit.
- Workflow bypass attempts are rejected by both the service and the enum transition rules.
- Database errors are translated into `DataAccessException` with context.
- Transactions roll back on failure through `Db.transaction(...)`.
- Input readers validate ranges, enums, dates and required values.
- A candidate cannot withdraw or view another candidate's application through the candidate portal.
- Offers cannot be created for applications that are not selected; joinings require an accepted offer.

---

## 13. Known limitations and future work

- The application is a console client; the HTML/CSS screens are prototypes only.
- No REST API, web server or session management is included.
- Concurrent seat allocation for vacancies is not implemented; eligibility checks vacancies but does not reserve one atomically.
- There is no email or notification integration.
- The sample candidate login is linked to the first candidate row; a full deployment would create one account per candidate.
- Future work could add a Spring Boot REST layer, a real frontend, pagination, file uploads for resumes and scheduled offer-expiry jobs.

---

## 14. Troubleshooting

**`Communications link failure` or `Access denied`**

- Confirm MySQL is running on `localhost:3306`.
- Check `db.properties` or the `RPMS_DB_*` environment variables.
- Ensure the user has permission to create databases.

**`Unknown database 'rpms'`**

- Run `java -jar target/rpms.jar --init-db` first, or execute `database/schema.sql` manually.

**Console characters look garbled on Windows**

- Use Windows Terminal or run `chcp 65001` before starting the application.
- The application logic does not depend on box-drawing characters.

**Reset all data**

```sql
DROP DATABASE rpms;
```

Then run `java -jar target/rpms.jar --init-db` again.
