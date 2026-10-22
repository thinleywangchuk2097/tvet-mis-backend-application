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
				+ "  CASE "
				+ "    WHEN a.service_id = 37 "
				+ "    THEN ncs.programme_title "
				+ "    WHEN a.service_id = 38 "
				+ "    THEN nc.programme_title "
				+ "    WHEN a.service_id = 39 "
				+ "    THEN d.programme_title "
				+ "  END AS course_name "
				+ "FROM "
				+ "  tbl_programme_announcement_dtls a "
				+ "  LEFT JOIN tbl_accredited_course_dtls b "
				+ "    ON a.programme_id = b.id "
				+ "  LEFT JOIN tbl_curriculum_development c "
				+ "    ON c.id = b.curriculum_id "
				+ "  LEFT JOIN tbl_ncs_app_dtls ncs "
				+ "    ON ncs.id = c.programme_id "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON a.programme_id = nc.id "
				+ "  LEFT JOIN tbl_ncs_app_dtls d "
				+ "    ON d.id = a.programme_id "
				+ "WHERE a.service_id IN(37, 38, 39)";
		return jdbcTemplate.queryForList(sql);
	}

	public List<Map<String, Object>> courseServiceType() {
		String sql ="SELECT "
				+ "  a.* "
				+ "FROM "
				+ "  tbl_service_master a "
				+ "WHERE a.id IN(37, 38, 39)";
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
				+ "WHERE a.id IN (7, 36, 4, 32, 5)";
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
				+ "    THEN d.programme_title "
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
				+ "    ON c.id = cd.programme_id "
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
	
	public List<Map<String, Object>> getTraineesDetailsForCertificate(
	        String registrationNo, String applicationNo) {
		
	    String sql = "SELECT "
	    		+ "  COALESCE ( "
	    		+ "    NULLIF (a.cid_no, ''), "
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
	    		+ "    WHEN b.service_id IN (37, 42) "
	    		+ "    THEN c.programme_title "
	    		+ "    WHEN b.service_id IN (39, 41) "
	    		+ "    THEN d.programme_title "
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
	    		+ "      WHEN b.service_id IN (37, 42) "
	    		+ "      THEN c.id "
	    		+ "      WHEN b.service_id IN (39, 41) "
	    		+ "      THEN d.id "
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
	    		+ "  cdm6.name AS overall_result "
	    		+ "FROM "
	    		+ "  tbl_programme_trainee_enrollment_dtls a "
	    		+ "  LEFT JOIN tbl_programme_announcement_dtls b "
	    		+ "    ON a.course_enrol_app_no = b.application_no "
	    		+ "  LEFT JOIN tbl_accredited_course_dtls ac "
	    		+ "    ON b.programme_id = ac.id "
	    		+ "  LEFT JOIN tbl_curriculum_development cd "
	    		+ "    ON cd.id = ac.curriculum_id "
	    		+ "  LEFT JOIN tbl_ncs_app_dtls c "
	    		+ "    ON c.id = cd.programme_id "
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
	    		+ "WHERE a.result_status_id = 94 "
	    		+ "  AND b.application_status_id = 59 "
	    		+ "  AND b.service_id IN (37, 39, 41, 42) "
	    		+ "  AND b.application_no = ? "
	    		+ "  AND ird.registration_no = ?";
	    return jdbcTemplate.queryForList(sql, applicationNo, registrationNo);
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
	    			+ "    ON ncs.id = cd.programme_id "
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
	
	
	
}
