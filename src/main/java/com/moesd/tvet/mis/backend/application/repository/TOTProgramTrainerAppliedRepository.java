package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import jakarta.persistence.Tuple;
import com.moesd.tvet.mis.backend.application.model.TOTProgramTrainerApplied;


public interface TOTProgramTrainerAppliedRepository extends JpaRepository<TOTProgramTrainerApplied, Long>{

	List<TOTProgramTrainerApplied> findByApplicationNo(String applicationNo);

	List<TOTProgramTrainerApplied> findByInstituteId(Integer instituteId);

	boolean existsByApplicationNoAndInstituteId(String applicationNo, Integer instituteId);


	/** Trainers with an issued mark sheet for an application (optionally one institute). */
	@NativeQuery("SELECT c.id AS applied_id, c.trainer_id, c.institute_id, d.name, "
			+ "  COALESCE(NULLIF(d.citizen_id, ''), d.work_permit_no) AS cid, "
			+ "  f.proposed_institute_name AS institute_name, "
			+ "  c.overall_percentage, c.grade, c.result, c.certificate_no, c.certificate_generated_at "
			+ "FROM tbl_tot_program_trainer_applied_dtls c "
			+ "  LEFT JOIN tbl_trainer_dtls d ON d.id = c.trainer_id "
			+ "  LEFT JOIN tbl_institute_registration_dtls f ON f.institute_id = c.institute_id "
			+ "WHERE c.application_no = ?1 AND c.certificate_no IS NOT NULL "
			+ "  AND (?2 IS NULL OR c.institute_id = ?2) "
			+ "ORDER BY d.name")
	List<Tuple> getCertifiedTrainers(String applicationNo, Integer instituteId);

	/** TOT programmes that have at least one issued mark sheet (optionally one institute). */
	@NativeQuery("SELECT a.application_no, b.program_name, b.program_code, a.venue, "
			+ "  a.program_start_date, a.program_end_date, COUNT(c.id) AS certified "
			+ "FROM tbl_tot_announcement_dtls a "
			+ "  JOIN tbl_tot_program_dtls b ON b.id = a.program_id "
			+ "  JOIN tbl_tot_program_trainer_applied_dtls c "
			+ "    ON c.application_no = a.application_no AND c.certificate_no IS NOT NULL "
			+ "WHERE (?1 IS NULL OR c.institute_id = ?1) "
			+ "GROUP BY a.application_no, b.program_name, b.program_code, a.venue, "
			+ "  a.program_start_date, a.program_end_date "
			+ "ORDER BY a.program_start_date DESC")
	List<Tuple> getCertifiedPrograms(Integer instituteId);

}
