-- =====================================================================
-- Public notices (home-page notice bar), managed by TVET QC (role 22).
-- Notices are stored in the existing tbl_notification_dtls with
-- notification_type = 0, so no new table is needed.
-- This adds the sidebar menu "Public Notices" -> /public-notices for role 22.
-- Safe to re-run. Log out and back in afterwards to see the menu.
-- =====================================================================

INSERT INTO tbl_privilege (privilege_name, route_name, dis_play_order, is_display, menu_icon, description, parent_id)
SELECT 'Public Notices', '/public-notices', 91, 1, 'Campaign',
       'Create, edit and delete notices shown on the public home page', 0
WHERE NOT EXISTS (SELECT 1 FROM tbl_privilege WHERE route_name = '/public-notices');

INSERT INTO tbl_role_privilege (role_id, privilege_id)
SELECT 22, p.id FROM tbl_privilege p
WHERE p.route_name = '/public-notices'
  AND NOT EXISTS (SELECT 1 FROM tbl_role_privilege rp WHERE rp.role_id = 22 AND rp.privilege_id = p.id);
