package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.RoleService;


public interface RoleServiceRepository extends JpaRepository<RoleService, Integer>{
	@Query(value = "SELECT a.* FROM tbl_role_service a WHERE FIND_IN_SET(?1,a.current_role_id) AND a.service_id = ?2 AND a.next_status_id = ?3", nativeQuery = true)
	Optional<RoleService> getNextAssignedRole(Integer roleId, Integer serviceId, Integer nextStatusId);
}
