package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.Sector;

import jakarta.persistence.Tuple;

public interface SectorOccupationRepository extends JpaRepository<Sector, Integer>{
	
	@Query(value =  
			"SELECT "
					+ "  JSON_ARRAYAGG( "
					+ "    JSON_OBJECT( "
					+ "      'sector_id', "
					+ "      s.id, "
					+ "      'sector_name', "
					+ "      s.sector_name, "
					+ "      'occupations', "
					+ "      (SELECT "
					+ "        JSON_ARRAYAGG( "
					+ "          JSON_OBJECT( "
					+ "            'occupation_id', "
					+ "            o.id, "
					+ "            'occupation_name', "
					+ "            o.occupation_name, "
					+ "            'isco_code', "
					+ "            o.isco_code "
					+ "          ) "
					+ "        ) "
					+ "      FROM "
					+ "        tbl_occupation_master o "
					+ "      WHERE o.sector_id = s.id "
					+ "        AND o.is_active = 'Y') "
					+ "    ) "
					+ "  ) AS result "
					+ "FROM "
					+ "  tbl_sector_master s "
					+ "WHERE s.is_active = 'Y'", nativeQuery = true)
		List<Tuple> getSectorOccupationLists();

}
