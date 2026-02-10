package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.dto.Privilegedto;
import com.moesd.tvet.mis.backend.application.model.Privilege;


public interface PrivilegeRepository extends JpaRepository<Privilege, Long>{
	@Query(value = "SELECT "
			+ "  b.* "
			+ "FROM "
			+ "  tbl_role_privilege a "
			+ "  LEFT JOIN tbl_privilege b "
			+ "    ON a.privilege_id = b.id "
			+ "WHERE a.role_id = ?1 "
			+ "  AND b.is_display = 1 "
			+ "ORDER BY b.dis_play_order ASC", nativeQuery = true)
	List<Privilegedto> getPrivileges(String roleId);
	
	@Query(value = "SELECT a.* FROM tbl_privilege a WHERE a.parent_id IS NULL", nativeQuery = true)
	List<Privilegedto> getParentPrivileges();
	
	@Query(value =  "SELECT a.* FROM tbl_privilege a WHERE a.parent_id=?", nativeQuery = true)
	List<Privilegedto> getChildPrivileges(String roleId);
	
	Optional<Privilege> findById(Long id);
}
