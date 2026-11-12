package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
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

	
}
