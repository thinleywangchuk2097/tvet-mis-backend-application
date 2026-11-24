package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.moesd.tvet.mis.backend.application.model.UserRole;

import jakarta.transaction.Transactional;


public interface UserRoleRepository extends JpaRepository<UserRole, Long>{
	@Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") Long userId);

	// true when the user already has this role (tbl_user_role)
	@Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
	boolean existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Integer roleId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
	void deleteByUserId(Long userId);
}
