package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;


public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Integer>{
	Optional<ServiceMaster> findById(Integer id);
	
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.id IN (37, 38, 39)", nativeQuery = true)
	List<ServiceMaster> getServiceNameCourseAnnouncement();
}
