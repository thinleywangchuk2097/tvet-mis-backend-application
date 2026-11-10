package com.moesd.tvet.mis.backend.application.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

/**
 * Read-only aggregate queries for staff dashboards (e.g. QAS Level 1).
 * Applications are limited to the services a role works on (tbl_role_service)
 * and to a date range on the workflow action date.
 */
@Repository
@RequiredArgsConstructor
public class RoleDashboardRepository {

	private final JdbcTemplate jdbc;

	/** Task status id for "completed" (same id other services use). */
	private static final int TASK_COMPLETED = 20;

	/**
	 * WHERE condition on tbl_workflow_dtls w: the role's services (tbl_role_service) within [from, to].
	 * roleId <= 0 means all services (system-wide, e.g. admin).
	 */
	private String scope(int roleId) {
		return (roleId > 0
				? " w.service_id IN (SELECT DISTINCT rs.service_id FROM tbl_role_service rs"
						+ " WHERE rs.assigned_role_id = ? OR rs.next_role_id = ?) AND"
				: "")
				+ " w.action_date >= ? AND w.action_date < ? ";
	}

	private Object[] scopeArgs(int roleId, LocalDate from, LocalDate to) {
		Object f = java.sql.Date.valueOf(from);
		Object t = java.sql.Date.valueOf(to.plusDays(1));
		return roleId > 0 ? new Object[] { roleId, roleId, f, t } : new Object[] { f, t };
	}

	// ---------------- system-wide TVET statistics ----------------

	public List<Map<String, Object>> institutesByOwnership() {
		return jdbc.queryForList("""
				SELECT COALESCE(o.name, 'Not specified') AS name, COUNT(*) AS value
				FROM tbl_institute_registration_dtls i
				LEFT JOIN tbl_dropdown_child_master o ON o.id = i.ownership_type_id
				GROUP BY o.name
				ORDER BY value DESC
				""");
	}

	public long count(String sql) {
		Long n = jdbc.queryForObject(sql, Long.class);
		return n == null ? 0 : n;
	}

	public List<Map<String, Object>> enrolmentByProgrammeType() {
		return jdbc.queryForList("""
				SELECT COALESCE(sm.service_name, 'Other') AS name, COUNT(*) AS value,
				       COALESCE(SUM(CASE WHEN tr.certificate_no IS NOT NULL AND tr.certificate_no <> ''
				                    THEN 1 ELSE 0 END), 0) AS certified
				FROM tbl_programme_trainee_enrollment_dtls tr
				JOIN tbl_programme_announcement_dtls a ON a.application_no = tr.course_enrol_app_no
				LEFT JOIN tbl_service_master sm ON sm.id = a.service_id
				GROUP BY sm.service_name
				ORDER BY value DESC
				""");
	}

	// ---------------- applications for the role ----------------

	public long applicationCount(int roleId, LocalDate from, LocalDate to) {
		Long n = jdbc.queryForObject(
				"SELECT COUNT(DISTINCT w.application_no) FROM tbl_workflow_dtls w WHERE" + scope(roleId), Long.class,
				scopeArgs(roleId, from, to));
		return n == null ? 0 : n;
	}

	public List<Map<String, Object>> applicationsByStatus(int roleId, LocalDate from, LocalDate to) {
		return jdbc.queryForList("""
				SELECT COALESCE(st.name, 'Unknown') AS name, COUNT(DISTINCT w.application_no) AS value
				FROM tbl_workflow_dtls w
				LEFT JOIN tbl_dropdown_child_master st ON st.id = w.status_id
				WHERE """ + scope(roleId) + """
				GROUP BY st.name
				ORDER BY value DESC
				""", scopeArgs(roleId, from, to));
	}

	public List<Map<String, Object>> applicationsByService(int roleId, LocalDate from, LocalDate to) {
		return jdbc.queryForList("""
				SELECT COALESCE(s.service_name, CONCAT('Service ', w.service_id)) AS name,
				       COUNT(DISTINCT w.application_no) AS value
				FROM tbl_workflow_dtls w
				LEFT JOIN tbl_service_master s ON s.id = w.service_id
				WHERE """ + scope(roleId) + """
				GROUP BY s.service_name, w.service_id
				ORDER BY value DESC
				""", scopeArgs(roleId, from, to));
	}

