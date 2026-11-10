package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentTraineeApp;
import jakarta.persistence.Tuple;

public interface CourseEnrollmentTraineeAppRepository extends JpaRepository<CourseEnrollmentTraineeApp, Long>{
	// The announcement's programme_id is the approved accredited course: its NCS programme comes
	// from the curriculum, or from ncs_id when the course has no curriculum.
	// Older RPL / re-assessment rows stored the NCS id directly (d), so that is the fallback.
	@NativeQuery("SELECT "
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
			+ "    THEN COALESCE(c.programme_title, d.programme_title) "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_programme_announcement_dtls a "
			+ "  LEFT JOIN tbl_accredited_course_dtls ac "
			+ "    ON ac.id = a.programme_id "
			+ "  LEFT JOIN tbl_curriculum_development cd "
			+ "    ON cd.id = ac.curriculum_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls c "
			+ "    ON c.id = COALESCE(cd.programme_id, ac.ncs_id) "
			+ "  LEFT JOIN tbl_non_accredited_course_dtls nc "
			+ "    ON a.programme_id = nc.id "
			+ "  LEFT JOIN tbl_ncs_app_dtls d "
			+ "    ON d.id = a.programme_id "
			+ "  LEFT JOIN tbl_programme_trainee_enrollment_dtls tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "WHERE tp.course_enrol_app_no = ? "
			+ "  AND a.service_id IN (37, 38, 39)")
	List<Tuple> getCourseAppliedTraineesByApplicationNo(String applicationNo);
	
	
	// The announcement's programme_id is the approved accredited course: its NCS programme comes
	// from the curriculum, or from ncs_id when the course has no curriculum.
	// Older RPL / re-assessment rows stored the NCS id directly (d), so that is the fallback.
	@NativeQuery("SELECT "
			+ "  tp.*, "
			+ "  a.ca_start_date, "
			+ "  a.ca_end_date, "
			+ "  a.application_start_date, "
			+ "  a.application_end_date, "
			+ "  a.course_start_date, "
			+ "  a.course_end_date, "
			+ "  a.certification_level_id, "
			+ "  CASE "
			+ "    WHEN a.service_id IN (41, 42) "
			+ "    THEN COALESCE(c.programme_title, d.programme_title) "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_programme_announcement_dtls a "
			+ "  LEFT JOIN tbl_accredited_course_dtls ac "
			+ "    ON ac.id = a.programme_id "
			+ "  LEFT JOIN tbl_curriculum_development cd "
			+ "    ON cd.id = ac.curriculum_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls c "
			+ "    ON c.id = COALESCE(cd.programme_id, ac.ncs_id) "
			+ "  LEFT JOIN tbl_ncs_app_dtls d "
			+ "    ON d.id = a.programme_id "
			+ "  LEFT JOIN tbl_programme_trainee_enrollment_dtls tp "
			+ "    ON a.application_no = tp.course_enrol_app_no "
			+ "WHERE tp.course_enrol_app_no = ? "
			+ "  AND a.service_id IN (41, 42)")
	List<Tuple> getCourseAppliedTraineesReAssessmentByApplicationNo(String applicationNo);
	
	
	@NativeQuery("SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_programme_trainee_enrollment_dtls a "
			+ "WHERE a.course_enrol_app_no = ?")
	List<CourseEnrollmentTraineeApp> findByApplicationNo(String application_no);
	
	
	// The announcement's programme_id is the approved accredited course: its NCS programme comes
	// from the curriculum, or from ncs_id when the course has no curriculum.
	// Older RPL / re-assessment rows stored the NCS id directly (d), so that is the fallback.
	@NativeQuery("SELECT "
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
			+ "    THEN COALESCE(c.programme_title, d.programme_title) "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_programme_announcement_dtls a "
			+ "  LEFT JOIN tbl_accredited_course_dtls ac "
			+ "    ON ac.id = a.programme_id "
			+ "  LEFT JOIN tbl_curriculum_development cd "
			+ "    ON cd.id = ac.curriculum_id "
			+ "  LEFT JOIN tbl_ncs_app_dtls c "
			+ "    ON c.id = COALESCE(cd.programme_id, ac.ncs_id) "
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
			+ "  AND a.certification_level_id = ? "
			+ "  AND a.service_id = ? "
			+ "  AND tp.result_status_id = 95 "
			+ "  AND a.service_id IN (37, 38, 39)")
	List<Tuple> getFailedTraineeDetails(String userId, String courseId, Integer certificationLevelId,Integer serviceId);
	
	@NativeQuery("SELECT "
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
			+ "  AND b.programme_id = ? "
			+ "  AND b.certification_level_id = ? "
			+ "  AND a.result_status_id = 95 ")
	List<CourseEnrollmentTraineeApp> getFailedTraineeReassessment(String user_id, Long programme_id, Integer certification_level_id);
	
	@NativeQuery("SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_assessor_task_assignment a "
			+ "WHERE a.application_no = ?")
	List<Tuple> fetchAssignedAssessors(String applicationNo);
	
	@NativeQuery("SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_programme_trainee_enrollment_dtls a "
			+ "WHERE a.id =?")
	Optional<CourseEnrollmentTraineeApp> removeTraineeFromSelectedProgramme(Long traineeId);
}
