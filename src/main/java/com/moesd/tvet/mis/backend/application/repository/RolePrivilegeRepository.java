package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.RolePrivilege;
import jakarta.persistence.Tuple;

public interface RolePrivilegeRepository extends JpaRepository<RolePrivilege,Long>{
	@Query(value =   "SELECT "
			+ "  r.id, "
			+ "  r.role_name AS roleName, "
			+ "  r.description, "
			+ "  ( "
			+ "    SELECT JSON_ARRAYAGG(rp.privilege_id) "
			+ "    FROM tbl_role_privilege rp "
			+ "    WHERE rp.role_id = r.id "
			+ "  ) AS assignedPrivilegeId "
			+ "FROM tbl_role r "
			+ "WHERE r.status_id = 1", nativeQuery = true)
	List<Tuple> getAllPrivilegeRole();
	
	void deleteByRoleId(Integer integer);
	
}
