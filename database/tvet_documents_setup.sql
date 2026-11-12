-- =====================================================================
-- Publications & Forms (public downloads, managed by TVET QC role 22)
-- Run once on db_tvet_mis. Safe to re-run.
--   1. Creates tbl_publication_forms (dev profile also auto-creates it via
--      ddl-auto=update; prod uses validate, so run this there).
--   2. Adds the sidebar menu "Publications & Forms" -> /tvetqc/documents
--      and assigns it to role 22 (TVET QC) only.
-- Files are stored under ${file.store.path}/tvet-documents/<category>/
-- =====================================================================

-- 1. Table
CREATE TABLE IF NOT EXISTS tbl_publication_forms (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    category       VARCHAR(20)  NOT NULL,          -- PUBLICATION | FORM
    title          VARCHAR(500) NOT NULL,
    published_date DATE         NULL,
    remarks        TEXT         NULL,
    file_name      VARCHAR(500) NULL,
    content_type   VARCHAR(200) NULL,
    file_size      BIGINT       NULL,
    upload_url     VARCHAR(1000) NULL,
    created_by     VARCHAR(100) NULL,
    created_at     DATETIME(6)  NULL,
    updated_by     VARCHAR(100) NULL,
    updated_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_publication_forms_category (category, published_date)
);

-- 2. Menu (top-level) for TVET QC
INSERT INTO tbl_privilege (privilege_name, route_name, dis_play_order, is_display, menu_icon, description, parent_id)
SELECT 'Publications & Forms', '/tvetqc/documents', 90, 1, 'LibraryBooks',
       'Create, edit and delete public Publications and Forms', 0
WHERE NOT EXISTS (SELECT 1 FROM tbl_privilege WHERE route_name = '/tvetqc/documents');

INSERT INTO tbl_role_privilege (role_id, privilege_id)
SELECT 22, p.id
FROM tbl_privilege p
WHERE p.route_name = '/tvetqc/documents'
  AND NOT EXISTS (SELECT 1 FROM tbl_role_privilege rp
                  WHERE rp.role_id = 22 AND rp.privilege_id = p.id);
