package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.AddTrainer;
import jakarta.persistence.Tuple;

public interface AddTrainerRepository extends JpaRepository<AddTrainer, Long> {
	
	@Query(value =  "SELECT "
			+ "  t.id, "
			+ "  t.citizen_id, "
			+ "  t.specialization, "
			+ "  t.work_permit_no, "
			+ "  t.name, "
			+ "  t.gender_id, "
			+ "  t.qualification_id, "
			+ "  t.work_experience, "
			+ "  t.employment_type_id, "
			+ "  t.email, "
			+ "  t.mobile_no, "
			+ "  t.institute_id, "
			+ "  t.joining_date, "
			+ "  t.status_id, "
			+ "  t.description, "
			+ "  t.created_by, "
			+ "  t.created_at, "
			+ "  t.updated_by, "
			+ "  t.updated_at, "
			+ "  (SELECT "
			+ "    IFNULL ( "
			+ "      JSON_ARRAYAGG( "
			+ "        JSON_OBJECT( "
			+ "          'id', "
			+ "          tc.id, "
			+ "          'courseId', "
			+ "          tc.course_id, "
			+ "          'courseTypeId', "
			+ "          tc.course_type_id "
			+ "        ) "
			+ "      ), "
			+ "      JSON_ARRAY () "
			+ "    ) "
			+ "  FROM "
			+ "    tbl_trainer_course_dtls tc "
			+ "  WHERE tc.trainer_id = t.id) AS courses "
			+ "FROM "
			+ "  tbl_trainer_dtls t "
			+ "WHERE t.institute_id = ? "
			+ "  AND t.status_id = 1 "
			+ "ORDER BY t.id DESC", nativeQuery = true)
	List<Tuple> getAllActiveTrainers(Integer institute_id);

	Optional<AddTrainer> findByCitizenId(String citizenId);

	Optional<AddTrainer> findByWorkPermitNo(String workPermitNo);
}
