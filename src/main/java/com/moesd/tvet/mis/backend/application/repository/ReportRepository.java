package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

	private final JdbcTemplate jdbcTemplate;

	public List<Map<String, Object>> getcourseData() {
		String sql =  "SELECT "
				+ "  a.*, "
				+ "  sm.service_name, "
				// readable names for the id columns (used by the Excel export)
				+ "  ir.proposed_institute_name AS institute_name, "
				+ "  lvl.name AS certification_level_name, "
				+ "  fs.name AS funding_source_name, "
				+ "  dz.dzongkhag_name AS training_location_name, "
				+ "  st.name AS application_status_name, "
				+ "  CASE "
				+ "    WHEN a.service_id = 38 "
				+ "    THEN nc.programme_title "
				+ "    WHEN a.service_id IN (37, 39, 41, 42) "
				+ "    THEN COALESCE(ncs.programme_title, d.programme_title) "
				+ "  END AS course_name "
				+ "FROM "
				+ "  tbl_programme_announcement_dtls a "
				+ "  LEFT JOIN tbl_service_master sm "
				+ "    ON sm.id = a.service_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls ir "
				+ "    ON ir.institute_id = a.institute_id "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl "
				+ "    ON lvl.id = a.certification_level_id "
				+ "  LEFT JOIN tbl_dropdown_child_master fs "
				+ "    ON fs.id = a.funding_source_id "
				+ "  LEFT JOIN tbl_dzongkhag_master dz "
				+ "    ON dz.id = a.training_location_id "
				+ "  LEFT JOIN tbl_dropdown_child_master st "
				+ "    ON st.id = a.application_status_id "
				+ "  LEFT JOIN tbl_accredited_course_dtls b "
				+ "    ON a.programme_id = b.id "
				+ "  LEFT JOIN tbl_curriculum_development c "
				+ "    ON c.id = b.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls ncs "
				+ "    ON ncs.id = COALESCE(c.programme_id, b.ncs_id) "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON a.programme_id = nc.id "
				+ "  LEFT JOIN tbl_ncs_app_dtls d "
				+ "    ON d.id = a.programme_id "
				+ "WHERE a.service_id IN (37, 38, 39, 41, 42)";
		return jdbcTemplate.queryForList(sql);
	}

	// Programme types for the course report (tbl_service_master):
	// 37 BQF, 38 Non-BQF, 39 RPL, 41 RPL re-assessment, 42 BQF re-assessment
	public List<Map<String, Object>> courseServiceType() {
		String sql ="SELECT "
				+ "  a.* "
				+ "FROM "
				+ "  tbl_service_master a "
				+ "WHERE a.id IN (37, 38, 39, 41, 42)";
		return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getAllInstitutes() {
		String sql ="SELECT a.* FROM tbl_institute_registration_dtls a";
		return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getInstitutesProposalType() {
		String sql = "SELECT "
				+ "  a.* "
				+ "FROM "
				+ "  tbl_service_master a "
				+ "WHERE a.id IN (6, 34, 35)";
		return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getInstituteRegistrationType() {
		String sql = "SELECT "
				+ "  a.* "
				+ "FROM "
				+ "  tbl_service_master a "
				// institute / SES centre / assessment centre registrations only
				// (Assessor 32 and Accreditor 5 have their own report)
				+ "WHERE a.id IN (7, 36, 4)";
		return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getInstitutesProposalDetails() {
		String sql = "SELECT a.* FROM tbl_institute_proposal a";
		return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getInstitutesRegistrationDetails() {
		String sql = "SELECT a.* FROM tbl_institute_registration_dtls a";
		return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getInstitutesTraineesDetails() {
		// 37 and RPL 39 both announce an approved accredited course: its NCS programme comes
		// from the curriculum, or from ncs_id when the course has no curriculum.
		// Older 39 rows stored the NCS id directly (d), so that is the fallback.
		String sql = "SELECT "
				+ "  a.*, "
				+ "  b.institute_id, "
				+ "  b.course_start_date, "
				+ "  b.course_end_date, "
				+ "  b.certification_level_id, "
				+ "  CASE "
				+ "    WHEN b.service_id = 37 "
				+ "    THEN c.programme_title "
				+ "    WHEN b.service_id = 38 "
				+ "    THEN nc.programme_title "
				+ "    WHEN b.service_id = 39 "
				+ "    THEN COALESCE(c.programme_title, d.programme_title) "
				+ "  END AS course_name "
				+ "FROM "
				+ "  tbl_programme_trainee_enrollment_dtls a "
				+ "  LEFT JOIN tbl_programme_announcement_dtls b "
				+ "    ON a.course_enrol_app_no = b.application_no "
				+ "  LEFT JOIN tbl_accredited_course_dtls ac "
				+ "    ON b.programme_id = ac.id "
				+ "  LEFT JOIN tbl_curriculum_development cd "
				+ "    ON cd.id = ac.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls c "
				+ "    ON c.id = COALESCE(cd.programme_id, ac.ncs_id) "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON b.programme_id = nc.id "
				+ "  LEFT JOIN tbl_ncs_app_dtls d "
				+ "    ON d.id = b.programme_id "
				+ "WHERE b.service_id IN (37, 38, 39)";
		return jdbcTemplate.queryForList(sql);
	}
	

	public List<Map<String, Object>> getInstituteSesCentreAssessmentCentreRegistrationDetails(
	        String registrationType, String applicationNo) {

	    String sql = "SELECT "
	    		+ "  a.registration_no, "
	    		+ "  a.proposed_institute_name AS institute_name, "
	    		+ "  b.dzongkhag_name AS location, "
	    		+ "  a.exact_location, "
	    		+ "  a.service_id, "
	    		+ "  a.application_no, "
	    		+ "  c.service_name AS registration_type "
	    		+ "FROM "
	    		+ "  tbl_institute_registration_dtls a "
	    		+ "  LEFT JOIN tbl_dzongkhag_master b "
	    		+ "    ON a.dzongkhag_id = b.id "
	    		+ "  LEFT JOIN tbl_service_master c "
	    		+ "    ON c.id = a.service_id "
	    		+ "WHERE a.application_no = ? "
	    		+ "  AND a.service_id = ?";

	    return jdbcTemplate.queryForList(sql, applicationNo, registrationType);
	}
	
	public List<Map<String, Object>> getAssessorAccreditorQualityAuditorDetails(
	        String registrationType, String applicationNo) {
		
	    String sql =  "SELECT "
	    		+ "  a.registration_no, "
	    		+ "  COALESCE( "
	    		+ "    NULLIF(a.citizen_id, ''), "
	    		+ "    a.reference_no "
	    		+ "  ) AS cidNo, "
	    		+ "  a.full_name, "
	    		+ "  a.application_no, "
	    		+ "  b.service_name AS registration_type "
	    		+ "FROM "
	    		+ "  tbl_assessor_accreditor_auditor_registration a "
	    		+ "  LEFT JOIN tbl_service_master b "
	    		+ "    ON a.service_id = b.id "
	    		+ "WHERE a.application_no = ? "
	    		+ "  AND a.service_id = ?";
	    return jdbcTemplate.queryForList(sql, applicationNo, registrationType);
	}
	
	// Shared SELECT/JOINs for trainee assessment certificates (competent trainees)
	private static final String TRAINEE_CERTIFICATE_BASE_SQL = "SELECT "
	    		+ "  COALESCE( "
	    		+ "    NULLIF(a.cid_no, ''), "
	    		+ "    a.reference_no "
	    		+ "  ) AS cid_no, "
	    		+ "  a.applicant_name, "
	    		+ "  a.certificate_no, "
	    		+ "  DATE_FORMAT( "
	    		+ "    a.certificate_issued_date, "
	    		+ "    '%M, %Y' "
	    		+ "  ) AS certificate_issued, "
	    		+ "  cdm.name AS gender, "
	    		+ "  b.course_start_date, "
	    		+ "  b.course_end_date, "
	    		+ "  b.certification_level_id, "
	    		+ "  cdm1.name AS certification_level, "
	    		+ "  sm.service_name AS programme_type, "
	    		+ "  b.service_id, "
	    		+ "  CASE "
	    		+ "    WHEN b.service_id IN (37, 39, 41, 42) "
	    		+ "    THEN COALESCE( "
	    		+ "      c.programme_title, "
	    		+ "      d.programme_title "
	    		+ "    ) "
	    		+ "  END AS course_name, "
	    		+ "  (SELECT "
	    		+ "    IFNULL( "
	    		+ "      JSON_ARRAYAGG( "
	    		+ "        JSON_OBJECT( "
	    		+ "          'unitCode', "
	    		+ "          unit.unit_code, "
	    		+ "          'unitTitle', "
	    		+ "          unit.unit_title "
	    		+ "        ) "
	    		+ "      ), "
	    		+ "      JSON_ARRAY() "
	    		+ "    ) "
	    		+ "  FROM "
	    		+ "    tbl_ncs_units unit "
	    		+ "  WHERE unit.ncs_id = "
	    		+ "    CASE "
	    		+ "      WHEN b.service_id IN (37, 39, 41, 42) "
	    		+ "      THEN COALESCE (c.id, d.id) "
	    		+ "    END) AS unit_codes, "
	    		+ "  CASE "
	    		+ "    WHEN b.certification_level_id IN (108, 109, 110) "
	    		+ "    THEN cdm2.name "
	    		+ "    WHEN b.certification_level_id IN (111, 112) "
	    		+ "    THEN a.internal_assessment "
	    		+ "  END AS internal_assessment_mark, "
	    		+ "  CASE "
	    		+ "    WHEN b.certification_level_id IN (108, 109, 110) "
	    		+ "    THEN cdm3.name "
	    		+ "    WHEN b.certification_level_id IN (111, 112) "
	    		+ "    THEN a.practical_assessment "
	    		+ "  END AS practical_assessment_mark, "
	    		+ "  CASE "
	    		+ "    WHEN b.certification_level_id IN (108, 109, 110) "
	    		+ "    THEN cdm4.name "
	    		+ "    WHEN b.certification_level_id IN (111, 112) "
	    		+ "    THEN a.theory_assessment "
	    		+ "  END AS theory_assessment_mark, "
	    		+ "  CASE "
	    		+ "    WHEN b.certification_level_id IN (108, 109, 110) "
	    		+ "    THEN cdm5.name "
	    		+ "    WHEN b.certification_level_id IN (111, 112) "
	    		+ "    THEN a.viva_assessment "
	    		+ "  END AS viva_assessment_mark, "
	    		+ "  cdm6.name AS overall_result, "
	    		+ "  b.application_no, "
	    		+ "  b.institute_id, "
	    		+ "  ird.proposed_institute_name AS institute_name, "
	    		// Assessment venue parts (joined in the frontend as Institute-Dzongkhag-Location)
	    		+ "  dz.dzongkhag_name, "
	    		+ "  ird.exact_location "
	    		+ "FROM "
	    		+ "  tbl_programme_trainee_enrollment_dtls a "
	    		+ "  LEFT JOIN tbl_programme_announcement_dtls b "
	    		+ "    ON a.course_enrol_app_no = b.application_no "
	    		+ "  LEFT JOIN tbl_accredited_course_dtls ac "
	    		+ "    ON b.programme_id = ac.id "
	    		+ "  LEFT JOIN tbl_curriculum_development cd "
	    		+ "    ON cd.id = ac.curriculum_id "
	    		+ "  LEFT JOIN tbl_ncs_app_dtls c "
	    		+ "    ON c.id = COALESCE (cd.programme_id, ac.ncs_id) "
	    		+ "  LEFT JOIN tbl_ncs_app_dtls d "
	    		+ "    ON d.id = b.programme_id "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm "
	    		+ "    ON a.gender_id = cdm.id "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm1 "
	    		+ "    ON cdm1.id = b.certification_level_id "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm2 "
	    		+ "    ON cdm2.id = a.internal_assessment "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm3 "
	    		+ "    ON cdm3.id = a.practical_assessment "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm4 "
	    		+ "    ON cdm4.id = a.theory_assessment "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm5 "
	    		+ "    ON cdm5.id = a.viva_assessment "
	    		+ "  LEFT JOIN tbl_service_master sm "
	    		+ "    ON sm.id = b.service_id "
	    		+ "  LEFT JOIN tbl_dropdown_child_master cdm6 "
	    		+ "    ON cdm6.id = a.result_status_id "
	    		+ "  LEFT JOIN tbl_institute_registration_dtls ird "
	    		+ "    ON ird.institute_id = b.institute_id "
	    		+ "  LEFT JOIN tbl_dzongkhag_master dz "
	    		+ "    ON dz.id = ird.dzongkhag_id ";

	public List<Map<String, Object>> getTraineesDetailsForCertificate(
	        String registrationNo, String applicationNo) {
	    String sql = TRAINEE_CERTIFICATE_BASE_SQL
	    		+ "WHERE a.result_status_id = 94 "
	    		+ "  AND b.application_status_id = 59 "
	    		+ "  AND b.service_id IN (37, 39, 41, 42) "
	    		+ "  AND b.application_no = ? "
	    		+ "  AND ird.registration_no = ?";
	    return jdbcTemplate.queryForList(sql, applicationNo, registrationNo);
	}

	// Certificate printing: competent trainees of all institutes
	public List<Map<String, Object>> getAllTraineesDetailsForCertificate() {
	    String sql = TRAINEE_CERTIFICATE_BASE_SQL
	    		+ "WHERE a.result_status_id = 94 "
	    		+ "  AND b.application_status_id = 59 "
	    		+ "  AND b.service_id IN (37, 39, 41, 42) "
	    		+ "ORDER BY ird.proposed_institute_name, b.application_no, a.applicant_name";
	    return jdbcTemplate.queryForList(sql);
	}
	
	public List<Map<String, Object>> getAccreditedProgrammeDetailsForCertificate(
	        String registrationNo, String applicationNo) {
		
	    	String sql ="SELECT "
	    			+ "  ac.id, "
	    			+ "  ac.application_no, "
	    			+ "  ac.institute_id, "
	    			+ "  ac.enrolment_capacity, "
	    			+ "  ac.registration_date, "
	    			+ "  ac.validity_date, "
	    			+ "  ac.curriculum_id, "
	    			+ "  ac.ncs_id, "
	    			+ "  ir.proposed_institute_name AS institute_name, "
	    			+ "  ir.registration_no, "
	    			+ "  sm.sector_name, "
	    			+ "  om.occupation_name, "
	    			+ "  ncs.programme_title, "
	    			+ "  ncs.certification_id "
	    			+ "FROM "
	    			+ "  tbl_accredited_course_dtls ac "
	    			+ "  LEFT JOIN tbl_curriculum_development cd "
	    			+ "    ON ac.curriculum_id = cd.id "
	    			+ "  LEFT JOIN tbl_ncs_app_dtls ncs "
	    			+ "    ON ncs.id = COALESCE(cd.programme_id, ac.ncs_id) "
	    			+ "  LEFT JOIN tbl_occupation_master om "
	    			+ "    ON om.id = ncs.occupation_id "
	    			+ "  LEFT JOIN tbl_sector_master sm "
	    			+ "    ON sm.id = ncs.sector_id "
	    			+ "  LEFT JOIN tbl_institute_registration_dtls ir "
	    			+ "    ON ac.institute_id = ir.institute_id "
	    			+ "WHERE ac.status_id = 57 "
	    			+ "  AND ir.registration_no = ? "
	    			+ "  AND ac.application_no = ?";
	    	return jdbcTemplate.queryForList(sql, registrationNo, applicationNo);
	}
	
	
	

	// Assessment report for one institute (logged-in registration no):
	// selected trainees (status 90) of its BQF (37), RPL (39) and re-assessment (41, 42)
	// announcements with their marks / competencies and overall result.
	// The NCS programme comes through the announced accredited course (curriculum, or ncs_id
	// when it has none); older rows that stored the NCS id directly fall back to d.
	public List<Map<String, Object>> getInstituteAssessmentReport(String registrationNo) {
		String sql = "SELECT "
				+ "  a.application_no AS announcement_no, "
				+ "  a.service_id, "
				+ "  sm.service_name, "
				+ "  COALESCE(c.programme_title, d.programme_title) AS course_name, "
				+ "  lvl.name AS certification_level, "
				+ "  a.certification_level_id, "
				+ "  a.course_start_date, "
				+ "  a.course_end_date, "
				+ "  a.ca_start_date, "
				+ "  a.ca_end_date, "
				+ "  tp.application_no AS trainee_application_no, "
				+ "  tp.applicant_name, "
				+ "  COALESCE(NULLIF(tp.cid_no, ''), tp.reference_no) AS cid_no, "
				+ "  g.name AS gender, "
				+ "  tp.mobile_no, "
				+ "  tp.email_id, "
				// certificate levels store competency ids; diploma levels store marks
				+ "  CASE WHEN a.certification_level_id IN (108, 109, 110) "
				+ "       THEN ia.name ELSE tp.internal_assessment END AS internal_assessment, "
				+ "  CASE WHEN a.certification_level_id IN (108, 109, 110) "
				+ "       THEN ta.name ELSE tp.theory_assessment END AS theory_assessment, "
				+ "  CASE WHEN a.certification_level_id IN (108, 109, 110) "
				+ "       THEN va.name ELSE tp.viva_assessment END AS viva_assessment, "
				+ "  CASE WHEN a.certification_level_id IN (108, 109, 110) "
				+ "       THEN pa.name ELSE tp.practical_assessment END AS practical_assessment, "
				+ "  rs.name AS overall_result, "
				+ "  tp.certificate_no, "
				+ "  tp.certificate_issued_date, "
				+ "  ir.proposed_institute_name AS institute_name "
				+ "FROM "
				+ "  tbl_programme_announcement_dtls a "
				+ "  JOIN tbl_institute_registration_dtls ir "
				+ "    ON ir.institute_id = a.institute_id "
				+ "  JOIN tbl_programme_trainee_enrollment_dtls tp "
				+ "    ON tp.course_enrol_app_no = a.application_no "
				+ "  LEFT JOIN tbl_service_master sm "
				+ "    ON sm.id = a.service_id "
				+ "  LEFT JOIN tbl_accredited_course_dtls ac "
				+ "    ON ac.id = a.programme_id "
				+ "  LEFT JOIN tbl_curriculum_development cd "
				+ "    ON cd.id = ac.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls c "
				+ "    ON c.id = COALESCE(cd.programme_id, ac.ncs_id) "
				+ "  LEFT JOIN tbl_ncs_app_dtls d "
				+ "    ON d.id = a.programme_id "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl "
				+ "    ON lvl.id = a.certification_level_id "
				+ "  LEFT JOIN tbl_dropdown_child_master g "
				+ "    ON g.id = tp.gender_id "
				+ "  LEFT JOIN tbl_dropdown_child_master ia "
				+ "    ON a.certification_level_id IN (108, 109, 110) AND ia.id = tp.internal_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master ta "
				+ "    ON a.certification_level_id IN (108, 109, 110) AND ta.id = tp.theory_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master va "
				+ "    ON a.certification_level_id IN (108, 109, 110) AND va.id = tp.viva_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master pa "
				+ "    ON a.certification_level_id IN (108, 109, 110) AND pa.id = tp.practical_assessment "
				+ "  LEFT JOIN tbl_dropdown_child_master rs "
				+ "    ON rs.id = tp.result_status_id "
				+ "WHERE ir.registration_no = ? "
				+ "  AND a.service_id IN (37, 39, 41, 42) "
				+ "  AND tp.status_id = 90 "
				+ "ORDER BY a.course_start_date DESC, a.application_no, tp.applicant_name";
		return jdbcTemplate.queryForList(sql, registrationNo);
	}
	// Assessor / Accreditor / QMS Auditor registrations report (all statuses).
	// Same table as AssessorAccreditorQMSAuditorController: 32 Assessor, 5 Accreditor, 3 QMS Auditor.
	public List<Map<String, Object>> getAssessorAccreditorAuditorReport() {
		String sql = "SELECT "
				+ "  a.id, "
				+ "  a.application_no, "
				+ "  a.registration_no, "
				+ "  a.service_id, "
				+ "  sm.service_name, "
				+ "  COALESCE(NULLIF(a.citizen_id, ''), a.reference_no) AS cid_no, "
				+ "  a.full_name, "
				+ "  g.name AS gender, "
				+ "  a.mobile_no, "
				+ "  a.email, "
				+ "  a.dzongkhag_id, "
				+ "  dz.dzongkhag_name, "
				+ "  a.organization_name, "
				+ "  a.designation, "
				+ "  a.years_of_experience, "
				+ "  a.sector_id, "
				// names are looked up by id; the stored name columns are often empty
				+ "  COALESCE(sec.sector_name, a.sector_name) AS sector_name, "
				+ "  COALESCE(occ.occupation_name, a.occupation_name) AS occupation_name, "
				+ "  COALESCE(lvl.name, a.certification_level_name) AS certification_level_name, "
				+ "  a.qms_training, "
				+ "  a.status_id, "
				+ "  st.name AS status_name, "
				+ "  a.created_at "
				+ "FROM "
				+ "  tbl_assessor_accreditor_auditor_registration a "
				+ "  LEFT JOIN tbl_service_master sm ON sm.id = a.service_id "
				+ "  LEFT JOIN tbl_dropdown_child_master g ON g.id = a.gender_id "
				+ "  LEFT JOIN tbl_dzongkhag_master dz ON dz.id = a.dzongkhag_id "
				+ "  LEFT JOIN tbl_dropdown_child_master st ON st.id = a.status_id "
				+ "  LEFT JOIN tbl_sector_master sec ON sec.id = a.sector_id "
				+ "  LEFT JOIN tbl_occupation_master occ ON occ.id = a.occupation_id "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl ON lvl.id = a.certification_level_id "
				+ "WHERE a.service_id IN (3, 5, 32) "
				+ "ORDER BY a.created_at DESC, a.id DESC";
		return jdbcTemplate.queryForList(sql);
	}
	// BQF / RPL programmes (AccreditedCourse, tbl_accredited_course_dtls).
	// Programme type name comes from tbl_service_master (service_id).
	public List<Map<String, Object>> getAccreditedProgrammesReport() {
		String sql = "SELECT "
				+ "  ac.id, "
				+ "  ac.application_no, "
				+ "  ac.service_id, "
				+ "  sm.service_name AS programme_category, "
				+ "  ncs.programme_title, "
				+ "  ncs.certification_id AS certification_level_id, "
				+ "  lvl.name AS certification_level, "
				+ "  sec.sector_name, "
				+ "  occ.occupation_name, "
				+ "  ac.fees_per_trainee, "
				+ "  ac.enrolment_capacity, "
				+ "  ac.registration_date, "
				+ "  ac.validity_date, "
				+ "  ac.status_id, "
				+ "  st.name AS status_name, "
				+ "  ac.created_at, "
				+ "  ir.proposed_institute_name AS institute_name, "
				+ "  dz.dzongkhag_name "
				+ "FROM "
				+ "  tbl_accredited_course_dtls ac "
				+ "  LEFT JOIN tbl_curriculum_development cd ON cd.id = ac.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls ncs ON ncs.id = COALESCE(cd.programme_id, ac.ncs_id) "
				+ "  LEFT JOIN tbl_dropdown_child_master lvl ON lvl.id = ncs.certification_id "
				+ "  LEFT JOIN tbl_sector_master sec ON sec.id = ncs.sector_id "
				+ "  LEFT JOIN tbl_occupation_master occ ON occ.id = ncs.occupation_id "
				+ "  LEFT JOIN tbl_dropdown_child_master st ON st.id = ac.status_id "
				+ "  LEFT JOIN tbl_service_master sm ON sm.id = ac.service_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls ir ON ir.institute_id = ac.institute_id "
				+ "  LEFT JOIN tbl_dzongkhag_master dz ON dz.id = ir.dzongkhag_id "
				+ "ORDER BY ac.created_at DESC, ac.id DESC";
		return jdbcTemplate.queryForList(sql);
	}

	// Non-BQF programmes (NonAccreditedCourse, tbl_non_accredited_course_dtls)
	public List<Map<String, Object>> getNonAccreditedProgrammesReport() {
		String sql = "SELECT "
				+ "  nc.id, "
				+ "  nc.application_no, "
				+ "  nc.service_id, "
				+ "  sm.service_name AS programme_category, "
				+ "  COALESCE(NULLIF(nc.programme_title, ''), nc.course_title) AS programme_title, "
				+ "  nc.programme_type_id AS certification_level_id, "
				+ "  pt.name AS certification_level, "
				+ "  NULL AS sector_name, "
				+ "  NULL AS occupation_name, "
				+ "  nc.fees_per_trainee, "
				+ "  nc.enrolment_capacity, "
				+ "  nc.registration_date, "
				+ "  nc.validity_date, "
				+ "  nc.status_id, "
				+ "  st.name AS status_name, "
				+ "  nc.created_at, "
				+ "  ir.proposed_institute_name AS institute_name, "
				+ "  dz.dzongkhag_name "
				+ "FROM "
				+ "  tbl_non_accredited_course_dtls nc "
				+ "  LEFT JOIN tbl_dropdown_child_master pt ON pt.id = nc.programme_type_id "
				+ "  LEFT JOIN tbl_dropdown_child_master st ON st.id = nc.status_id "
				+ "  LEFT JOIN tbl_service_master sm ON sm.id = nc.service_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls ir ON ir.institute_id = nc.institute_id "
				+ "  LEFT JOIN tbl_dzongkhag_master dz ON dz.id = ir.dzongkhag_id "
				+ "ORDER BY nc.created_at DESC, nc.id DESC";
		return jdbcTemplate.queryForList(sql);
	}
}
