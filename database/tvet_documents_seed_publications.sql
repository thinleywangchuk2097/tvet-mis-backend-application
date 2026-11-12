-- OPTIONAL: copies the 37 publications that were hard-coded in the old
-- PublicationIndex.jsx into tbl_publication_forms, WITHOUT files.
-- They appear on the public page as 'No file' until TVET QC opens each
-- one (Edit) and attaches the file. Skip this script to start empty.
-- Run after tvet_documents_setup.sql. Safe to re-run (skips existing titles).

INSERT INTO tbl_publication_forms (category, title, published_date, remarks, created_by, created_at)
SELECT x.category, x.title, x.published_date, x.remarks, 'migration', NOW(6) FROM (
    SELECT 'PUBLICATION' AS category, 'Guidelines For Accreditation of Courses 2011' AS title, DATE '2021-04-26' AS published_date, NULL AS remarks UNION ALL
    SELECT 'PUBLICATION', 'Regulations for Registration of Training Provider, 2014', DATE '2021-04-26', NULL UNION ALL
    SELECT 'PUBLICATION', 'NCS Operations Manual', DATE '2015-03-12', 'National Competency Standards Operations Manual' UNION ALL
    SELECT 'PUBLICATION', 'Institute proposal format', DATE '2022-04-21', 'Institute proposal format must be followed not limiting to information given in the format' UNION ALL
    SELECT 'PUBLICATION', 'Attendance Sheet', DATE '2022-05-31', 'Attendance form for onsite evaluation of TVET Program' UNION ALL
    SELECT 'PUBLICATION', 'Payment sheet', DATE '2022-06-01', 'Payment sheet for Accreditors' UNION ALL
    SELECT 'PUBLICATION', 'Revised Regulations for Training Providers 2023', DATE '2023-06-06', 'Revised Regulations for Registration of Training Providers 2023' UNION ALL
    SELECT 'PUBLICATION', 'TVET Qualification Pathways as per BQF', DATE '2023-06-06', 'TVET PATHWAYS AS PER BQF 2023' UNION ALL
    SELECT 'PUBLICATION', 'Revised Lesson Plan Form 2023', DATE '2023-09-01', 'Revised Session Plan Form 2023' UNION ALL
    SELECT 'PUBLICATION', 'Revised Task Sheet 2023', DATE '2023-09-01', 'Revised Task Sheet Form 2023' UNION ALL
    SELECT 'PUBLICATION', 'Trainee Practical Note', DATE '2023-09-01', 'Trainee Practical Note book sample' UNION ALL
    SELECT 'PUBLICATION', 'REVISED QMS Manual for Training Providers (Both Private and Public Institutes)', DATE '2023-12-18', NULL UNION ALL
    SELECT 'PUBLICATION', 'Guidelines for Curriculum Development', DATE '2023-12-28', 'Guidelines for Curriculum Development' UNION ALL
    SELECT 'PUBLICATION', 'Quality and Compliance Auditing Checklist 2023', DATE '2023-12-01', 'Audit Checklist December 2023' UNION ALL
    SELECT 'PUBLICATION', 'TOR for internal auditor', DATE '2024-05-09', 'Terms for Reference for QMS internal auditor' UNION ALL
    SELECT 'PUBLICATION', 'RPL Assessment Centre Accreditation Form', DATE '2024-07-24', 'On-site Physical Verification of Assessment Centre for Accreditation' UNION ALL
    SELECT 'PUBLICATION', 'Conflict of Interest Form', DATE '2024-07-01', 'Conflict of Interest form 2024' UNION ALL
    SELECT 'PUBLICATION', 'Course Profile Format 2024', DATE '2024-01-01', 'Course Profile Format revised 2024' UNION ALL
    SELECT 'PUBLICATION', 'Checklist for additional training group', DATE '2023-07-01', 'Checklist for additional training group' UNION ALL
    SELECT 'PUBLICATION', 'De-Registration Form', DATE '2024-08-30', 'Training Provider De-Registration Form' UNION ALL
    SELECT 'PUBLICATION', 'Checklist for additional training group', DATE '2024-08-30', 'Checklist for additional training group' UNION ALL
    SELECT 'PUBLICATION', 'TVET QMS Audit Checklist 2024', DATE '2024-09-12', 'TVET QMS Audit Checklist' UNION ALL
    SELECT 'PUBLICATION', 'Final QMS Audit Report', DATE '2024-09-12', NULL UNION ALL
    SELECT 'PUBLICATION', 'Physical Verification of Training Providers (New Establishment)', DATE '2024-11-12', 'On-site verification form (edited)' UNION ALL
    SELECT 'PUBLICATION', 'Accreditation check list 2024', DATE '2024-11-18', 'Revised checklist, 2024' UNION ALL
    SELECT 'PUBLICATION', 'TOR for Lead Trainer', DATE '2025-01-01', 'Terms for Reference to Lead Trainer 2025' UNION ALL
    SELECT 'PUBLICATION', 'Accreditation Checklist for Diploma BQF4', DATE '2024-03-07', 'Accreditation checklist for Diploma courses' UNION ALL
    SELECT 'PUBLICATION', 'TOR and Conflict of Interest', DATE '2025-08-16', 'Conflict Of Interest of Accreditors' UNION ALL
    SELECT 'PUBLICATION', 'Supplementary Educational Service Centre Format', DATE '2025-09-03', 'SES Centre Establishment Proposal Format' UNION ALL
    SELECT 'PUBLICATION', 'Driving Ground Checklist', DATE '2024-08-02', 'Driving Ground Checklist for DTIs' UNION ALL
    SELECT 'PUBLICATION', 'SES Centre Onsite Verification Form Oct 2025', DATE '2025-10-16', 'The checklist to be used during onsite verification of SES centre' UNION ALL
    SELECT 'PUBLICATION', '2Draft TVET Quality and Qualifications Regulation 2025', DATE '2025-10-30', 'Draft Regulation for Consultation' UNION ALL
    SELECT 'PUBLICATION', 'Self-assessment form for TVET Program Accreditation', DATE '2025-11-03', 'This self-assessment of TVET program to be used by the TVET providers prior to on-site evaluation of the TVET program conducted by the TVET QC' UNION ALL
    SELECT 'PUBLICATION', 'Post-accreditation monitoring and audit form', DATE '2025-12-09', 'This form to be used by the monitoring officer, TVET QC, BQPCA' UNION ALL
    SELECT 'PUBLICATION', 'Driving Ground Drawing sample', DATE '2025-12-11', 'To be followed by the training providers' UNION ALL
    SELECT 'PUBLICATION', 'POST ASSESSMENT REPORT AND VALIDATION', DATE '2026-01-05', 'This post assessment report to be prepared by the ASSESSMENT COORDINATOR AND ASSESSORS and submit to TVET QC' UNION ALL
    SELECT 'PUBLICATION', 'Application for Qualification Accreditation of TVET program level 2-5', DATE '2025-02-01', NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM tbl_publication_forms t WHERE t.category = 'PUBLICATION' AND t.title = x.title);
