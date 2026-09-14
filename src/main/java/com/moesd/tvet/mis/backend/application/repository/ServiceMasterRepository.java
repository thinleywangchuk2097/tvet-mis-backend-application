package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;

import jakarta.persistence.Tuple;

public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Integer> {

	Optional<ServiceMaster> findById(Integer id);

	@NativeQuery("SELECT " + "  a.* " + "FROM " + "  tbl_service_master a "
			+ "WHERE a.id IN (37, 38, 39)")
	List<ServiceMaster> getServiceNameCourseAnnouncement();

	// Check if service name exists (for validation)
	boolean existsByServiceName(String serviceName);
	
	@NativeQuery("SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.is_active = 'Y'")
	List<Tuple> getAllServiceMaster();
}
