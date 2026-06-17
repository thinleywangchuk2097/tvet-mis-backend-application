package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.MonitoringAssessment;
import jakarta.persistence.Tuple;

public interface MonitoringAssessmentRepository extends JpaRepository<MonitoringAssessment, Long> {
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_service_master a "
			+ "WHERE a.id IN (7, 36, 4)", nativeQuery = true)
	List<Tuple> getInstituteTypeDropdown();
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_institute_registration_dtls a "
					+ "WHERE a.service_id = ?", nativeQuery = true)
	List<Tuple> getInstituteDropdown(String service_id);
	
	@Query(value =  
			"SELECT "
					+ "  ma.id, "
					+ "  ma.application_no, "
					+ "  ma.dzongkhag_id, "
					+ "  ma.exact_location, "
					+ "  ma.institute_name, "
					+ "  ma.status_id, "
					+ "  ma.institute_id, "
					+ "  ma.monitoring_date, "
					+ "  ma.registration_no, "
					+ "  ma.service_id, "
					+ "  ma.created_by, "
					+ "  ma.created_at, "
					+ "  ma.updated_by, "
					+ "  ma.updated_at, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', mac.id, "
					+ "        'standardId', mac.standard_id, "
					+ "        'responseId', mac.response_id, "
					+ "        'remarks', mac.remarks "
					+ "      ) "
					+ "    ) "
					+ "  FROM tbl_monitoring_assessment_checklist_dtls mac "
					+ "  WHERE mac.assessment_id = ma.id) AS checklists "
					+ "FROM "
					+ "  tbl_monitoring_assessment_dtls ma "
					+ "WHERE "
					+ "  ma.registration_no = ?", nativeQuery = true)
	List<Tuple> getMonitoringAssessment(String user_id);
	
	Optional<MonitoringAssessment> findByApplicationNo(String applicationNo);
	
	@Query(value =  
	"SELECT "
			+ "  ma.id, "
			+ "  ma.application_no, "
			+ "  ma.dzongkhag_id, "
			+ "  ma.exact_location, "
			+ "  ma.institute_name, "
			+ "  ma.status_id, "
			+ "  ma.institute_id, "
			+ "  ma.monitoring_date, "
			+ "  ma.registration_no, "
			+ "  ma.service_id, "
			+ "  ma.created_by, "
			+ "  ma.created_at, "
			+ "  ma.updated_by, "
			+ "  ma.updated_at, "
			+ "  (SELECT "
			+ "    JSON_ARRAYAGG( "
			+ "      JSON_OBJECT( "
			+ "        'id', mac.id, "
			+ "        'standardId', mac.standard_id, "
			+ "        'responseId', mac.response_id, "
			+ "        'remarks', mac.remarks "
			+ "      ) "
			+ "    ) "
			+ "  FROM tbl_monitoring_assessment_checklist_dtls mac "
			+ "  WHERE mac.assessment_id = ma.id) AS checklists "
			+ "FROM "
			+ "  tbl_monitoring_assessment_dtls ma "
			+ "WHERE "
			+ "  ma.application_no = ?", nativeQuery = true)
	List<Tuple> getMonitoringAssessmentByApplicationNo(String applicationNo);
	
	
	@Query(value =  
	"SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_monitoring_assessment_dtls a "
			+ "WHERE a.registration_no = ?", nativeQuery = true)
	List<Tuple> getInstitutesRenewalStatus(String registrationNo);
	
}
