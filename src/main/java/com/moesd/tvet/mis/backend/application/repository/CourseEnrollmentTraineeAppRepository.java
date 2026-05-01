package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentTraineeApp;
import jakarta.persistence.Tuple;

public interface CourseEnrollmentTraineeAppRepository extends JpaRepository<CourseEnrollmentTraineeApp, Long>{
	@Query(value = "SELECT "
			+ "  tp.*, "
			+ "  a.ca_start_date, "
			+ "  a.ca_end_date, "
			+ "  a.certification_level_id, "
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
			+ "  LEFT JOIN tbl_course_enrollment_trainee_app tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "WHERE tp.course_enrol_app_no = ? "
			+ "  AND a.service_id IN (37, 38, 39)", nativeQuery = true)
	List<Tuple> getCourseAppliedTraineesByApplicationNo(String application_no);
	
	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_course_enrollment_trainee_app a "
			+ "WHERE a.course_enrol_app_no = ?", nativeQuery = true)
	List<CourseEnrollmentTraineeApp> findByApplicationNo(String application_no);
	
	
	@Query(value =  "SELECT "
			+ "  tp.*, "
			+ "  a.application_start_date, "
			+ "  a.application_end_date, "
			+ "  a.course_start_date, "
			+ "  a.course_end_date, "
			+ "  a.certification_level_id, "
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
			+ "  LEFT JOIN tbl_course_enrollment_trainee_app tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "  LEFT JOIN tbl_institute_registration_dtls e "
			+ "    ON e.institute_id = a.institute_id "
			+ "  LEFT JOIN tbl_user f "
			+ "    ON f.user_id = e.registration_no "
			+ "WHERE f.user_id = ? AND tp.result_status_id =95 "
			+ "  AND a.service_id IN (37, 38, 39)", nativeQuery = true)
	List<Tuple> getFailedTraineeDetails(String user_id);
	
	
}
