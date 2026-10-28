package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import com.moesd.tvet.mis.backend.application.model.TOTAnnouncement;

import jakarta.persistence.Tuple;

public interface TOTAnnouncementRepository extends JpaRepository<TOTAnnouncement, Long> {
	
	Optional<TOTAnnouncement> findById(Long id);

	Optional<TOTAnnouncement> findFirstByApplicationNo(String applicationNo);
	
	@NativeQuery( 
			"SELECT "
					+ "  a.*, "
					+ "  c.id AS applied_id, "
					+ "  c.trainer_id, "
					+ "  c.institute_id AS applied_institute_id, "
					+ "  c.is_selected, "
					+ "  c.overall_percentage, "
					+ "  c.grade, "
					+ "  c.result, "
					+ "  c.certificate_no, "
					+ "  c.certificate_generated_at, "
					+ "  COALESCE( "
					+ "    NULLIF(d.citizen_id, ''), "
					+ "    d.work_permit_no "
					+ "  ) AS cid_no, "
					+ "  d.name, "
					+ "  d.email, "
					+ "  d.mobile_no, "
					+ "  e.name AS gender, "
					+ "  f.proposed_institute_name AS institute_name, "
					+ "  g.name AS qualification, "
					+ "  b.program_name, "
					+ "  b.program_code, "
					+ "  b.program_type_id, "
					+ "  (SELECT "
					+ "    IFNULL( "
					+ "      JSON_ARRAYAGG( "
					+ "        JSON_OBJECT( "
					+ "          'id', "
					+ "          opt.id, "
					+ "          'description', "
					+ "          opt.description, "
					+ "          'duration', "
					+ "          opt.duration, "
					+ "          'moduleCode', "
					+ "          opt.module_code, "
					+ "          'moduleName', "
					+ "          opt.module_name, "
					+ "          'moduleOrder', "
					+ "          opt.module_order, "
					+ "          'weightage', "
					+ "          opt.weightage, "
					+ "          'contents', "
					+ "          (SELECT "
					+ "            IFNULL( "
					+ "              JSON_ARRAYAGG( "
					+ "                JSON_OBJECT( "
					+ "                  'id', "
					+ "                  cct.id, "
					+ "                  'courseContent', "
					+ "                  cct.course_content, "
					+ "                  'fullMark', "
					+ "                  cct.full_mark "
					+ "                ) "
					+ "              ), "
					+ "              JSON_ARRAY() "
					+ "            ) "
					+ "          FROM "
					+ "            tbl_tot_course_content_dtls cct "
					+ "          WHERE cct.module_id = opt.id) "
					+ "        ) "
					+ "      ), "
					+ "      JSON_ARRAY() "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_tot_module_dtls opt "
					+ "  WHERE opt.program_id = b.id) AS modules "
					+ "FROM "
					+ "  tbl_tot_announcement_dtls a "
					+ "  LEFT JOIN tbl_tot_program_dtls b "
					+ "    ON a.program_id = b.id "
					+ "  LEFT JOIN tbl_tot_program_trainer_applied_dtls c "
					+ "    ON a.id = c.program_announcement_id "
					+ "  LEFT JOIN tbl_trainer_dtls d "
					+ "    ON c.trainer_id = d.id "
					+ "  LEFT JOIN tbl_dropdown_child_master e "
					+ "    ON e.id = d.gender_id "
					+ "  LEFT JOIN tbl_institute_registration_dtls f "
					+ "    ON f.institute_id = c.institute_id "
					+ "  LEFT JOIN tbl_dropdown_child_master g "
					+ "    ON g.id = d.qualification_id "
					+ "WHERE a.application_no = ? "
					+ "ORDER BY d.name")
	List<Tuple> getTrainerApplied(String applicationNo);
	
	
}
