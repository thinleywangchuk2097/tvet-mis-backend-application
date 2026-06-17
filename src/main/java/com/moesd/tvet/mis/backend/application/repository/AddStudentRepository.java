package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.AddStudent;
import jakarta.persistence.Tuple;

public interface AddStudentRepository extends JpaRepository<AddStudent, Long> {
	
	@Query(value =  "SELECT "
			+ "  s.id, "
			+ "  s.student_code, "
			+ "  s.citizen_id, "
			+ "  s.first_name, "
			+ "  s.middle_name, "
			+ "  s.last_name, "
			+ "  s.email, "
			+ "  s.mobile_no, "
			+ "  s.date_of_birth, "
			+ "  s.gender_id, "
			+ "  s.dzongkhag_id, "
			+ "  s.exact_location, "
			+ "  s.emergency_contact_name, "
			+ "  s.emergency_contact_no, "
			+ "  s.enrollment_date, "
			+ "  s.current_class, "
			+ "  s.school_name, "
			+ "  s.school_exact_location, "
			+ "  s.status_id, "
			+ "  s.institute_id, "
			+ "  s.created_by, "
			+ "  s.created_at, "
			+ "  s.updated_by, "
			+ "  s.updated_at, "
			+ "  (SELECT "
			+ "    IFNULL( "
			+ "      JSON_ARRAYAGG( "
			+ "        JSON_OBJECT( "
			+ "          'id', "
			+ "          ss.id, "
			+ "          'subjectId', "
			+ "          ss.subject_id, "
			+ "          'tutorId', "
			+ "          ss.tutor_id "
			+ "        ) "
			+ "      ), "
			+ "      JSON_ARRAY() "
			+ "    ) "
			+ "  FROM "
			+ "    tbl_student_subject_dtls ss "
			+ "  WHERE ss.student_id = s.id) AS subjects "
			+ "FROM "
			+ "  tbl_student_dtls s "
			+ "WHERE s.institute_id = ? AND s.status_id = 1 "
			+ "ORDER BY s.id DESC", nativeQuery = true)
	List<Tuple> getAllActiveStudents(Integer institute_id);
}
