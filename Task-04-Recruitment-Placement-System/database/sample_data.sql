-- ============================================================
-- HireTrack sample data (safe to run multiple times)
-- ============================================================

USE rpms;

INSERT IGNORE INTO companies (name, industry, location, website, contact_person, contact_email, status) VALUES
('TechNova Solutions', 'Information Technology', 'Bengaluru', 'https://technova.example.com', 'Priya Menon', 'priya.menon@technova.example.com', 'ACTIVE'),
('DataBridge Systems', 'Data & Analytics', 'Hyderabad', 'https://databridge.example.com', 'Arjun Rao', 'arjun.rao@databridge.example.com', 'ACTIVE'),
('CloudSprint Labs', 'Cloud Services', 'Pune', 'https://cloudsprint.example.com', 'Neha Kulkarni', 'neha.k@cloudsprint.example.com', 'ACTIVE'),
('FinEdge Technologies', 'FinTech', 'Mumbai', 'https://finedge.example.com', 'Vikram Shah', 'vikram.shah@finedge.example.com', 'ACTIVE');

INSERT IGNORE INTO candidates
(registration_no, first_name, last_name, email, phone, qualification, passing_year, percentage, technology, backlogs, skills, status) VALUES
('KA2026001', 'Aarav', 'Sharma', 'aarav.sharma@example.com', '9876543210', 'B.Tech', 2025, 78.50, 'Java', 0, 'Java, Spring Boot, MySQL, REST API', 'ACTIVE'),
('KA2026002', 'Diya', 'Patel', 'diya.patel@example.com', '9876543211', 'B.Tech', 2025, 68.00, 'Python', 1, 'Python, SQL, Pandas, Excel', 'ACTIVE'),
('KA2026003', 'Rohan', 'Verma', 'rohan.verma@example.com', '9876543212', 'BCA', 2024, 72.00, 'Java', 0, 'Java, Selenium, Testing, SQL', 'ACTIVE'),
('KA2026004', 'Ishita', 'Nair', 'ishita.nair@example.com', '9876543213', 'MCA', 2025, 81.20, 'Java', 0, 'Java, Hibernate, Spring, MySQL', 'ACTIVE'),
('KA2026005', 'Kabir', 'Singh', 'kabir.singh@example.com', '9876543214', 'B.Tech', 2023, 59.50, 'Testing', 2, 'Manual Testing, SQL, JIRA', 'ACTIVE'),
('KA2026006', 'Meera', 'Joshi', 'meera.joshi@example.com', '9876543215', 'B.Sc', 2025, 75.00, 'Python', 0, 'Python, AWS, Linux, Communication', 'ACTIVE');

INSERT IGNORE INTO jobs
(company_id, title, technology, qualification_required, min_percentage, passing_year_from,
 max_backlogs, vacancies, package_lpa, location, status, application_deadline) VALUES
((SELECT id FROM companies WHERE name = 'TechNova Solutions'),
 'Java Backend Developer', 'Java, Spring Boot, MySQL', 'B.Tech, BE, MCA', 65.00, 2024, 0, 4, 7.50,
 'Bengaluru', 'OPEN', DATE_ADD(CURDATE(), INTERVAL 45 DAY)),
((SELECT id FROM companies WHERE name = 'TechNova Solutions'),
 'QA Automation Engineer', 'Selenium, Java, Testing', 'B.Tech, BCA, MCA', 60.00, 2023, 1, 2, 5.00,
 'Pune', 'OPEN', DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
((SELECT id FROM companies WHERE name = 'DataBridge Systems'),
 'Data Analyst', 'Python, SQL, Excel', 'B.Sc, B.Tech, MCA', 70.00, 2024, 0, 3, 6.50,
 'Hyderabad', 'OPEN', DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
((SELECT id FROM companies WHERE name = 'CloudSprint Labs'),
 'Cloud Support Associate', 'AWS, Linux, Networking', 'B.Tech, BCA', 60.00, 2023, 1, 5, 4.80,
 'Remote', 'OPEN', DATE_ADD(CURDATE(), INTERVAL 60 DAY)),
((SELECT id FROM companies WHERE name = 'FinEdge Technologies'),
 'Java Developer', 'Java, Spring, Hibernate, MySQL', 'B.Tech, MCA', 75.00, 2025, 0, 2, 9.00,
 'Mumbai', 'OPEN', DATE_ADD(CURDATE(), INTERVAL 15 DAY)),
((SELECT id FROM companies WHERE name = 'FinEdge Technologies'),
 'Business Analyst', 'Communication, Excel, SQL', 'BBA, B.Tech, MBA', 65.00, 2023, 0, 2, 6.00,
 'Gurugram', 'ON_HOLD', DATE_ADD(CURDATE(), INTERVAL 40 DAY));

-- Demo applications across the recruitment pipeline
INSERT IGNORE INTO applications (candidate_id, job_id, status, applied_at) VALUES
((SELECT id FROM candidates WHERE email = 'aarav.sharma@example.com'),
 (SELECT id FROM jobs WHERE title = 'Java Backend Developer'), 'APPLIED', DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM candidates WHERE email = 'ishita.nair@example.com'),
 (SELECT id FROM jobs WHERE title = 'Java Backend Developer'), 'SHORTLISTED', DATE_SUB(NOW(), INTERVAL 8 DAY)),
((SELECT id FROM candidates WHERE email = 'diya.patel@example.com'),
 (SELECT id FROM jobs WHERE title = 'Data Analyst'), 'APPLIED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
((SELECT id FROM candidates WHERE email = 'rohan.verma@example.com'),
 (SELECT id FROM jobs WHERE title = 'QA Automation Engineer'), 'INTERVIEW_SCHEDULED', DATE_SUB(NOW(), INTERVAL 6 DAY)),
((SELECT id FROM candidates WHERE email = 'meera.joshi@example.com'),
 (SELECT id FROM jobs WHERE title = 'Cloud Support Associate'), 'OFFERED', DATE_SUB(NOW(), INTERVAL 12 DAY));

INSERT IGNORE INTO interviews
(application_id, round_no, round_type, scheduled_at, interviewer, mode, status) VALUES
((SELECT a.id FROM applications a
  JOIN candidates c ON c.id = a.candidate_id
  JOIN jobs j ON j.id = a.job_id
  WHERE c.email = 'rohan.verma@example.com' AND j.title = 'QA Automation Engineer'),
 1, 'Technical Round', DATE_ADD(NOW(), INTERVAL 2 DAY), 'Priya Menon', 'VIDEO', 'SCHEDULED');

INSERT IGNORE INTO offers (application_id, offered_ctc, offer_date, status, joining_date) VALUES
((SELECT a.id FROM applications a
  JOIN candidates c ON c.id = a.candidate_id
  JOIN jobs j ON j.id = a.job_id
  WHERE c.email = 'meera.joshi@example.com' AND j.title = 'Cloud Support Associate'),
 4.80, CURDATE(), 'PENDING', DATE_ADD(CURDATE(), INTERVAL 30 DAY));
