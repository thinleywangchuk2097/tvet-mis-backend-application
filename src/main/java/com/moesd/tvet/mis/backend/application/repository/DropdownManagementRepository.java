package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.moesd.tvet.mis.backend.application.model.DropdownParent;


public interface DropdownManagementRepository extends JpaRepository<DropdownParent, Integer>{
	@Modifying
	@Query("DELETE FROM DropdownChild c WHERE c.parent.id = :parentId")
	int deleteChildrenByParentId(@Param("parentId") Integer parentId);

	@Modifying
	@Query("DELETE FROM DropdownParent p WHERE p.id = :parentId")
	int deleteParentById(@Param("parentId") Integer parentId);

	@EntityGraph(attributePaths = { "dropdownChild" })
	@Query("SELECT d FROM DropdownParent d")
	List<DropdownParent> findAllWithChildren();
	
	@Query("SELECT c.id FROM DropdownChild c WHERE c.id = :id")
	Optional<Integer> findChildById(@Param("id") Integer id);
}
