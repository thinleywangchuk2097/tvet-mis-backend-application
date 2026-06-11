package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;

import jakarta.persistence.Tuple;

public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Integer> {

	Optional<ServiceMaster> findById(Integer id);

	@Query(value = "SELECT " + "  a.* " + "FROM " + "  tbl_service_master a "
			+ "WHERE a.id IN (37, 38, 39)", nativeQuery = true)
	List<ServiceMaster> getServiceNameCourseAnnouncement();

	// Check if service name exists (for validation)
	boolean existsByServiceName(String serviceName);
	
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.is_active = 'Y'", nativeQuery = true)
	List<Tuple> getAllServiceMaster();
}
