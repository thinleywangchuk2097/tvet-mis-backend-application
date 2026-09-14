package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.OJTCompany;

import jakarta.persistence.Tuple;


public interface OJTCompanyRepository extends JpaRepository<OJTCompany, Long>{
	
	boolean existsByRegistrationNo(String registrationNo);
	
	Optional<OJTCompany> findById(Long id);
	
	@NativeQuery("SELECT "
					+ "  a.* "
					+ "FROM "
					+ "  tbl_ojt_company_dtls a "
					+ "WHERE a.institute_id = ?")
	List<Tuple> getCompanyByInstituteId(String institute_id);
	
	
	
	
}
