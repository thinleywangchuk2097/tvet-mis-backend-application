package com.moesd.tvet.mis.backend.application.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

/** Read-only aggregate queries for the institute dashboard. */
@Repository
@RequiredArgsConstructor
public class InstituteDashboardRepository {

	private final JdbcTemplate jdbc;

	/** Common FROM for trainees of this institute's programmes. */
	private static final String TRAINEE_FROM = """
			FROM tbl_programme_trainee_enrollment_dtls tr
			JOIN tbl_programme_announcement_dtls a ON a.application_no = tr.course_enrol_app_no
			WHERE a.institute_id = ?
			""";

	public Map<String, Object> findInstitute(String registrationNo) {
		List<Map<String, Object>> rows = jdbc.queryForList("""
				SELECT i.institute_id, i.proposed_institute_name AS institute_name, i.registration_no,
				       i.application_no, i.created_at, i.institute_renewal_date, i.email_id, i.mobile_no,
				       i.exact_location, i.bhutanese_employees, i.non_bhutanese_employees,
				       d.dzongkhag_name, s.name AS status_name
				FROM tbl_institute_registration_dtls i
				LEFT JOIN tbl_dzongkhag_master d ON d.id = i.dzongkhag_id
				LEFT JOIN tbl_dropdown_child_master s ON s.id = i.status_id
				WHERE i.registration_no = ?
				LIMIT 1
				""", registrationNo);
		return rows.isEmpty() ? null : rows.get(0);
	}

	public List<Map<String, Object>> trainersByEmployment(Long instituteId) {
		return jdbc.queryForList("""
				SELECT COALESCE(e.name, 'Not specified') AS name, COUNT(*) AS value
				FROM tbl_trainer_dtls t
				LEFT JOIN tbl_dropdown_child_master e ON e.id = t.employment_type_id
				WHERE t.institute_id = ? AND t.status_id = 1
				GROUP BY e.name
				ORDER BY value DESC
				""", instituteId);
	}

	public long staffCount(Long instituteId) {
		Long n = jdbc.queryForObject("""
				SELECT COUNT(*) FROM tbl_staff_management_dtls
				WHERE institute_id = ? AND (status_id IS NULL OR status_id <> 0)
				""", Long.class, instituteId);
		return n == null ? 0 : n;
	}

	public long accreditedCourseCount(Long instituteId) {
		Long n = jdbc.queryForObject(
				"SELECT COUNT(*) FROM tbl_accredited_course_dtls WHERE institute_id = ?", Long.class, instituteId);
		return n == null ? 0 : n;
	}

	public long nonAccreditedCourseCount(Long instituteId) {
		Long n = jdbc.queryForObject(
				"SELECT COUNT(*) FROM tbl_non_accredited_course_dtls WHERE institute_id = ?", Long.class,
				String.valueOf(instituteId));
		return n == null ? 0 : n;
	}

	public List<Map<String, Object>> programmes(Long instituteId) {
		return jdbc.queryForList("""
				SELECT a.application_no, a.service_id, a.course_start_date, a.course_end_date,
				       a.application_start_date, a.application_end_date, a.enrollment_capacity, a.created_at,
				       dcd.name AS certification_name,
				       COALESCE(
				         CASE WHEN a.service_id = 37 THEN c.programme_title
				              WHEN a.service_id = 38 THEN nc.programme_title
				              WHEN a.service_id = 39 THEN d.programme_title END,
				         sm.service_name) AS course_name,
				       COALESCE(t.total, 0) AS enrolled
				FROM tbl_programme_announcement_dtls a
				LEFT JOIN tbl_accredited_course_dtls ac ON ac.id = a.programme_id
				LEFT JOIN tbl_curriculum_development cd ON ac.curriculum_id = cd.id
				LEFT JOIN tbl_ncs_app_dtls c ON c.id = cd.programme_id
				LEFT JOIN tbl_non_accredited_course_dtls nc ON a.programme_id = nc.id
				LEFT JOIN tbl_ncs_app_dtls d ON d.id = a.programme_id
				LEFT JOIN tbl_dropdown_child_master dcd ON dcd.id = a.certification_level_id
				LEFT JOIN tbl_service_master sm ON sm.id = a.service_id
				LEFT JOIN (SELECT course_enrol_app_no, COUNT(*) AS total
				           FROM tbl_programme_trainee_enrollment_dtls GROUP BY course_enrol_app_no) t
				       ON t.course_enrol_app_no = a.application_no
				WHERE a.institute_id = ?
				ORDER BY a.course_start_date DESC
				LIMIT 100
				""", String.valueOf(instituteId));
	}

