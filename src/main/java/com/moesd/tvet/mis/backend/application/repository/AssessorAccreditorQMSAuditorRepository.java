package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.AssessorAccreditorQMSAuditor;
import jakarta.persistence.Tuple;


public interface AssessorAccreditorQMSAuditorRepository extends JpaRepository<AssessorAccreditorQMSAuditor, Long>{
	@Query(value =  
			"SELECT "
					+ "  a.id, "
					+ "  a.application_no, "
					+ "  a.reference_no, "
					+ "  a.citizen_id, "
					+ "  a.full_name, "
					+ "  a.gender_id, "
					+ "  a.date_of_birth, "
					+ "  a.mobile_no, "
					+ "  a.email, "
					+ "  a.dzongkhag_id, "
					+ "  a.organization_name, "
					+ "  a.sector_id, "
					+ "  a.sector_name, "
					+ "  a.occupation_id, "
					+ "  a.occupation_name, "
					+ "  a.certification_level_id, "
					+ "  a.certification_level_name, "
					+ "  a.designation, "
					+ "  a.years_of_experience, "
					+ "  a.responsibility, "
					+ "  a.qms_training, "
					+ "  a.academic_background, "
					+ "  a.status_id, "
					+ "  a.created_at, "
					+ "  a.updated_at, "
					+ "  a.created_by, "
					+ "  a.updated_by, "
					+ "  a.remarks, "
					+ "  t.task_status_id, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', w.id, "
					+ "        'organizationName', w.organization_name, "
					+ "        'designation', w.designation, "
					+ "        'year', w.year, "
					+ "        'responsibility', w.responsibility, "
					+ "        'createdAt', w.created_at, "
					+ "        'updatedAt', w.updated_at "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_work_experience w "
					+ "  WHERE w.application_no = a.application_no) AS work_experiences, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', d.id, "
					+ "        'documentName', d.document_name, "
					+ "        'url', d.upload_url "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_document_master d "
					+ "  WHERE d.application_no = a.application_no) AS documents "
					+ "FROM "
					+ "  tbl_assessor_accreditor_qmsauditor_registration a "
					+ "  LEFT JOIN tbl_task_dtls t "
					+ "    ON a.application_no = t.application_no "
					+ "WHERE a.application_no = ?", nativeQuery = true)
		List<Tuple> getApplicationDetailByApplicationNo(String application_no);
	
		Optional<AssessorAccreditorQMSAuditor> findByApplicationNo(String applicationNo);
}
