package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.TracerQuestionTypeDropdown;

import jakarta.persistence.Tuple;

public interface TracerQuestionTypeDropdownRepository extends JpaRepository<TracerQuestionTypeDropdown, Long>{
	
	@Query(value =  
			"SELECT "
					+ "  a.id, "
					+ "  a.label, "
					+ "  a.value "
					+ "FROM "
					+ "  tbl_tracer_question_type_dropdown_dtls a", nativeQuery = true)
		List<Tuple> getTracerQuestionDropdownType();
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_dropdown_master a "
					+ "WHERE a.id IN (24, 25)", nativeQuery = true)
		List<Tuple> getParentTracerTypes();
	

}
