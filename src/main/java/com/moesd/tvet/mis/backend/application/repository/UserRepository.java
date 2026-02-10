package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.User;
import jakarta.persistence.Tuple;

public interface UserRepository extends JpaRepository<User, Long>{
	@Query(value = "SELECT * FROM tbl_user a WHERE a.user_id = ?1 AND a.status_id = ?2", nativeQuery = true)
	Optional<User> findByUsername(String username, int status);
	
	Optional<User> findByUserId(String userId);
	
	@Query(value = "SELECT * FROM tbl_user a WHERE a.user_id = ?1 AND a.status_id = ?2", nativeQuery = true)
	List<Tuple> findNDIByUserId (String userId, int status);
	
	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_user a "
			+ "WHERE a.email_id = ? "
			+ "  AND a.status_id = 1", nativeQuery = true)
	Optional<User> findByEmailId(String emailId);
	
	@Query(value =  "SELECT "
			+ "  a.*, "
			+ "  ( "
			+ "    SELECT JSON_ARRAYAGG(role_id) "
			+ "    FROM tbl_user_role "
			+ "    WHERE user_id = a.id "
			+ "  ) AS roles "
			+ "FROM "
			+ "  tbl_user a "
			+ "WHERE "
			+ "  a.status_id = 1", nativeQuery = true)
	List<Tuple> getAllUsers();
	
	@Query(value =  "SELECT "
			+ "  a.user_id, "
			+ "  CONCAT( "
			+ "    a.first_name, "
			+ "    ' ', "
			+ "    CASE "
			+ "      WHEN a.middle_name IS NOT NULL "
			+ "      AND a.middle_name != '' "
			+ "      THEN CONCAT(a.middle_name, ' ') "
			+ "      ELSE '' "
			+ "    END, "
			+ "    a.last_name "
			+ "  ) AS username, "
			+ "  b.role_name AS current_role_name "
			+ "FROM "
			+ "  tbl_user a "
			+ "  LEFT JOIN tbl_role b "
			+ "    ON a.current_role = b.id "
			+ "WHERE a.user_id = ?", nativeQuery = true)
	List<Tuple> getUserNameCurrentRoleName(String roleId);
}
