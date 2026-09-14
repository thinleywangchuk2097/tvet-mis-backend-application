package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import com.moesd.tvet.mis.backend.application.model.DropdownChild;

public interface DropdownChildRepository extends JpaRepository<DropdownChild, Integer> {
	@NativeQuery("SELECT a.id, a.name FROM tbl_dropdown_child_master a WHERE a.parent_id = :parentId")
	List<Map<String, Object>> findChildByParentId(@Param("parentId") Integer parentId);
	
	@NativeQuery("SELECT "
			+ "  a.id, "
			+ "  a.service_name "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.id IN(25, 48, 49)")
	List<Map<String, Object>> getCurriculumServiceType();
	
}
