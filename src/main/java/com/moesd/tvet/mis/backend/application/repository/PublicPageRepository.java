package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PublicPageRepository {
	
	private final JdbcTemplate jdbcTemplate;

	public List<Map<String, Object>> trackApplicationStatus(String applicationNo) {
		String sql =  "SELECT "
				+ "  a.application_no, "
				+ "  a.application_name, "
				+ "  b.name AS current_status, "
				+ "  e.service_name, "
				+ "  d.role_name AS application_at "
				+ "FROM "
				+ "  tbl_workflow_dtls a "
				+ "  LEFT JOIN tbl_dropdown_child_master b "
				+ "    ON b.id = a.status_id "
				+ "  LEFT JOIN tbl_task_dtls c "
				+ "    ON c.application_no = a.application_no "
				+ "  LEFT JOIN tbl_role d "
				+ "    ON d.id = c.assigned_role_id "
				+ "    LEFT JOIN tbl_service_master e "
				+ "    ON e.id = a.service_id "
				+ "WHERE a.application_no = ?";
		return jdbcTemplate.queryForList(sql, applicationNo);
	}
	
	public List<Map<String, Object>> getOngoingCourses() {
		String sql =  "SELECT "
				+ "  a.*, "
				+ "  e.name AS certificate_level, "
				+ "  f.proposed_institute_name AS institute_name, "
				+ "  g.dzongkhag_name AS training_location, "
				+ "  CASE "
				+ "    WHEN a.service_id IN (37, 39) "
				+ "    THEN c.programme_title "
				+ "    WHEN a.service_id = 38 "
				+ "    THEN nc.programme_title "
				+ "  END AS course_name "
				+ "FROM "
				+ "  tbl_programme_announcement_dtls a "
				+ "  LEFT JOIN tbl_accredited_course_dtls b "
				+ "    ON a.programme_id = b.id "
				+ "  LEFT JOIN tbl_curriculum_development cd "
				+ "    ON b.curriculum_id IS NOT NULL "
				+ "   AND b.curriculum_id = cd.id "
				+ "  LEFT JOIN tbl_ncs_app_dtls c "
				+ "    ON (b.curriculum_id IS NOT NULL AND c.id = cd.programme_id) "
				+ "    OR (b.curriculum_id IS NULL     AND c.id = b.ncs_id) "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON a.programme_id = nc.id "
				+ "  LEFT JOIN tbl_dropdown_child_master e "
				+ "    ON e.id = a.certification_level_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls f "
				+ "    ON f.institute_id = a.institute_id "
				+ "  LEFT JOIN tbl_dzongkhag_master g "
				+ "    ON g.id = a.training_location_id "
				+ "WHERE a.service_id IN (37, 38, 39) "
				+ "  AND a.course_end_date >= CURRENT_DATE "
				+ "  AND a.application_end_date < CURRENT_DATE";
		return jdbcTemplate.queryForList(sql);	
	}
	
	public List<Map<String, Object>> getAllInstitutes() {
		String sql =   "SELECT a.* "
				+ " FROM tbl_institute_registration_dtls a";
		return jdbcTemplate.queryForList(sql);
		
	}
	
	public List<Map<String, Object>> getCourseBySector() {
		String sql =   "SELECT "
				+ "  d.sector_name, "
				+ "  COUNT(a.application_no) AS sector_value "
				+ "FROM "
				+ "  tbl_accredited_course_dtls a "
				+ "  LEFT JOIN tbl_curriculum_development b "
				+ "    ON a.curriculum_id IS NOT NULL "
				+ "   AND a.curriculum_id = b.id "
				+ "  LEFT JOIN tbl_ncs_app_dtls c "
				+ "    ON (a.curriculum_id IS NOT NULL AND c.id = b.programme_id) "
				+ "    OR (a.curriculum_id IS NULL     AND c.id = a.ncs_id) "
				+ "  LEFT JOIN tbl_sector_master d "
				+ "    ON d.id = c.sector_id "
				+ "WHERE a.status_id = 57 "
				+ "GROUP BY c.sector_id";
		return jdbcTemplate.queryForList(sql);
		
	}
	
	
	public List<Map<String, Object>> getCourseAnnounceNotifications() {
		String sql = "SELECT "
				+ "  CASE "
				+ "    WHEN a.service_id IN (37, 39) "
				+ "    THEN c.programme_title "
				+ "    WHEN a.service_id = 38 "
				+ "    THEN nc.programme_title "
				+ "  END AS course_name, "
				+ "  a.course_start_date, "
				+ "  a.course_end_date "
				+ "FROM "
				+ "  tbl_programme_announcement_dtls a "
				+ "  LEFT JOIN tbl_accredited_course_dtls b "
				+ "    ON a.programme_id = b.id "
				+ "  LEFT JOIN tbl_curriculum_development cd "
				+ "    ON b.curriculum_id IS NOT NULL "
				+ "   AND cd.id = b.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls c "
				+ "    ON (b.curriculum_id IS NOT NULL AND c.id = cd.programme_id) "
				+ "    OR (b.curriculum_id IS NULL     AND c.id = b.ncs_id) "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON a.programme_id = nc.id "
				+ "WHERE a.service_id IN (37, 38, 39) "
				+ "  AND a.application_end_date >= CURRENT_DATE";
		return jdbcTemplate.queryForList(sql);
		
	}

	// Headline figures for the public home page ("TVET by the numbers").
	// Each figure is read on its own so one failing query does not hide the others.
	public java.util.Map<String, Object> getHomeStats() {
		java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
		// all approved (status 57) registered providers: institutes, SES centres and assessment centres
		stats.put("trainingInstitutes", count(
				"SELECT COUNT(*) FROM tbl_institute_registration_dtls WHERE status_id = 57"));
		// approved accredited + non-accredited programmes
		stats.put("programsOffered", count(
				"SELECT (SELECT COUNT(*) FROM tbl_accredited_course_dtls WHERE status_id = 57) "
						+ "+ (SELECT COUNT(*) FROM tbl_non_accredited_course_dtls WHERE status_id = 57)"));
		// registered trainers (tbl_trainer_dtls, AddTrainer): active ones only (deleted = status 0),
		// each person once even if listed by more than one institute
		stats.put("registeredTrainers", count(
				"SELECT COUNT(DISTINCT COALESCE(NULLIF(citizen_id, ''), NULLIF(work_permit_no, ''), CONCAT('id-', id))) "
						+ "FROM tbl_trainer_dtls WHERE COALESCE(status_id, 1) <> 0"));
		// trainee applications to announced programmes
		stats.put("traineesEnrolled", count("SELECT COUNT(*) FROM tbl_programme_trainee_enrollment_dtls"));
		// Dzongkhags with at least one approved registered provider
		stats.put("dzongkhagsReached", count(
				"SELECT COUNT(DISTINCT dzongkhag_id) FROM tbl_institute_registration_dtls "
						+ "WHERE status_id = 57 AND dzongkhag_id IS NOT NULL AND dzongkhag_id <> ''"));
		return stats;
	}

	private Long count(String sql) {
		try {
			Long n = jdbcTemplate.queryForObject(sql, Long.class);
			return n == null ? 0L : n;
		} catch (Exception e) {
			return null; // shown as "—" on the page
		}
	}
	// Public NCS publication list (same data as the NCS create page, with names joined)
	public List<Map<String, Object>> getNcsPublications() {
		String sql = "SELECT "
				+ "  a.id, "
				+ "  a.application_no, "
				+ "  a.sector_id, "
				+ "  sm.sector_name, "
				+ "  a.occupation_id, "
				+ "  om.occupation_name, "
				+ "  a.certification_id, "
				+ "  lvl.name AS certification_level, "
				+ "  a.programme_title, "
				+ "  a.validity_date, "
				+ "  (SELECT "
				+ "    IFNULL( "
				+ "      JSON_ARRAYAGG( "
				+ "        JSON_OBJECT('unitCode', u.unit_code, 'unitTitle', u.unit_title) "
				+ "      ), "
				+ "      JSON_ARRAY() "
				+ "    ) "
				+ "  FROM tbl_ncs_units u "
				+ "  WHERE u.ncs_id = a.id) AS units, "
				+ "  (SELECT "
				+ "    IFNULL( "
				+ "      JSON_ARRAYAGG( "
				+ "        JSON_OBJECT('id', d.id, 'documentName', d.document_name, 'url', d.upload_url) "
				+ "      ), "
				+ "      JSON_ARRAY() "
				+ "    ) "
				+ "  FROM tbl_document_master d "
				+ "  WHERE d.application_no = a.application_no) AS documents "
				+ "FROM "
				+ "  tbl_ncs_app_dtls a "
				+ "  LEFT JOIN tbl_sector_master sm "
				+ "    ON sm.id = a.sector_id "
				+ "  LEFT JOIN tbl_occupation_master om "
				+ "    ON om.id = a.occupation_id "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl "
				+ "    ON lvl.id = a.certification_id "
				+ "ORDER BY sm.sector_name, om.occupation_name, a.certification_id";
		return jdbcTemplate.queryForList(sql);
	}
	// Public curriculum publication list — Endorsed (status 59) curricula only
	public List<Map<String, Object>> getCurriculumPublications() {
		String sql = "SELECT "
				+ "  c.id, "
				+ "  c.application_no, "
				+ "  c.curriculum_title, "
				+ "  c.curriculum_type_id, "
				+ "  st.service_name AS curriculum_type, "
				+ "  c.certificate_level_id, "
				+ "  cl.name AS certificate_level_name, "
				+ "  COALESCE(ncs.programme_title, c.programme_title) AS programme_title, "
				+ "  c.description, "
				+ "  sm.sector_name, "
				+ "  om.occupation_name, "
				+ "  k.proposed_institute_name AS institute_name, "
				+ "  (SELECT "
				+ "    IFNULL( "
				+ "      JSON_ARRAYAGG( "
				+ "        JSON_OBJECT('id', d.id, 'documentName', d.document_name, 'url', d.upload_url) "
				+ "      ), "
				+ "      JSON_ARRAY() "
				+ "    ) "
				+ "  FROM tbl_document_master d "
				+ "  WHERE d.application_no = c.application_no) AS documents "
				+ "FROM "
				+ "  tbl_curriculum_development c "
				+ "  LEFT JOIN tbl_ncs_app_dtls ncs "
				+ "    ON ncs.id = c.programme_id "
				+ "  LEFT JOIN tbl_sector_master sm "
				+ "    ON sm.id = ncs.sector_id "
				+ "  LEFT JOIN tbl_occupation_master om "
				+ "    ON om.id = ncs.occupation_id "
				+ "  LEFT JOIN tbl_dropdown_child_master cl "
				+ "    ON cl.id = c.certificate_level_id "
				+ "  LEFT JOIN tbl_service_master st "
				+ "    ON st.id = c.curriculum_type_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls k "
				+ "    ON k.institute_id = c.institute_id "
				+ "WHERE c.status_id = 59 "
				+ "ORDER BY sm.sector_name, c.curriculum_title";
		return jdbcTemplate.queryForList(sql);
	}
	// Public assessment result lookup by CID / reference no (results of approved assessments only)
	public List<Map<String, Object>> getAssessmentResultsByCid(String cidNo) {
		String sql = "SELECT "
				+ "  a.id, "
				+ "  COALESCE(NULLIF(a.cid_no, ''), a.reference_no) AS cid_no, "
				+ "  a.applicant_name, "
				+ "  b.service_id, "
				+ "  sm.service_name AS assessment_type, "
				+ "  COALESCE(c.programme_title, d.programme_title) AS course_name, "
				+ "  b.certification_level_id, "
				+ "  lvl.name AS certification_level, "
				+ "  b.course_start_date, "
				+ "  b.course_end_date, "
				+ "  ird.proposed_institute_name AS institute_name, "
				// certificate levels store competency ids; diploma levels store marks
				+ "  CASE WHEN b.certification_level_id IN (108, 109, 110) "
				+ "       THEN ia.name ELSE a.internal_assessment END AS internal_assessment_mark, "
				+ "  CASE WHEN b.certification_level_id IN (108, 109, 110) "
				+ "       THEN ta.name ELSE a.theory_assessment END AS theory_assessment_mark, "
				+ "  CASE WHEN b.certification_level_id IN (108, 109, 110) "
				+ "       THEN va.name ELSE a.viva_assessment END AS viva_assessment_mark, "
				+ "  CASE WHEN b.certification_level_id IN (108, 109, 110) "
				+ "       THEN pa.name ELSE a.practical_assessment END AS practical_assessment_mark, "
				+ "  a.result_status_id, "
				+ "  rs.name AS overall_result, "
				+ "  a.certificate_no, "
				+ "  DATE_FORMAT(a.certificate_issued_date, '%M, %Y') AS certificate_issued, "
				// for the self-service certificate download (same data as the BQF certificate)
				+ "  dz.dzongkhag_name, "
				+ "  ird.exact_location, "
				+ "  (SELECT IFNULL(JSON_ARRAYAGG(JSON_OBJECT('unitCode', u.unit_code, 'unitTitle', u.unit_title)), JSON_ARRAY()) "
				+ "     FROM tbl_ncs_units u "
				+ "    WHERE u.ncs_id = COALESCE(c.id, d.id)) AS unit_codes "
				+ "FROM "
				+ "  tbl_programme_trainee_enrollment_dtls a "
				+ "  JOIN tbl_programme_announcement_dtls b "
				+ "    ON b.application_no = a.course_enrol_app_no "
				+ "  LEFT JOIN tbl_service_master sm "
				+ "    ON sm.id = b.service_id "
				+ "  LEFT JOIN tbl_accredited_course_dtls ac "
				+ "    ON ac.id = b.programme_id "
				+ "  LEFT JOIN tbl_curriculum_development cd "
				+ "    ON cd.id = ac.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls c "
				+ "    ON c.id = COALESCE(cd.programme_id, ac.ncs_id) "
				+ "  LEFT JOIN tbl_ncs_app_dtls d "
				+ "    ON d.id = b.programme_id "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl "
				+ "    ON lvl.id = b.certification_level_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls ird "
				+ "    ON ird.institute_id = b.institute_id "
				+ "  LEFT JOIN tbl_dzongkhag_master dz "
				+ "    ON dz.id = ird.dzongkhag_id "
				+ "  LEFT JOIN tbl_dropdown_child_master ia "
				+ "    ON b.certification_level_id IN (108, 109, 110) AND ia.id = a.internal_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master ta "
				+ "    ON b.certification_level_id IN (108, 109, 110) AND ta.id = a.theory_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master va "
				+ "    ON b.certification_level_id IN (108, 109, 110) AND va.id = a.viva_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master pa "
				+ "    ON b.certification_level_id IN (108, 109, 110) AND pa.id = a.practical_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master rs "
				+ "    ON rs.id = a.result_status_id "
				+ "WHERE b.service_id IN (37, 39, 41, 42) "
				+ "  AND b.application_status_id = 59 "
				+ "  AND a.result_status_id IS NOT NULL "
				+ "  AND COALESCE(NULLIF(a.cid_no, ''), a.reference_no) = ? "
				+ "ORDER BY b.course_end_date DESC, a.id DESC";
		return jdbcTemplate.queryForList(sql, cidNo);
	}
	// Public trainer report: active trainers (AddTrainer, status 1) with their courses and institute.
	// No CID / work permit / contact details are returned.
	public List<Map<String, Object>> getPublicTrainers() {
		String sql = "SELECT "
				+ "  t.id, "
				+ "  t.name, "
				+ "  g.name AS gender, "
				+ "  q.name AS qualification, "
				+ "  t.specialization, "
				+ "  et.name AS employment_type, "
				+ "  ir.proposed_institute_name AS institute_name, "
				+ "  (SELECT IFNULL(JSON_ARRAYAGG(JSON_OBJECT( "
				+ "      'courseType', ct.name, "
				+ "      'title', CASE WHEN LOWER(ct.name) LIKE '%non%' "
				+ "                    THEN nc.programme_title ELSE ncs.programme_title END, "
				+ "      'level', CASE WHEN LOWER(ct.name) LIKE '%non%' "
				+ "                    THEN ct.name ELSE lvl.name END "
				+ "    )), JSON_ARRAY()) "
				+ "   FROM tbl_trainer_course_dtls tc "
				+ "   LEFT JOIN tbl_dropdown_child_master ct ON ct.id = tc.course_type_id "
				+ "   LEFT JOIN tbl_accredited_course_dtls ac ON ac.id = tc.course_id "
				+ "   LEFT JOIN tbl_curriculum_development cd ON cd.id = ac.curriculum_id "
				+ "   LEFT JOIN tbl_ncs_app_dtls ncs ON ncs.id = COALESCE(cd.programme_id, ac.ncs_id) "
				+ "   LEFT JOIN tbl_dropdown_child_master lvl ON lvl.id = ncs.certification_id "
				+ "   LEFT JOIN tbl_non_accredited_course_dtls nc ON nc.id = tc.course_id "
				+ "   WHERE tc.trainer_id = t.id) AS courses "
				+ "FROM "
				+ "  tbl_trainer_dtls t "
				+ "  LEFT JOIN tbl_dropdown_child_master g ON g.id = t.gender_id "
				+ "  LEFT JOIN tbl_dropdown_child_master q ON q.id = t.qualification_id "
				+ "  LEFT JOIN tbl_dropdown_child_master et ON et.id = t.employment_type_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls ir ON ir.institute_id = t.institute_id "
				+ "WHERE t.status_id = 1 "
				+ "ORDER BY t.name";
		return jdbcTemplate.queryForList(sql);
	}
	// Public "Courses Accredited" report: approved accredited courses (status 57)
	// with the registered institute offering each one. Grouped by programme in the frontend.
	public List<Map<String, Object>> getAccreditedCoursesWithInstitutes() {
		String sql = "SELECT "
				+ "  ac.id AS accredited_course_id, "
				+ "  ncs.id AS programme_id, "
				+ "  ncs.programme_title, "
				+ "  ncs.certification_id AS certification_level_id, "
				+ "  lvl.name AS certification_level, "
				+ "  ac.validity_date, "
				+ "  ir.institute_id, "
				+ "  ir.proposed_institute_name AS institute_name, "
				+ "  dz.dzongkhag_name, "
				+ "  COALESCE(NULLIF(ir.mobile_no, ''), ir.telephone_no) AS contact_no, "
				+ "  ir.email_id "
				+ "FROM "
				+ "  tbl_accredited_course_dtls ac "
				+ "  LEFT JOIN tbl_curriculum_development cd ON cd.id = ac.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls ncs ON ncs.id = COALESCE(cd.programme_id, ac.ncs_id) "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl ON lvl.id = ncs.certification_id "
				+ "  JOIN tbl_institute_registration_dtls ir ON ir.institute_id = ac.institute_id "
				+ "  LEFT JOIN tbl_dzongkhag_master dz ON dz.id = ir.dzongkhag_id "
				+ "WHERE ac.status_id = 57 "
				+ "  AND ncs.id IS NOT NULL "
				+ "ORDER BY ncs.programme_title, ir.proposed_institute_name";
		return jdbcTemplate.queryForList(sql);
	}
}
