package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.OnCampusJobPlacementSession;
import jakarta.persistence.Tuple;

public interface OnCampusJobPlacementSessionRepository extends JpaRepository<OnCampusJobPlacementSession,Long>{
	
	boolean existsBySessionName(String sessionName);
	
	Optional<OnCampusJobPlacementSession> findById(Long id);
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_campus_job_placement_session_dtls a "
					+ "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getPlacementSessionByInstituteId(String institute_id);
	
	
}
