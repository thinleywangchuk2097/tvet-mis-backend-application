package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationDetails;

import jakarta.persistence.Tuple;

public interface InstituteRegistrationDetailsRepository extends JpaRepository<InstituteRegistrationDetails, Long> {
	@Query(value = "SELECT * FROM tbl_institute_registration_dtls " + "WHERE registration_no = ?", nativeQuery = true)
	List<Tuple> getInstituteDetails(String registration_no);
}
