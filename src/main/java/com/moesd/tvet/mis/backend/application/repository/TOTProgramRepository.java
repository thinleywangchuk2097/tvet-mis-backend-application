package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.TOTProgram;

import jakarta.persistence.Tuple;

public interface TOTProgramRepository extends JpaRepository<TOTProgram, Long> {
	
	Optional<TOTProgram> findById(Long id);
	
	@Query(value =  
			"SELECT "
					+ "    p.id, "
					+ "    p.program_name, "
					+ "    p.program_code, "
					+ "    p.program_type_id, "
					+ "    p.description, "
					+ "    p.status_id, "
					+ "    p.created_at, "
					+ "    ( "
					+ "        SELECT JSON_ARRAYAGG( "
					+ "            JSON_OBJECT( "
					+ "                'id', m.id, "
					+ "                'moduleName', m.module_name, "
					+ "                'moduleCode', m.module_code, "
					+ "                'description', m.description, "
					+ "                'prerequisites', m.prerequisites, "
					+ "                'duration', m.duration, "
					+ "                'learningOutcomes', m.learning_outcomes, "
					+ "                'moduleOrder', m.module_order "
					+ "            ) "
					+ "        ) "
					+ "        FROM tbl_tot_module_dtls m "
					+ "        WHERE m.program_id = p.id "
					+ "    ) AS modules "
					+ "FROM tbl_tot_program_dtls p "
					+ "WHERE p.status_id = 122", nativeQuery = true)
	List<Tuple> getToTPrograms();
	
	@Query(value =  
			 "SELECT "
					 + "  a.* "
					 + "FROM "
					 + "  tbl_tot_announcement_dtls a", nativeQuery = true)
	List<Tuple> getToTProgramsAnnouncement();
	
	
}
