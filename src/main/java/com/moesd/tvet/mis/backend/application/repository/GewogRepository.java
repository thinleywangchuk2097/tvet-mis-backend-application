package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.Gewog;

public interface GewogRepository extends JpaRepository<Gewog, Integer> {

	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_gewog_master a "
			+ "WHERE a.dzongkhag_id = ?", nativeQuery = true)
	List<Gewog> getGewogByDzongkhagId(Integer dzongkhagId);

}
