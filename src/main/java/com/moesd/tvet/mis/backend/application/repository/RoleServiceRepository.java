package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.RoleService;


public interface RoleServiceRepository extends JpaRepository<RoleService, Integer>{
	
	@NativeQuery("SELECT a.* FROM tbl_role_service a WHERE a.assigned_role_id=? AND a.service_id =? AND a.status_id=?")
	Optional<RoleService> getNextAssignedRole(Integer roleId, Integer serviceId, Integer nextStatusId);
}
