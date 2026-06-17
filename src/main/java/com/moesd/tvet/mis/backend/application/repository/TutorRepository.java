package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.Tutor;
import jakarta.persistence.Tuple;

public interface TutorRepository extends JpaRepository<Tutor, Long>{
	
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_tutor_dtls a "
			+ "WHERE a.status_id = 1 "
			+ "  AND a.institute_id = ?", nativeQuery = true)
	List<Tuple> getAllActiveTutors(Integer institute_id);
	
	@Query(value =  "SELECT "
			+ "  b.citizen_id, "
			+ "  b.first_name, "
			+ "  b.middle_name, "
			+ "  b.last_name, "
			+ "  a.tutor_id "
			+ "FROM "
			+ "  tbl_tuition_announcement_dtls a "
			+ "  LEFT JOIN tbl_tutor_dtls b "
			+ "    ON a.tutor_id = b.id "
			+ "    WHERE a.institute_id =? AND a.subject_id=?", nativeQuery = true)
	List<Tuple> getTutorBySubjectId(Integer institute_id, Integer subject_id);
	
}
