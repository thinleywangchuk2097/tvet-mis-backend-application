package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.OnCampusJobPlacementTrainee;

import jakarta.persistence.Tuple;

public interface OnCampusJobPlacementTraineeRepository extends JpaRepository<OnCampusJobPlacementTrainee,Long>{
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_campus_job_placement_trainee_dtls a "
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
					+ "  t.institute_id, "
					+ "  t.placement_date, "
					+ "  t.start_date, "
					+ "  f.id AS firm_id, "
					+ "  f.firm_name, "
					+ "  f.registration_no AS firm_registration_no, "
					+ "  f.contact_person AS firm_contact_person, "
					+ "  f.contact_phone AS firm_contact_phone, "
					+ "  f.contact_email AS firm_contact_email, "
					+ "  f.dzongkhag_id, "
					+ "  f.address AS firm_address, "
					+ "  s.id AS session_id, "
					+ "  s.session_name, "
					+ "  s.session_date, "
					+ "  s.session_time, "
					+ "  s.venue, "
					+ "  t.created_at, "
					+ "  t.updated_at "
					+ "FROM "
					+ "  tbl_campus_job_placement_trainee_dtls t "
					+ "  INNER JOIN tbl_campus_job_placement_firm_dtls f "
					+ "    ON t.firm_id = f.id "
					+ "  INNER JOIN tbl_campus_job_placement_session_dtls s "
					+ "    ON f.session_id = s.id "
					+ "WHERE 1 = 1 "
					+ "ORDER BY t.created_at DESC", nativeQuery = true)
	List<Tuple> getTraineeOnPlacementReport();
	
	
	
}