	public List<Map<String, Object>> applicationsByMonth(int roleId, LocalDate from, LocalDate to) {
		return jdbc.queryForList("SELECT DATE_FORMAT(w.action_date, '%Y-%m') AS ym, "
				+ "COUNT(DISTINCT w.application_no) AS value FROM tbl_workflow_dtls w WHERE" + scope(roleId)
				+ " GROUP BY ym",
				scopeArgs(roleId, from, to));
	}

	public List<Map<String, Object>> recentApplications(int roleId, LocalDate from, LocalDate to) {
		return jdbc.queryForList("""
				SELECT w.application_no, w.application_name, w.action_date, w.wf_remarks,
				       s.service_name, st.name AS status_name
				FROM tbl_workflow_dtls w
				LEFT JOIN tbl_service_master s ON s.id = w.service_id
				LEFT JOIN tbl_dropdown_child_master st ON st.id = w.status_id
				WHERE """ + scope(roleId) + """
				ORDER BY w.action_date DESC
				LIMIT 300
				""", scopeArgs(roleId, from, to));
	}

	/** Open tasks currently sitting with the role: unclaimed (group) vs claimed. */
	public Map<String, Object> pendingTasks(int roleId) {
		return jdbc.queryForMap("""
				SELECT COUNT(DISTINCT t.application_no) AS total,
				       COUNT(DISTINCT CASE WHEN t.assigned_user_id IS NULL OR t.assigned_user_id = ''
				                      THEN t.application_no END) AS unclaimed,
				       COUNT(DISTINCT CASE WHEN t.assigned_user_id IS NOT NULL AND t.assigned_user_id <> ''
				                      THEN t.application_no END) AS claimed
				FROM tbl_task_dtls t
				WHERE (? <= 0 OR t.assigned_role_id = ?)
				  AND (t.task_status_id IS NULL OR t.task_status_id <> ?)
				""", roleId, roleId, TASK_COMPLETED);
	}

	// ---------------- system administration (admin dashboard) ----------------

	public Map<String, Object> userTotals() {
		return jdbc.queryForMap("""
				SELECT COUNT(*) AS total,
				       COALESCE(SUM(CASE WHEN status_id = '1' THEN 1 ELSE 0 END), 0) AS active,
				       COALESCE(SUM(CASE WHEN YEAR(created_at) = YEAR(CURDATE()) THEN 1 ELSE 0 END), 0) AS new_this_year,
				       COALESCE(SUM(CASE WHEN created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01') THEN 1 ELSE 0 END), 0)
				         AS new_this_month,
				       COALESCE(SUM(CASE WHEN lock_until IS NOT NULL AND lock_until > NOW() THEN 1 ELSE 0 END), 0)
				         AS locked
				FROM tbl_user
				""");
	}

	public List<Map<String, Object>> usersByRole() {
		return jdbc.queryForList("""
				SELECT r.role_name AS name, COUNT(DISTINCT ur.user_id) AS value
				FROM tbl_role r
				LEFT JOIN tbl_user_role ur ON ur.role_id = r.id
				GROUP BY r.id, r.role_name
				ORDER BY value DESC
				""");
	}

	/** New user registrations per month (yyyy-MM) within [from, to]. */
	public List<Map<String, Object>> usersByMonth(LocalDate from, LocalDate to) {
		return jdbc.queryForList("""
				SELECT DATE_FORMAT(created_at, '%Y-%m') AS ym, COUNT(*) AS value
				FROM tbl_user
				WHERE created_at >= ? AND created_at < ?
				GROUP BY ym
				""", java.sql.Date.valueOf(from), java.sql.Date.valueOf(to.plusDays(1)));
	}

