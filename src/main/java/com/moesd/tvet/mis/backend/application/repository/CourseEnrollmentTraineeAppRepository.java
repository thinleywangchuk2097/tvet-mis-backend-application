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
			+ "  a.application_status_id, "
			+ "  CASE "
			+ "    WHEN a.service_id = 37 "
			+ "    THEN c.programme_title "
			+ "    WHEN a.service_id = 38 "
			+ "    THEN nc.programme_title "
			+ "    WHEN a.service_id = 39 "
			+ "    THEN d.programme_title "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_programme_announcement_dtls a "
			+ "  LEFT JOIN tbl_curriculum_development cd "
			+ "  ON cd.programme_id = a.programme_id "
			+ "  LEFT JOIN tbl_accredited_course_dtls b "
			+ "    ON cd.id = b.curriculum_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls c "
			+ "    ON c.id = a.programme_id "
			+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
			+ "    ON a.programme_id = nc.id "
			+ "  LEFT JOIN tbl_ncs_app_dtls d "
			+ "    ON d.id = a.programme_id "
			+ "  LEFT JOIN tbl_programme_trainee_enrollment_dtls tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "WHERE tp.course_enrol_app_no = ? "
			+ "  AND a.service_id IN (37, 38, 39)", nativeQuery = true)
	List<Tuple> getCourseAppliedTraineesByApplicationNo(String application_no);
	
	
	@Query(value = "SELECT "
			+ "  tp.*, "
			+ "  a.ca_start_date, "
			+ "  a.ca_end_date, "
			+ "  a.application_start_date, "
			+ "  a.application_end_date, "
			+ "  a.course_start_date, "
			+ "  a.course_end_date, "
			+ "  a.certification_level_id, "
			+ "  CASE "
			+ "    WHEN a.service_id = 41 "
			+ "    THEN c.programme_title "
			+ "    WHEN a.service_id = 42 "
			+ "    THEN d.programme_title "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_programme_announcement_dtls a "
			+ "  LEFT JOIN tbl_curriculum_development cd "
			+ "    ON cd.programme_id = a.programme_id "
			+ "  LEFT JOIN tbl_accredited_course_dtls b "
			+ "    ON cd.id = b.curriculum_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls c "
			+ "    ON c.id = cd.programme_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls d "
			+ "    ON d.id = a.programme_id "
			+ "  LEFT JOIN tbl_programme_trainee_enrollment_dtls tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "WHERE tp.course_enrol_app_no = ? "
			+ "  AND a.service_id IN (41, 42)", nativeQuery = true)
	List<Tuple> getCourseAppliedTraineesReAssessmentByApplicationNo(String application_no);
	
	
	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_programme_trainee_enrollment_dtls a "
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
			+ "    THEN c.programme_title "
			+ "    WHEN a.service_id = 38 "
			+ "    THEN nc.programme_title "
			+ "    WHEN a.service_id = 39 "
			+ "    THEN d.programme_title "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_programme_announcement_dtls a "
			+ "  LEFT JOIN tbl_curriculum_development cd "
			+ "    ON cd.programme_id = a.programme_id "
			+ "  LEFT JOIN tbl_accredited_course_dtls b "
			+ "    ON cd.id = b.curriculum_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls c "
			+ "    ON c.id = cd.programme_id "
			+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
			+ "    ON a.programme_id = nc.id "
			+ "  LEFT JOIN tbl_ncs_app_dtls d "
			+ "    ON d.id = a.programme_id "
			+ "  LEFT JOIN tbl_programme_trainee_enrollment_dtls tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "  LEFT JOIN tbl_institute_registration_dtls e "
			+ "    ON e.institute_id = a.institute_id "
			+ "  LEFT JOIN tbl_user f "
			+ "    ON f.user_id = e.registration_no "
			+ "WHERE f.user_id = ? "
			+ "  AND a.programme_id = ? "
			+ "  AND tp.result_status_id = 95 "
			+ "  AND a.service_id IN (37, 38, 39)", nativeQuery = true)
	List<Tuple> getFailedTraineeDetails(String user_id, String course_id);
	
	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_programme_trainee_enrollment_dtls a "
			+ "  LEFT JOIN tbl_programme_announcement_dtls b "
			+ "    ON a.course_enrol_app_no = b.application_no "
			+ "  LEFT JOIN tbl_institute_registration_dtls c "
			+ "    ON c.institute_id = b.institute_id "
			+ "  LEFT JOIN tbl_user d "
			+ "    ON d.user_id = c.registration_no "
			+ "WHERE d.user_id = ? "
			+ "  AND b.programme_id = ?", nativeQuery = true)
	List<CourseEnrollmentTraineeApp> getFailedTraineeReassessment(String user_id, String course_id);
	
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_assessor_task_assignment a "
			+ "WHERE a.application_no = ?", nativeQuery = true)
	List<Tuple> fetchAssignedAssessors(String application_no);
}
