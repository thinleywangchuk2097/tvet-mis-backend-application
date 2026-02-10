package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.moesd.tvet.mis.backend.application.model.Role;
import jakarta.persistence.Tuple;

public interface RoleRepository extends JpaRepository<Role, Integer>{
	Optional<Role> findById(Integer Id);
	 
	boolean existsByRoleName(String roleName);

	//boolean existsByRoleNameAndIdNot(String roleName, Integer id);

	@Query("SELECT r FROM Role r LEFT JOIN FETCH r.privileges WHERE r.id = :id")
	Optional<Role> findByIdWithPrivileges(@Param("id") Integer integer);
	
	@Query(value = "SELECT a.* FROM tbl_role a WHERE a.status_id=1", nativeQuery = true)
	List<Tuple> getRoles();
	
	@Query(value =  "SELECT "
			+ "  c.role_name, "
			+ "  c.id, "
			+ "  d.role_name AS current_role_name, "
			+ "  a.current_role "
			+ "FROM "
			+ "  tbl_user a "
			+ "  LEFT JOIN tbl_user_role b "
			+ "    ON a.id = b.user_id "
			+ "  LEFT JOIN tbl_role c "
			+ "    ON b.role_id = c.id "
			+ "  LEFT JOIN tbl_role d "
			+ "    ON a.current_role = d.id "
			+ "WHERE a.user_id = ?", nativeQuery = true)
	List<Tuple> getUserAssociatedRoles(String roleId);
}
