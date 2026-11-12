-- =====================================================================
-- Feedback / Complain
--   1. tbl_feedback_complaint (public submissions)
--   2. Sidebar menu "Feedback & Complaints" -> /feedback-complaints
--      for EVERY role (everyone logged in sees feedback; the Complaints
--      tab inside the page is shown to TVET QC, role 22, only).
-- Safe to re-run. Log out and back in afterwards to see the menu.
-- =====================================================================

CREATE TABLE IF NOT EXISTS tbl_feedback_complaint (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    reference_no       VARCHAR(30)  NULL,
    type               VARCHAR(20)  NOT NULL,      -- FEEDBACK | COMPLAINT
    anonymous          BIT(1)       NULL,
    name               VARCHAR(200) NULL,
    email              VARCHAR(200) NULL,
    contact_no         VARCHAR(30)  NULL,
    submitter_role     VARCHAR(50)  NULL,
    category           VARCHAR(50)  NULL,
    rating             INT          NULL,
    experience         TEXT         NULL,
    suggestions        TEXT         NULL,
    recommend          BIT(1)       NULL,
    incident_date      DATE         NULL,
    against_person     VARCHAR(200) NULL,
    institution        VARCHAR(300) NULL,
    urgency            VARCHAR(20)  NULL,
    incident_details   TEXT         NULL,
    desired_resolution TEXT         NULL,
    status             VARCHAR(20)  NULL,          -- SUBMITTED | UNDER_REVIEW | RESOLVED | REJECTED
    action_taken       TEXT         NULL,
    action_by          VARCHAR(100) NULL,
    action_at          DATETIME(6)  NULL,
    created_at         DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feedback_complaint_ref (reference_no),
    KEY idx_feedback_complaint_type (type, created_at)
);

INSERT INTO tbl_privilege (privilege_name, route_name, dis_play_order, is_display, menu_icon, description, parent_id)
SELECT 'Feedback & Complaints', '/feedback-complaints', 92, 1, 'Feedback',
       'Feedback from the public (all users); complaints and actions (TVET QC)', 0
WHERE NOT EXISTS (SELECT 1 FROM tbl_privilege WHERE route_name = '/feedback-complaints');

-- every role gets the menu
INSERT INTO tbl_role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM tbl_role r
JOIN tbl_privilege p ON p.route_name = '/feedback-complaints'
WHERE NOT EXISTS (SELECT 1 FROM tbl_role_privilege rp WHERE rp.role_id = r.id AND rp.privilege_id = p.id);

-- 3. Complaint attachments (any number; 10 MB each, 50 MB total; stored under
--    ${file.store.path}/feedback-complaint/<reference no>/)
CREATE TABLE IF NOT EXISTS tbl_feedback_complaint_file (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    complaint_id BIGINT        NOT NULL,
    file_name    VARCHAR(500)  NULL,
    content_type VARCHAR(200)  NULL,
    file_size    BIGINT        NULL,
    upload_url   VARCHAR(1000) NULL,
    created_at   DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_feedback_complaint_file (complaint_id)
);
