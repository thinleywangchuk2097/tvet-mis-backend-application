package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.ProgramMonitoring;
import jakarta.persistence.Tuple;

public interface ProgramMonitoringRepository extends JpaRepository<ProgramMonitoring, Long>{
	
	@Query(value =  
			"SELECT "
					+ "  pm.id, "
					+ "  pm.application_no, "
					+ "  pm.dzongkhag_id, "
					+ "  pm.exact_location, "
					+ "  pm.institute_name, "
					+ "  pm.status_id, "
					+ "  pm.institute_id, "
					+ "  pm.course_type_id, "
					+ "  pm.course_id, "
					+ "  pm.monitoring_date, "
					+ "  pm.registration_no, "
					+ "  pm.service_id, "
					+ "  pm.created_by, "
					+ "  pm.created_at, "
					+ "  pm.updated_by, "
					+ "  pm.updated_at, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', "
					+ "        pmc.id, "
					+ "        'standardId', "
					+ "        pmc.standard_id, "
					+ "        'responseId', "
					+ "        pmc.response_id, "
					+ "        'remarks', "
					+ "        pmc.remarks "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_program_monitoring_checklist_dtls pmc "
					+ "  WHERE pmc.monitoring_id = pm.id) AS checklists "
					+ "FROM "
					+ "  tbl_program_monitoring_dtls pm "
					+ "WHERE pm.registration_no = ?", nativeQuery = true)
	List<Tuple> getProgramMonitoring(String user_id);
	
	Optional<ProgramMonitoring> findByApplicationNo(String applicationNo);
	
	@Query(value =  
			 "SELECT "
					 + "  pm.id, "
					 + "  pm.application_no, "
					 + "  pm.dzongkhag_id, "
					 + "  pm.exact_location, "
					 + "  pm.institute_name, "
					 + "  pm.status_id, "
					 + "  pm.institute_id, "
					 + "  pm.course_type_id, "
					 + "  pm.course_id, "
					 + "  pm.monitoring_date, "
					 + "  pm.registration_no, "
					 + "  pm.service_id, "
					 + "  pm.created_by, "
					 + "  pm.created_at, "
					 + "  pm.updated_by, "
					 + "  pm.updated_at, "
					 + "  (SELECT "
					 + "    JSON_ARRAYAGG( "
					 + "      JSON_OBJECT( "
					 + "        'id', "
					 + "        pmc.id, "
					 + "        'standardId', "
					 + "        pmc.standard_id, "
					 + "        'responseId', "
					 + "        pmc.response_id, "
					 + "        'remarks', "
					 + "        pmc.remarks "
					 + "      ) "
					 + "    ) "
					 + "  FROM "
					 + "    tbl_program_monitoring_checklist_dtls pmc "
					 + "  WHERE pmc.monitoring_id = pm.id) AS checklists, "
					 + "  (SELECT "
					 + "    IFNULL ( "
					 + "      JSON_ARRAYAGG( "
					 + "        JSON_OBJECT( "
					 + "          'id', "
					 + "          d.id, "
					 + "          'documentName', "
					 + "          d.document_name, "
					 + "          'url', "
					 + "          d.upload_url "
					 + "        ) "
					 + "      ), "
					 + "      JSON_ARRAY () "
					 + "    ) "
					 + "  FROM "
					 + "    tbl_document_master d "
					 + "  WHERE d.application_no = pm.application_no) AS documents "
					 + "FROM "
					 + "  tbl_program_monitoring_dtls pm "
					 + "WHERE pm.application_no = ?", nativeQuery = true)
			List<Tuple> getProgramMonitoringByApplicationNo(String applicationNo);
	
	@Query(value = "SELECT "
			+ "  a.id, "
			+ "  a.service_name "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.id IN(26, 13)", nativeQuery = true)
	List<Tuple> getCourseService();
	
	@Query(value =  
			"SELECT "
					+ "  c.id, "
					+ "  c.programme_title AS course_name "
					+ "FROM "
					+ "  tbl_accredited_course_dtls a "
					+ "  LEFT JOIN tbl_curriculum_development b "
					+ "    ON a.curriculum_id = b.id "
					+ "  LEFT JOIN tbl_ncs_app_dtls c "
					+ "    ON c.id = b.programme_id "
					+ "WHERE a.institute_id = ? "
					+ "  AND a.status_id = 57", nativeQuery = true)
	List<Tuple> getAccreditedCourse(Integer institute_id);
	
	@Query(value =  
			"SELECT "
					+ "  a.id, "
					+ "  a.programme_title AS course_name "
					+ "FROM "
					+ "  tbl_non_accredited_course_dtls a "
					+ "WHERE a.institute_id = ? "
					+ "  AND a.status_id = 57", nativeQuery = true)
	List<Tuple> getNonAccreditedCourse(Integer institute_id);
}
