package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.RoleService;


public interface RoleServiceRepository extends JpaRepository<RoleService, Integer>{
	
	@Query(value = "SELECT a.* FROM tbl_role_service a WHERE a.assigned_role_id=? AND a.service_id =? AND a.status_id=?", nativeQuery = true)
	Optional<RoleService> getNextAssignedRole(Integer roleId, Integer serviceId, Integer nextStatusId);
}
