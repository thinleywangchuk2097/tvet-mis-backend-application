package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.NonAccreditedCourse;
import jakarta.persistence.Tuple;

public interface NonAccreditedCourseRepository extends JpaRepository<NonAccreditedCourse,Long>{
	
	@Query(value = 
			 "SELECT "
					 + "  n.id, "
					 + "  n.application_no, "
					 + "  n.institute_id, "
					 + "  n.course_title, "
					 + "  n.fees_per_trainee, "
					 + "  n.enrolment_capacity, "
					 + "  n.curriculum_id, "
					 + "  n.status_id, "
					 + "  n.registration_date, "
					 + "  n.validity_date, "
					 + "  n.service_id, "
					 + "  n.created_by, "
					 + "  n.created_at, "
					 + "  n.updated_by, "
					 + "  n.updated_at, "
					 + "  k.proposed_institute_name, "
					 + "  k.email_id, "
					 + "  k.mobile_no, "
					 + "  k.registration_no, "
					 + "  t.task_status_id, "
					 + "  cd.curriculum_title, "
					 + "  cd.total_ojt_duration, "
					 + "  cd.total_practical_duration, "
					 + "  cd.total_theory_duration, "
					 + "  cd.certificate_level_id, "
					 + "  (SELECT "
					 + "    JSON_ARRAYAGG( "
					 + "      JSON_OBJECT( "
					 + "        'id', "
					 + "        d.id, "
					 + "        'documentName', "
					 + "        d.document_name, "
					 + "        'url', "
					 + "        d.upload_url, "
					 + "        'createdAt', "
					 + "        d.created_at "
					 + "      ) "
					 + "    ) "
					 + "  FROM "
					 + "    tbl_document_master d "
					 + "  WHERE d.application_no = n.application_no) AS documents, "
					 + "  (SELECT "
					 + "    JSON_ARRAYAGG( "
					 + "      JSON_OBJECT( "
					 + "        'id', "
					 + "        q.id, "
					 + "        'standardId', "
					 + "        q.standard_id, "
					 + "        'responseId', "
					 + "        q.response_id, "
					 + "        'remarks', "
					 + "        q.remarks "
					 + "      ) "
					 + "    ) "
					 + "  FROM "
					 + "    tbl_non_accredited_course_quality_standard_response q "
					 + "  WHERE q.application_no = n.application_no) AS quality_standard_responses "
					 + "FROM "
					 + "  tbl_non_accredited_course_dtls n "
					 + "  LEFT JOIN tbl_task_dtls t "
					 + "    ON n.application_no = t.application_no "
					 + "  LEFT JOIN tbl_institute_registration_dtls k "
					 + "    ON k.institute_id = n.institute_id "
					 + "  LEFT JOIN tbl_curriculum_development cd "
					 + "    ON cd.id = n.curriculum_id "
					 + "WHERE n.application_no = ?1", nativeQuery = true)
		List<Tuple> getNonAccreditedCourseByApplicationNo(String application_no);
	
		Optional<NonAccreditedCourse> findByApplicationNo(String applicationNo);
		
		@Query(value =  
				"SELECT "
						+ "  a.*, "
						+ "  d.name AS status_name "
						+ "FROM "
						+ "  tbl_non_accredited_course_dtls a "
						+ "  LEFT JOIN tbl_institute_registration_dtls b "
						+ "    ON a.institute_id = b.institute_id "
						+ "  LEFT JOIN tbl_user c "
						+ "    ON c.user_id = b.registration_no "
						+ "  LEFT JOIN tbl_dropdown_child_master d "
						+ "    ON d.id = a.status_id "
						+ "WHERE c.user_id = ?", nativeQuery = true)
		List<Tuple> getNonAccreditedCourseDetailsByUserId(String user_id);
		
		@Query(value =  
				"SELECT "
						+ "  a.id, "
						+ "  a.enrolment_capacity, "
						+ "  a.fees_per_trainee, "
						+ "  e.certificate_level_id, "
						+ "  a.course_title AS course_name "
						+ "FROM "
						+ "  tbl_non_accredited_course_dtls a "
						+ "  LEFT JOIN tbl_institute_registration_dtls c "
						+ "    ON c.institute_id = a.institute_id "
						+ "    LEFT JOIN tbl_curriculum_development e "
						+ "    ON e.id = a.curriculum_id "
						+ "  LEFT JOIN tbl_user d "
						+ "    ON c.registration_no = d.user_id "
						+ "WHERE a.status_id = 57 "
						+ "  AND d.user_id = ?", nativeQuery = true)
		List<Tuple> getNonAccreditedApprovedCourseByUserId(String user_id);
		
	
	

}
