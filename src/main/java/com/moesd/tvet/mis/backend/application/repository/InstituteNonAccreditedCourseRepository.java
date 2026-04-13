package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.InstituteNonAccreditedCourse;
import jakarta.persistence.Tuple;

public interface InstituteNonAccreditedCourseRepository extends JpaRepository<InstituteNonAccreditedCourse,Long>{
	
	@Query(value = 
		    "SELECT "
		        + "  n.id, "
		        + "  n.application_no, "
		        + "  n.course_title, "
		        + "  n.theory_hour, "
		        + "  n.practical_hour, "
		        + "  n.ojt_hour, "
		        + "  n.fees_per_trainee, "
		        + "  n.enrolment_capacity, "
		        + "  n.certificate_level_id, "
		        + "  n.curriculum_type_id, "
		        + "  n.status_id, "
		        + "  n.registration_date, "
		        + "  n.validity_date, "
		        + "  n.created_by, "
		        + "  n.created_at, "
		        + "  n.updated_by, "
		        + "  n.updated_at, "
		        + "  k.proposed_institute_name, "
		        + "  k.registration_no, "
		        + "  t.task_status_id, "
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
		        + "  WHERE d.application_no = n.application_no) AS documents "
		        + "FROM "
		        + "  tbl_non_accredited_course_dtls n "
		        + "  LEFT JOIN tbl_task_dtls t "
		        + "    ON n.application_no = t.application_no "
		        + "  LEFT JOIN tbl_institute_registration_dtls k "
		        + "    ON k.institute_id = n.institute_id "
		        + "WHERE n.application_no = ?1", nativeQuery = true)
		List<Tuple> getNonAccreditedCourseByApplicationNo(String application_no);
	
		Optional<InstituteNonAccreditedCourse> findByApplicationNo(String applicationNo);
		
		@Query(value =  
				"SELECT "
						+ "  a.* "
						+ "FROM "
						+ "  tbl_non_accredited_course_dtls a "
						+ "  LEFT JOIN tbl_institute_registration_dtls b "
						+ "    ON a.institute_id = b.institute_id "
						+ "  LEFT JOIN tbl_user c "
						+ "    ON c.user_id = b.registration_no "
						+ "WHERE c.user_id=?", nativeQuery = true)
		List<Tuple> getNonAccreditedCourseDetailsByUserId(String user_id);
	
	

}
