package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.TracerQuestionTypeDropdown;

import jakarta.persistence.Tuple;

public interface TracerQuestionTypeDropdownRepository extends JpaRepository<TracerQuestionTypeDropdown, Long>{
	
	@NativeQuery("SELECT "
					+ "  a.id, "
					+ "  a.label, "
					+ "  a.value "
					+ "FROM "
					+ "  tbl_tracer_question_type_dropdown_dtls a")
		List<Tuple> getTracerQuestionDropdownType();
	
	@NativeQuery("SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_dropdown_master a "
					+ "WHERE a.id IN (24, 25)")
		List<Tuple> getParentTracerTypes();
	

}