	public Map<String, Object> roleTotals() {
		return jdbc.queryForMap("""
				SELECT COUNT(*) AS total,
				       COALESCE(SUM(CASE WHEN status_id = '1' THEN 1 ELSE 0 END), 0) AS active
				FROM tbl_role
				""");
	}

	public Map<String, Object> dropdownTotals() {
		return jdbc.queryForMap("""
				SELECT (SELECT COUNT(*) FROM tbl_dropdown_master) AS masters,
				       (SELECT COUNT(*) FROM tbl_dropdown_master WHERE status_id = '1') AS active_masters,
				       (SELECT COUNT(*) FROM tbl_dropdown_child_master) AS values_total
				""");
	}

	public Map<String, Object> serviceTotals() {
		return jdbc.queryForMap("""
				SELECT COUNT(*) AS total,
				       COALESCE(SUM(CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END), 0) AS active
				FROM tbl_service_master
				""");
	}

	/** Open tasks grouped by the role they are waiting with. */
	public List<Map<String, Object>> pendingTasksByRole() {
		return jdbc.queryForList("""
				SELECT COALESCE(r.role_name, CONCAT('Role ', t.assigned_role_id)) AS name,
				       COUNT(DISTINCT t.application_no) AS value,
				       COUNT(DISTINCT CASE WHEN t.assigned_user_id IS NULL OR t.assigned_user_id = ''
				                      THEN t.application_no END) AS unclaimed
				FROM tbl_task_dtls t
				LEFT JOIN tbl_role r ON r.id = t.assigned_role_id
				WHERE t.task_status_id IS NULL OR t.task_status_id <> ?
				GROUP BY t.assigned_role_id, r.role_name
				ORDER BY value DESC
				""", TASK_COMPLETED);
	}

	// ---------------- TTTRC dashboards (TOT + curriculum) ----------------

	/** TOT announcements with applied / selected / certified counts (newest first). */
	public List<Map<String, Object>> totAnnouncements() {
		return jdbc.queryForList("""
				SELECT a.id, a.application_no, p.program_name, p.program_code,
				       a.application_start_date, a.application_end_date,
				       a.program_start_date, a.program_end_date,
				       a.max_participants, a.venue,
				       (SELECT COUNT(*) FROM tbl_tot_program_trainer_applied_dtls x
				         WHERE x.program_announcement_id = a.id) AS applied,
				       (SELECT COUNT(*) FROM tbl_tot_program_trainer_applied_dtls x
				         WHERE x.program_announcement_id = a.id AND x.is_selected = 1) AS selected,
				       (SELECT COUNT(*) FROM tbl_tot_program_trainer_applied_dtls x
				         WHERE x.program_announcement_id = a.id AND x.certificate_no IS NOT NULL) AS certified
				FROM tbl_tot_announcement_dtls a
				LEFT JOIN tbl_tot_program_dtls p ON p.id = a.program_id
				ORDER BY a.program_start_date DESC, a.id DESC
				LIMIT 200
				""");
	}

	/** TOT trainers by result (Competent / Not Competent / not assessed yet). */
	public List<Map<String, Object>> totResults() {
		return jdbc.queryForList("""
				SELECT COALESCE(NULLIF(result, ''), 'Not assessed') AS name, COUNT(*) AS value
				FROM tbl_tot_program_trainer_applied_dtls
				GROUP BY COALESCE(NULLIF(result, ''), 'Not assessed')
				ORDER BY value DESC
				""");
	}

	/** TOT trainers by grade (assessed only). */
	public List<Map<String, Object>> totGrades() {
		return jdbc.queryForList("""
				SELECT grade AS name, COUNT(*) AS value
				FROM tbl_tot_program_trainer_applied_dtls
				WHERE grade IS NOT NULL AND grade <> ''
				GROUP BY grade
				ORDER BY grade
				""");
	}

	/** Institutes sending the most trainers to TOT (top 8). */
	public List<Map<String, Object>> totByInstitute() {
		return jdbc.queryForList("""
				SELECT COALESCE(ir.proposed_institute_name, 'Not specified') AS name, COUNT(*) AS value
				FROM tbl_tot_program_trainer_applied_dtls x
				LEFT JOIN tbl_institute_registration_dtls ir ON ir.institute_id = x.institute_id
				GROUP BY ir.proposed_institute_name
				ORDER BY value DESC
				LIMIT 8
				""");
	}

