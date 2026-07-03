package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.CurriculumDevelopment;
import jakarta.persistence.Tuple;


public interface CurriculumDevelopmentRepository extends JpaRepository<CurriculumDevelopment, Long> {
	@Query(value =  
			"SELECT "
					+ "  c.id, "
					+ "  c.application_no, "
					+ "  c.curriculum_name, "
					+ "  c.curriculum_type_id, "
					+ "  c.description, "
					+ "  c.certificate_level_id, "
					+ "  c.course_type_id, "
					+ "  c.entry_requirement, "
					+ "  c.ncs_id, "
					+ "  c.total_ojt_duration, "
					+ "  c.total_practical_duration, "
					+ "  c.total_program_duration, "
					+ "  c.total_theory_duration, "
					+ "  c.institute_id, "
					+ "  c.status_id, "
					+ "  c.created_by, "
					+ "  c.created_at, "
					+ "  c.updated_by, "
					+ "  c.updated_at, "
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
					+ "  WHERE d.application_no = c.application_no) AS documents "
					+ "FROM "
					+ "  tbl_curriculum_development c "
					+ "  LEFT JOIN tbl_task_dtls t "
					+ "    ON c.application_no = t.application_no "
					+ "  LEFT JOIN tbl_institute_registration_dtls k "
					+ "    ON k.institute_id = c.institute_id "
					+ "WHERE c.application_no = ?", nativeQuery = true)
		List<Tuple> getCurriculumDetails(String application_no);
	
		@Query(value =  
				"SELECT "
						+ "  c.id, "
						+ "  c.application_no, "
						+ "  c.curriculum_name, "
						+ "  c.curriculum_type_id, "
						+ "  c.description, "
						+ "  c.certificate_level_id, "
						+ "  c.course_type_id, "
						+ "  c.entry_requirement, "
						+ "  c.ncs_id, "
						+ "  c.total_ojt_duration, "
						+ "  c.total_practical_duration, "
						+ "  c.total_program_duration, "
						+ "  c.total_theory_duration, "
						+ "  c.institute_id, "
						+ "  c.status_id, "
						+ "  c.created_by, "
						+ "  c.created_at, "
						+ "  c.updated_by, "
						+ "  c.updated_at, "
						+ "  k.proposed_institute_name, "
						+ "  k.registration_no, "
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
						+ "  WHERE d.application_no = c.application_no) AS documents "
						+ "FROM "
						+ "  tbl_curriculum_development c "
						+ "  LEFT JOIN tbl_institute_registration_dtls k "
						+ "    ON k.institute_id = c.institute_id "
						+ "  LEFT JOIN tbl_user u "
						+ "    ON k.registration_no = u.user_id "
						+ "WHERE u.user_id =?", nativeQuery = true)
		List<Tuple> getCurriculumDetailsByUserId(String user_id);
		
		@Query(value = 
			    "SELECT a.* " +
			    "FROM tbl_curriculum_development a " +
			    "INNER JOIN tbl_institute_registration_dtls b ON a.institute_id = b.institute_id " +
			    "INNER JOIN tbl_user c ON c.user_id = b.registration_no " +
			    "WHERE a.status_id = 57 " +
			    "AND c.user_id = ? " +
			    "AND a.curriculum_type_id = ?",
			    nativeQuery = true)
		List<Tuple> getApprovedCurriculumDataByUserId(String user_id, String curriculum_type);
		
		//Optional<CurriculumDevelopment> findByApplicationNo(String applicationNo);
		
		CurriculumDevelopment findByApplicationNo(String applicationNo);
}
