package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentApp;
import jakarta.persistence.Tuple;

public interface CourseEnrollmentAppRepository extends JpaRepository<CourseEnrollmentApp, Long> {
	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_course_enrollment_app a "
			+ "  LEFT JOIN tbl_institute_registration_dtls b "
			+ "    ON a.institute_id = b.institute_id "
			+ "  LEFT JOIN tbl_user c "
			+ "    ON b.registration_no = c.user_id "
			+ "WHERE c.user_id = ? "
			+ "  AND a.service_id = ?", nativeQuery = true)
	List<Tuple> getCourseDetailsAnnouncementByUserId(String user_id, String service_id);
	
	@Query(value =  "SELECT "
			+ "  a.*, "
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
			+ "WHERE a.service_id IN(37, 38, 39)", nativeQuery = true)
	List<Tuple> getAllCourseAnnouncement();
	
	@Query(value =  "SELECT "
			+ "  a.*, "
			+ "  cd.name AS certification_name, "
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
			+ "  LEFT JOIN tbl_dropdown_child_master cd "
			+ "    ON cd.id = a.certification_level_id "
			+ "WHERE a.application_no = ? "
			+ "  AND a.service_id IN (37, 38, 39)", nativeQuery = true)
	List<Tuple> getCourseAnnouncementByApplicationNo(String application_no);
	
	@Query(value =  "SELECT "
			+ "  a.*, "
			+ "  cd.name AS certification_name, "
			+ "  CASE "
			+ "    WHEN a.service_id = 41 "
			+ "    THEN c.occupation_name "
			+ "    WHEN a.service_id = 42 "
			+ "    THEN d.occupation_name "
			+ "  END AS course_name "
			+ "FROM "
			+ "  tbl_course_enrollment_app a "
			+ "  LEFT JOIN tbl_accredited_course_dtls b "
			+ "    ON a.course_id = b.id "
			+ "  LEFT JOIN tbl_occupation_master c "
			+ "    ON c.id = b.course_id "
			+ "  LEFT JOIN tbl_occupation_master d "
			+ "    ON d.id = a.course_id "
			+ "  LEFT JOIN tbl_dropdown_child_master cd "
			+ "    ON cd.id = a.certification_level_id "
			+ "WHERE a.application_no = ? "
			+ "  AND a.service_id IN (41, 42)", nativeQuery = true)
	List<Tuple> getReAssessmentAnnouncementByApplicationNo(String application_no);
	
	Optional<CourseEnrollmentApp> findByApplicationNo(String application_no);
	
	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.id IN (41, 42)", nativeQuery = true)
	List<Tuple> getReAssessmentServiceName();
}
