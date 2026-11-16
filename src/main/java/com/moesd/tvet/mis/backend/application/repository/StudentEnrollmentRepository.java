package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.StudentEnrollment;

import jakarta.persistence.Tuple;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {

	/** Active enrolments of an SES centre with the subject name. */
	@NativeQuery("SELECT "
			+ "  a.*, "
			+ "  s.subject_name "
			+ "FROM "
			+ "  tbl_student_enrollment_dtls a "
			+ "  LEFT JOIN tbl_subject_dtls s "
			+ "    ON s.id = a.subject_id "
			+ "WHERE a.status_id = 1 "
			+ "  AND a.institute_id = ?1 "
			+ "ORDER BY a.enrollment_year DESC, a.id DESC")
	List<Tuple> getAllActiveEnrollments(Integer instituteId);
}