	public Map<String, Object> totTotals() {
		return jdbc.queryForMap("""
				SELECT (SELECT COUNT(*) FROM tbl_tot_program_dtls) AS programs,
				       (SELECT COUNT(*) FROM tbl_tot_announcement_dtls) AS announcements,
				       COUNT(*) AS applied,
				       COUNT(DISTINCT trainer_id) AS trainers,
				       COALESCE(SUM(CASE WHEN is_selected = 1 THEN 1 ELSE 0 END), 0) AS selected,
				       COALESCE(SUM(CASE WHEN certificate_no IS NOT NULL THEN 1 ELSE 0 END), 0) AS certified
				FROM tbl_tot_program_trainer_applied_dtls
				""");
	}

	/** Curriculum applications grouped by a dropdown / service name. */
	public List<Map<String, Object>> curriculumByStatus() {
		return jdbc.queryForList("""
				SELECT COALESCE(s.name, 'Pending') AS name, c.status_id AS id, COUNT(*) AS value
				FROM tbl_curriculum_development c
				LEFT JOIN tbl_dropdown_child_master s ON s.id = c.status_id
				GROUP BY s.name, c.status_id
				ORDER BY value DESC
				""");
	}

	public List<Map<String, Object>> curriculumByType() {
		return jdbc.queryForList("""
				SELECT COALESCE(sm.service_name, 'Not specified') AS name, COUNT(*) AS value
				FROM tbl_curriculum_development c
				LEFT JOIN tbl_service_master sm ON sm.id = c.curriculum_type_id
				GROUP BY sm.service_name
				ORDER BY value DESC
				""");
	}

	public List<Map<String, Object>> curriculumByLevel() {
		return jdbc.queryForList("""
				SELECT COALESCE(l.name, 'Not specified') AS name, COUNT(*) AS value
				FROM tbl_curriculum_development c
				LEFT JOIN tbl_dropdown_child_master l ON l.id = c.certificate_level_id
				GROUP BY l.name
				ORDER BY value DESC
				""");
	}

	/** Latest curriculum applications. */
	public List<Map<String, Object>> recentCurricula() {
		return jdbc.queryForList("""
				SELECT c.application_no, c.curriculum_title, c.status_id,
				       sm.service_name AS curriculum_type, s.name AS status_name,
				       ir.proposed_institute_name AS institute_name, c.created_at
				FROM tbl_curriculum_development c
				LEFT JOIN tbl_service_master sm ON sm.id = c.curriculum_type_id
				LEFT JOIN tbl_dropdown_child_master s ON s.id = c.status_id
				LEFT JOIN tbl_institute_registration_dtls ir ON ir.institute_id = c.institute_id
				ORDER BY c.created_at DESC, c.id DESC
				LIMIT 10
				""");
	}

	// ---------------- CMFG monitoring (service 55) ----------------
	// The CMFG table and the workflow table are queried separately and matched in Java,
	// so no text join (collation mix) between them is needed.

	private static final int CMFG_SERVICE_ID = 55;

	/** CMFG monitoring applications by workflow status. */
	public List<Map<String, Object>> cmfgByStatus() {
		return jdbc.queryForList("""
				SELECT COALESCE(st.name, 'Pending') AS name, COUNT(DISTINCT w.application_no) AS value
				FROM tbl_workflow_dtls w
				LEFT JOIN tbl_dropdown_child_master st ON st.id = w.status_id
				WHERE w.service_id = ?
				GROUP BY COALESCE(st.name, 'Pending')
				ORDER BY value DESC
				""", CMFG_SERVICE_ID);
	}

	/** Current workflow status of each CMFG monitoring application. */
	public List<Map<String, Object>> cmfgStatuses() {
		return jdbc.queryForList("""
				SELECT w.application_no, w.status_id, st.name AS status_name
				FROM tbl_workflow_dtls w
				LEFT JOIN tbl_dropdown_child_master st ON st.id = w.status_id
				WHERE w.service_id = ?
				""", CMFG_SERVICE_ID);
	}

