package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.OJTTraineeDetails;

import jakarta.persistence.Tuple;

public interface OJTTraineeDetailsRepository extends JpaRepository<OJTTraineeDetails, Long>{
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_ojt_trainee_dtls a "
					+ "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getTraineeByInstituteId(String institute_id);
	
	@Query(value =  
			"SELECT "
					+ "  t.id AS trainee_id, "
					+ "  t.trainee_cid, "
					+ "  t.trainee_name, "
					+ "  t.course_id, "
					+ "  t.position, "
					+ "  t.salary, "
					+ "  t.remarks, "
					+ "  t.employment_status_id, "
					+ "  a.id AS agreement_id, "
					+ "  a.agreement_title, "
					+ "  a.start_date AS agreement_start_date, "
					+ "  a.end_date AS agreement_end_date, "
					+ "  a.super_visor_name, "
					+ "  a.supervisor_contact_no, "
					+ "  c.id AS company_id, "
					+ "  c.company_name, "
					+ "  c.registration_no, "
					+ "  c.contact_person_name, "
					+ "  c.contact_person_mobile_no, "
					+ "  c.contact_person_email, "
					+ "  c.dzongkhag_id, "
					+ "  c.address AS company_address, "
					+ "  t.institute_id, "
					+ "  t.created_at, "
					+ "  t.updated_at "
					+ "FROM "
					+ "  tbl_ojt_trainee_dtls t "
					+ "  INNER JOIN tbl_ojt_company_agreement_dtls a "
					+ "    ON t.agreement_id = a.id "
					+ "  INNER JOIN tbl_ojt_company_dtls c "
					+ "    ON a.company_id = c.id "
					+ "WHERE 1 = 1 "
					+ "ORDER BY t.created_at DESC", nativeQuery = true)
	List<Tuple> getTraineeOJTReport();
	
}
