package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.OnCampusJobPlacementFirm;
import jakarta.persistence.Tuple;


public interface OnCampusJobPlacementFirmRepository extends JpaRepository<OnCampusJobPlacementFirm,Long>{
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_campus_job_placement_firm_dtls a "
					+ "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getFirmByInstituteId(String institute_id);
	
	Optional<OnCampusJobPlacementFirm> findById(Long id);
	
}
