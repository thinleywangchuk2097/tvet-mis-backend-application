package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;

import jakarta.persistence.Tuple;

public interface QualityStandardRepository extends JpaRepository<InstituteRegistrationQualityStandard, Integer> {

	List<InstituteRegistrationQualityStandard> findByServiceId(Integer serviceId);

	List<InstituteRegistrationQualityStandard> findByParentId(Integer parentId);
	
	@NativeQuery("SELECT DISTINCT "
			+ "  a.service_id, "
			+ "  b.service_name "
			+ "FROM "
			+ "  tbl_registration_quality_standard a "
			+ "  LEFT JOIN tbl_service_master b "
			+ "    ON a.service_id = b.id "
			+ "WHERE a.service_id IS NOT NULL "
			+ "ORDER BY b.service_name")
	List<Tuple> getServiceNameForQualityCheckList();
	
}
