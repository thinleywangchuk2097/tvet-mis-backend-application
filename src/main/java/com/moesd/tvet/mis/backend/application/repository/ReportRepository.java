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
	
}