	public Map<String, Object> traineeTotals(Long instituteId) {
		return jdbc.queryForMap("""
				SELECT COUNT(*) AS total,
				       COALESCE(SUM(CASE WHEN tr.certificate_no IS NOT NULL AND tr.certificate_no <> ''
				                    THEN 1 ELSE 0 END), 0) AS graduates
				""" + TRAINEE_FROM, String.valueOf(instituteId));
	}

	public List<Map<String, Object>> traineesByGender(Long instituteId) {
		return jdbc.queryForList("""
				SELECT COALESCE(g.name, 'Not specified') AS name, COUNT(*) AS value
				FROM tbl_programme_trainee_enrollment_dtls tr
				JOIN tbl_programme_announcement_dtls a ON a.application_no = tr.course_enrol_app_no
				LEFT JOIN tbl_dropdown_child_master g ON g.id = tr.gender_id
				WHERE a.institute_id = ?
				GROUP BY g.name
				ORDER BY value DESC
				""", String.valueOf(instituteId));
	}

	/** Enrolments per month (yyyy-MM) for the last 12 months. */
	public List<Map<String, Object>> monthlyEnrollments(Long instituteId) {
		return jdbc.queryForList("""
				SELECT DATE_FORMAT(tr.created_at, '%Y-%m') AS ym, COUNT(*) AS value
				""" + TRAINEE_FROM + """
				  AND tr.created_at >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 11 MONTH), '%Y-%m-01')
				GROUP BY ym
				""", String.valueOf(instituteId));
	}

	/** Programmes starting per month (yyyy-MM) for the last 12 months. */
	public List<Map<String, Object>> programmesPerMonth(Long instituteId) {
		return jdbc.queryForList("""
				SELECT DATE_FORMAT(a.course_start_date, '%Y-%m') AS ym, COUNT(*) AS value
				FROM tbl_programme_announcement_dtls a
				WHERE a.institute_id = ?
				  AND a.course_start_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 11 MONTH), '%Y-%m-01')
				GROUP BY ym
				""", String.valueOf(instituteId));
	}

	public Map<String, Object> totSummary(Long instituteId) {
		return jdbc.queryForMap("""
				SELECT COUNT(DISTINCT application_no) AS programmes,
				       COUNT(*) AS trainers_applied,
				       COALESCE(SUM(CASE WHEN is_selected = 1 THEN 1 ELSE 0 END), 0) AS trainers_selected,
				       COALESCE(SUM(CASE WHEN certificate_no IS NOT NULL THEN 1 ELSE 0 END), 0) AS certified
				FROM tbl_tot_program_trainer_applied_dtls
				WHERE institute_id = ?
				""", instituteId);
	}

	/** Application numbers belonging to this institute, one query per source table. */
	public List<String> applicationNos(Long instituteId) {
		String id = String.valueOf(instituteId);
		String[] sources = {
				"SELECT application_no FROM tbl_institute_registration_dtls WHERE institute_id = ?",
				"SELECT application_no FROM tbl_institute_change_dtls WHERE institute_id = ?",
				"SELECT application_no FROM tbl_accredited_course_dtls WHERE institute_id = ?",
				"SELECT application_no FROM tbl_non_accredited_course_dtls WHERE institute_id = ?",
				"SELECT application_no FROM tbl_programme_announcement_dtls WHERE institute_id = ?",
				"SELECT DISTINCT application_no FROM tbl_tot_program_trainer_applied_dtls WHERE institute_id = ?" };
		List<String> out = new ArrayList<>();
		for (String sql : sources) {
			try {
				out.addAll(jdbc.queryForList(sql, String.class, id));
			} catch (Exception ignored) {
				// table/column not present in this environment - skip this source
			}
		}
		return out;
	}

	public List<Map<String, Object>> workflows(List<String> applicationNos) {
		if (applicationNos.isEmpty()) return List.of();
		String in = String.join(",", java.util.Collections.nCopies(applicationNos.size(), "?"));
		return jdbc.queryForList("""
				SELECT w.application_no, w.action_date, w.wf_remarks, s.service_name, st.name AS status_name
				FROM tbl_workflow_dtls w
				LEFT JOIN tbl_service_master s ON s.id = w.service_id
				LEFT JOIN tbl_dropdown_child_master st ON st.id = w.status_id
				WHERE w.application_no IN (""" + in + """
				)
				ORDER BY w.action_date DESC
				LIMIT 200
				""", applicationNos.toArray());
	}
}
