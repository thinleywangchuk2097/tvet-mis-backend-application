package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.OJTTraineeDetails;

import jakarta.persistence.Tuple;

public interface OJTTraineeDetailsRepository extends JpaRepository<OJTTraineeDetails, Long>{
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_ojt_trainee_dtls a "
					+ "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getTraineeByInstituteId(String institute_id);
}
