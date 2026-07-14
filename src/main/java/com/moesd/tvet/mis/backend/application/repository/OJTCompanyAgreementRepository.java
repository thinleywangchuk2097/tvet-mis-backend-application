package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.OJTCompanyAgreement;

import jakarta.persistence.Tuple;

public interface OJTCompanyAgreementRepository extends JpaRepository<OJTCompanyAgreement, Long>{
	
	Optional<OJTCompanyAgreement> findById(Long id);
	
	boolean existsByAgreementTitle(String agreementTitle);
	
	@Query(value =  
			"SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_ojt_company_agreement_dtls a "
					+ "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getAgreementByInstituteId(String institute_id);
	
	
	
}
