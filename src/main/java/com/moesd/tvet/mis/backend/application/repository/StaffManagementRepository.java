package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moesd.tvet.mis.backend.application.model.StaffManagement;

import jakarta.persistence.Tuple;

public interface StaffManagementRepository extends JpaRepository<StaffManagement, Long> {

	@Query(value =  
			"SELECT "
					+ "  sm.id, "
					+ "  sm.has_citizen_id, "
					+ "  sm.citizen_id, "
					+ "  sm.name, "
					+ "  sm.email, "
					+ "  sm.mobile_no, "
					+ "  sm.reference_no, "
					+ "  sm.gender_id, "
					+ "  sm.dob, "
					+ "  sm.institute_id, "
					+ "  sm.created_by, "
					+ "  sm.created_at, "
					+ "  sm.updated_by, "
					+ "  sm.updated_at, "
					+ "  ir.proposed_institute_name, "
					+ "  ir.registration_no, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', "
					+ "        seh.id, "
					+ "        'appointmentDate', "
					+ "        seh.appointment_date, "
					+ "        'employmentTypeId', "
					+ "        seh.employment_type_id, "
					+ "        'qualificationId', "
					+ "        seh.qualification_id, "
					+ "        'designation', "
					+ "        seh.designation, "
					+ "        'resignationDate', "
					+ "        seh.resignation_date "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_staff_employment_history_dtls seh "
					+ "  WHERE seh.staff_id = sm.id) AS staff_employment_history, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', "
					+ "        sth.id, "
					+ "        'trainingName', "
					+ "        sth.training_name, "
					+ "        'trainingStart', "
					+ "        sth.training_start, "
					+ "        'trainingEnd', "
					+ "        sth.training_end, "
					+ "        'providerName', "
					+ "        sth.provider_name, "
					+ "        'resignationDate', "
					+ "        sth.resignation_date, "
					+ "        'fundingSourceId', "
					+ "        sth.funding_source_id, "
					+ "        'trainingCost', "
					+ "        sth.training_cost "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_staff_training_history_dtls sth "
					+ "  WHERE sth.staff_id = sm.id) AS staff_training_history "
					+ "FROM "
					+ "  tbl_staff_management_dtls sm "
					+ "  LEFT JOIN tbl_institute_registration_dtls ir "
					+ "    ON sm.institute_id = ir.institute_id "
					+ "WHERE sm.institute_id = ? AND sm.status_id=1", nativeQuery = true)
	List<Tuple> getInstituteStaff(String instituteId);
	
	@Query("SELECT s FROM StaffManagement s WHERE s.id = :id AND s.statusId != 0")
	Optional<StaffManagement> findByIdAndStatusIdNot(@Param("id") Long id, @Param("statusId") Integer statusId);
	
	@Query("SELECT s FROM StaffManagement s WHERE s.citizenId = :citizenId AND s.statusId != 0")
	Optional<StaffManagement> findByCitizenIdAndStatusIdNot(@Param("citizenId") String citizenId, @Param("statusId") Integer statusId);
	
	@Query("SELECT COUNT(s) > 0 FROM StaffManagement s WHERE s.citizenId = :citizenId AND s.statusId != 0")
	boolean existsByCitizenIdAndStatusIdNot(@Param("citizenId") String citizenId, @Param("statusId") Integer statusId);    
}
