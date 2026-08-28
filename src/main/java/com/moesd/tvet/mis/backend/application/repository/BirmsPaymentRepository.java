package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.BirmsPayment;

import jakarta.persistence.Tuple;

public interface BirmsPaymentRepository extends JpaRepository<BirmsPayment, Long> {

    Optional<BirmsPayment> findByPaymentAdviceNo(String paymentAdviceNo);

    Optional<BirmsPayment> findByApplicationNo(String applicationNo);

    Optional<BirmsPayment> findByReceiptNo(String receiptNo);
    
    @Query(value =  
    		 "SELECT "
    				 + "  a.* "
    				 + "FROM "
    				 + "  tbl_birms_payment_details a", nativeQuery = true)
	List<Tuple> getAllPaymentDetails();
    
    @Query(value =  
    		"SELECT "
    				+ "  c.id AS course_id, "
    				+ "  c.programme_title AS course_name "
    				+ "FROM "
    				+ "  tbl_accredited_course_dtls a "
    				+ "  LEFT JOIN tbl_curriculum_development b "
    				+ "    ON a.curriculum_id = b.id "
    				+ "  LEFT JOIN tbl_ncs_app_dtls c "
    				+ "    ON c.id = b.programme_id "
    				+ "WHERE a.institute_id = ?", nativeQuery = true)
	List<Tuple> getCourseByInstituteId(String instituteId);
    
    
}