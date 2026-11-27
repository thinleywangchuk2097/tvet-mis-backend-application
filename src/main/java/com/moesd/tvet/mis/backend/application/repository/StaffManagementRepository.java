package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moesd.tvet.mis.backend.application.model.StaffManagement;

import jakarta.persistence.Tuple;

public interface StaffManagementRepository extends JpaRepository<StaffManagement, Long> {

	@NativeQuery("SELECT "
					+ "  sm.id, "
					+ "  sm.has_citizen_id, "
					+ "  sm.citizen_id, "
					+ "  sm.name, "
					+ "  sm.email, "
					+ "  sm.mobile_no, "
					+ "  sm.reference_no, "
					+ "  sm.agency_name, "
					+ "  sm.field_of_study, "
					+ "  sm.position_level, "
					+ "  sm.employee_id, "
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
					+ "        seh.designation "
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
					+ "WHERE sm.institute_id = ? AND sm.status_id=1")
	List<Tuple> getInstituteStaff(String instituteId);
	
	@Query("SELECT s FROM StaffManagement s WHERE s.id = :id AND s.statusId != 0")
	Optional<StaffManagement> findByIdAndStatusIdNot(@Param("id") Long id, @Param("statusId") Integer statusId);
	
	// same CID already registered (not deleted) in this institute; excludeId = the staff being edited (-1 = none)
	@Query("SELECT COUNT(s) > 0 FROM StaffManagement s WHERE s.instituteId = :instituteId "
			+ "AND TRIM(s.citizenId) = :citizenId AND s.statusId != 0 AND s.id <> :excludeId")
	boolean existsCitizenInInstitute(@Param("instituteId") Integer instituteId, @Param("citizenId") String citizenId,
			@Param("excludeId") Long excludeId);

	// same reference no (staff without CID) already registered (not deleted) in this institute
	@Query("SELECT COUNT(s) > 0 FROM StaffManagement s WHERE s.instituteId = :instituteId "
			+ "AND TRIM(s.referenceNo) = :referenceNo AND s.statusId != 0 AND s.id <> :excludeId")
	boolean existsReferenceInInstitute(@Param("instituteId") Integer instituteId,
			@Param("referenceNo") String referenceNo, @Param("excludeId") Long excludeId);
}
