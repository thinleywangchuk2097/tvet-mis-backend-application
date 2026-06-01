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
				+ "    WHEN a.service_id = 37 "
				+ "    THEN c.occupation_name "
				+ "    WHEN a.service_id = 38 "
				+ "    THEN nc.course_title "
				+ "    WHEN a.service_id = 39 "
				+ "    THEN d.occupation_name "
				+ "  END AS course_name "
				+ "FROM "
				+ "  tbl_course_enrollment_app a "
				+ "  LEFT JOIN tbl_accredited_course_dtls b "
				+ "    ON a.course_id = b.id "
				+ "  LEFT JOIN tbl_occupation_master c "
				+ "    ON c.id = b.course_id "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON a.course_id = nc.id "
				+ "  LEFT JOIN tbl_occupation_master d "
				+ "    ON d.id = a.course_id "
				+ "  LEFT JOIN tbl_dropdown_child_master e "
				+ "    ON e.id = a.certification_level_id "
				+ "  LEFT JOIN tbl_institute_registration_dtls f "
				+ "    ON f.institute_id = a.institute_id "
				+ "  LEFT JOIN tbl_dzongkhag_master g "
				+ "    ON g.id = a.training_location_id "
				+ "WHERE a.service_id IN (37, 38, 39) "
				+ "  AND a.course_end_date >= CURRENT_DATE";
		return jdbcTemplate.queryForList(sql);	
	}
	
	public List<Map<String, Object>> getAllInstitutes() {
		String sql =   "SELECT a.* "
				+ " FROM tbl_institute_registration_dtls a";
		return jdbcTemplate.queryForList(sql);
		
	}
	public List<Map<String, Object>> getCourseBySector() {
		String sql =    "SELECT "
				+ "  b.sector_name, "
				+ "  COUNT(a.application_no) AS sector_value "
				+ "FROM "
				+ "  tbl_accredited_course_dtls a "
				+ "  LEFT JOIN tbl_sector_master b "
				+ "    ON a.sector_id = b.id "
				+ "WHERE a.status_id = 57 "
				+ "GROUP BY a.sector_id";
		return jdbcTemplate.queryForList(sql);
		
	}
	
	
	public List<Map<String, Object>> getCourseAnnounceNotifications() {
		String sql =    "SELECT "
				+ "  CASE "
				+ "    WHEN a.service_id = 37 "
				+ "    THEN c.occupation_name "
				+ "    WHEN a.service_id = 38 "
				+ "    THEN nc.course_title "
				+ "    WHEN a.service_id = 39 "
				+ "    THEN d.occupation_name "
				+ "  END AS course_name, "
				+ "  a.course_start_date, "
				+ "  a.course_end_date "
				+ "FROM "
				+ "  tbl_course_enrollment_app a "
				+ "  LEFT JOIN tbl_accredited_course_dtls b "
				+ "    ON a.course_id = b.id "
				+ "  LEFT JOIN tbl_occupation_master c "
				+ "    ON c.id = b.course_id "
				+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
				+ "    ON a.course_id = nc.id "
				+ "  LEFT JOIN tbl_occupation_master d "
				+ "    ON d.id = a.course_id "
				+ "WHERE a.service_id IN (37, 38, 39) "
				+ "  AND a.application_end_date >= CURRENT_DATE";
		return jdbcTemplate.queryForList(sql);
		
	}
	
}
