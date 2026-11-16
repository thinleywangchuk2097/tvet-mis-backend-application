package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.Tutor;
import jakarta.persistence.Tuple;

public interface TutorRepository extends JpaRepository<Tutor, Long>{
	
	@NativeQuery("SELECT "
			+ "  a.*, "
			+ "  (SELECT GROUP_CONCAT(ts.subject_id ORDER BY ts.id) "
			+ "     FROM tbl_tutor_subject_dtls ts WHERE ts.tutor_id = a.id) AS subject_ids "
			+ "FROM "
			+ "  tbl_tutor_dtls a "
			+ "WHERE a.status_id = 1 "
			+ "  AND a.institute_id = ?")
	List<Tuple> getAllActiveTutors(String institute_id);

	Optional<Tutor> findByApplicationNo(String applicationNo);

	/** Add Tutor application with its subjects and the institute name (task list view). */
	@NativeQuery("SELECT "
			+ "  a.*, "
			+ "  (SELECT GROUP_CONCAT(ts.subject_id ORDER BY ts.id) "
			+ "     FROM tbl_tutor_subject_dtls ts WHERE ts.tutor_id = a.id) AS subject_ids, "
			+ "  (SELECT GROUP_CONCAT(s.subject_name ORDER BY ts.id SEPARATOR ', ') "
			+ "     FROM tbl_tutor_subject_dtls ts JOIN tbl_subject_dtls s ON s.id = ts.subject_id "
			+ "    WHERE ts.tutor_id = a.id) AS subject_names, "
			+ "  i.proposed_institute_name, "
			+ "  i.registration_no "
			+ "FROM "
			+ "  tbl_tutor_dtls a "
			+ "  LEFT JOIN tbl_institute_registration_dtls i "
			+ "    ON i.institute_id = a.institute_id "
			+ "WHERE a.application_no = ?1")
	List<Tuple> getTutorByApplicationNo(String applicationNo);

	
}
