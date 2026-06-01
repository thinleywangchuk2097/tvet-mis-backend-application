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
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
	void deleteByUserId(Long userId);
}
