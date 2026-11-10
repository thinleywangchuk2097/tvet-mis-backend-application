-- =====================================================================
-- TVET-MIS : Self Assessment Report (TVET Provider Accreditation)
-- Service ID : 19  (tbl_service_master -> 'Accreditation of TVET Programme')
--
-- Run this script ONCE on db_tvet_mis2026 before using the
-- /registration/self-assessment-report screen.
--
-- What it does
--   1. Creates the SAR tables (Hibernate ddl-auto=update also creates them in dev).
--   2. Seeds the Quality Standards checklist (service_id = 19) as a 3 level tree:
--        Criteria      (parent_id = 0)          description = '<Level>|<Allocated Score>'
--        Indicator     (parent_id = criteria)   description = indicator name
--        Sub-Indicator (parent_id = indicator)  description = allocated points (4)
--      The frontend reads these rows via /api/v1/common/get-quality-standards/19,
--      so names, weights and points can later be maintained directly in this table.
--   3. Sets the task list route for service 19.
--   4. Adds workflow rows (tbl_role_service) for service 19:
--        Institute submit (55) -> QAS role 7 verify (56) -> REC role 23 endorse (59)
--        -> TVETQC role 22 approve (57).  Reject (58) possible at every level.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. Tables
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_self_assessment_report_dtls` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_no` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `institute_id` int DEFAULT NULL,
  `service_id` int DEFAULT NULL,
  `status_id` int DEFAULT NULL,
  `total_score` double DEFAULT NULL,
  `grade` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `submitted_date` datetime(6) DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_by` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sar_application_no` (`application_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `tbl_self_assessment_report_quality_response` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `standard_id` bigint NOT NULL,
  `self_point` int DEFAULT NULL,
  `earned_point` int DEFAULT NULL,
  `justification` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`),
  KEY `FK_sar_qr_application_no` (`application_no`),
  CONSTRAINT `FK_sar_qr_application_no` FOREIGN KEY (`application_no`) REFERENCES `tbl_self_assessment_report_dtls` (`application_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `tbl_self_assessment_report_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_no` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `member_type` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ACCREDITOR | COORDINATOR',
  `user_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CID (accreditor)',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `designation` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_sar_member_application_no` (`application_no`),
  CONSTRAINT `FK_sar_member_application_no` FOREIGN KEY (`application_no`) REFERENCES `tbl_self_assessment_report_dtls` (`application_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- ---------------------------------------------------------------------
-- 3. Task list route for service 19
-- ---------------------------------------------------------------------
UPDATE tbl_service_master
   SET route = 'tasklist/view-self-assessment-report',
       service_name = COALESCE(service_name, 'Accreditation of TVET Programme')
 WHERE id = 19;

-- ---------------------------------------------------------------------
-- 4. Workflow (tbl_role_service) for service 19
-- ---------------------------------------------------------------------
INSERT INTO tbl_role_service (assigned_role_id, next_role_id, service_id, status_id, created_at)
SELECT x.assigned_role_id, x.next_role_id, 19, x.status_id, NOW(6)
  FROM (
        SELECT 7  AS assigned_role_id, NULL AS next_role_id, 55 AS status_id UNION ALL  -- submitted
        SELECT 7,  23,   56 UNION ALL                                                  -- QAS verified -> REC
        SELECT 7,  NULL, 58 UNION ALL                                                  -- QAS rejected
        SELECT 23, 22,   59 UNION ALL                                                  -- REC endorsed -> TVETQC
        SELECT 23, NULL, 58 UNION ALL                                                  -- REC rejected
        SELECT 22, NULL, 57 UNION ALL                                                  -- TVETQC approved
        SELECT 22, NULL, 58                                                            -- TVETQC rejected
       ) x
 WHERE NOT EXISTS (
        SELECT 1 FROM tbl_role_service r
         WHERE r.service_id = 19
           AND r.assigned_role_id = x.assigned_role_id
           AND r.status_id = x.status_id);
