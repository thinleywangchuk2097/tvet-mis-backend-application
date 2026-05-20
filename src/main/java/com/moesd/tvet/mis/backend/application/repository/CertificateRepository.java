package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationDetails;

import jakarta.persistence.Tuple;

public interface CertificateRepository extends JpaRepository<InstituteRegistrationDetails, Long> {

	@Query(value = "SELECT * FROM tbl_institute_registration_dtls WHERE status_id = 57", nativeQuery = true)
	List<Tuple> getAssessmentInstitute();

	@Query(value = "SELECT " + "  b.id, " + "  b.occupation_name course_name " + "FROM "
			+ "  tbl_accredited_course_dtls a " + "  LEFT JOIN tbl_occupation_master b "
			+ "    ON (a.course_id = b.id) " + "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getAssessmentCourse(Integer sectorId);

}
