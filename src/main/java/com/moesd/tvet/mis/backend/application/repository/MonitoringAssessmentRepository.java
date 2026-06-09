package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.MonitoringAssessment;

import jakarta.persistence.Tuple;

public interface MonitoringAssessmentRepository extends JpaRepository<MonitoringAssessment, Long> {
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.id IN (7, 36, 4)", nativeQuery = true)
	List<Tuple> getInstituteTypeDropdown();
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_institute_registration_dtls a "
					+ "WHERE a.service_id = ?", nativeQuery = true)
	List<Tuple> getInstituteDropdown(String service_id);
}