	/** One row per monitoring application: institute and Yes / No answers (newest first). */
	public List<Map<String, Object>> cmfgApplications() {
		return jdbc.queryForList("""
				SELECT m.application_no, MIN(m.created_at) AS created_at,
				       COUNT(*) AS total_items,
				       SUM(CASE WHEN m.response_id = 'Y' THEN 1 ELSE 0 END) AS yes_count,
				       SUM(CASE WHEN m.response_id = 'N' THEN 1 ELSE 0 END) AS no_count,
				       ir.proposed_institute_name AS institute_name
				FROM tbl_cmfg_monitoring_dtl m
				LEFT JOIN tbl_institute_registration_dtls ir
				       ON ir.institute_id = CAST(m.institute_id AS UNSIGNED)
				GROUP BY m.application_no, ir.proposed_institute_name
				ORDER BY created_at DESC
				LIMIT 200
				""");
	}

	// ---------------- Tracer surveys (DWPS 1 dashboard) ----------------
	// Question sets and sends are queried separately and matched in Java (no text join).

	/** One row per tracer survey (question set): title, tracer type and number of questions. */
	public List<Map<String, Object>> tracerSurveys() {
		return jdbc.queryForList("""
				SELECT q.application_no, MAX(q.tracer_title) AS tracer_title,
				       MAX(q.parent_tracer_type_id) AS parent_tracer_type_id,
				       MAX(pt.name) AS tracer_type,
				       MAX(st.name) AS sub_tracer_type,
				       COUNT(*) AS questions,
				       MIN(q.created_at) AS created_at
				FROM tbl_tracer_question_dtls q
				LEFT JOIN tbl_dropdown_child_master pt ON pt.id = q.parent_tracer_type_id
				LEFT JOIN tbl_dropdown_child_master st ON st.id = q.sub_tracer_type_id
				GROUP BY q.application_no
				ORDER BY created_at DESC
				""");
	}

	/** Sends per survey (question set): sent and responded (status 55 = survey submitted). */
	public List<Map<String, Object>> tracerSendsBySurvey() {
		return jdbc.queryForList("""
				SELECT s.question_application_no AS application_no,
				       COUNT(*) AS sent,
				       SUM(CASE WHEN s.status_id = 55 THEN 1 ELSE 0 END) AS responded,
				       MAX(s.created_at) AS last_sent
				FROM tbl_tracer_survey_send_dtls s
				GROUP BY s.question_application_no
				""");
	}

	/** Sends and responses per month within [from, to]. */
	public List<Map<String, Object>> tracerSendsByMonth(LocalDate from, LocalDate to) {
		return jdbc.queryForList("""
				SELECT DATE_FORMAT(s.created_at, '%Y-%m') AS ym,
				       COUNT(*) AS value,
				       SUM(CASE WHEN s.status_id = 55 THEN 1 ELSE 0 END) AS responded
				FROM tbl_tracer_survey_send_dtls s
				WHERE s.created_at >= ? AND s.created_at < ?
				GROUP BY DATE_FORMAT(s.created_at, '%Y-%m')
				ORDER BY ym
				""", java.sql.Date.valueOf(from), java.sql.Date.valueOf(to.plusDays(1)));
	}

	/** Sends by tracer type (parent tracer type on the send record). */
	public List<Map<String, Object>> tracerSendsByType() {
		return jdbc.queryForList("""
				SELECT COALESCE(pt.name, 'Not specified') AS name,
				       COUNT(*) AS value,
				       SUM(CASE WHEN s.status_id = 55 THEN 1 ELSE 0 END) AS responded
				FROM tbl_tracer_survey_send_dtls s
				LEFT JOIN tbl_dropdown_child_master pt ON pt.id = s.parent_tracer_type_id
				GROUP BY COALESCE(pt.name, 'Not specified')
				ORDER BY value DESC
				""");
	}
}
