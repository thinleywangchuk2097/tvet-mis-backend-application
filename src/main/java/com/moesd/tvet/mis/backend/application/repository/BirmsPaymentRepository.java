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
    				+ "  a.*, "
    				+ "  b.institute_id, "
    				+ "  d.proposed_institute_name AS institute_name, "
    				+ "  e.course_id "
    				+ "FROM "
    				+ "  tbl_birms_payment_details a "
    				+ "  LEFT JOIN tbl_course_enrollment_app b "
    				+ "    ON a.application_no = b.application_no "
    				+ "  LEFT JOIN tbl_course_enrollment_trainee_app c "
    				+ "    ON c.course_enrol_app_no = b.application_no "
    				+ "  LEFT JOIN tbl_institute_registration_dtls d "
    				+ "    ON d.institute_id = b.institute_id "
    				+ "  LEFT JOIN tbl_accredited_course_dtls e "
    				+ "    ON e.id = b.course_id "
    				+ "GROUP BY c.course_enrol_app_no", nativeQuery = true)
	List<Tuple> getAllPaymentDetails();
    
    @Query(value =  
    		 "SELECT "
    				 + "  a.course_id, "
    				 + "  b.occupation_name AS course_name "
    				 + "FROM "
    				 + "  tbl_accredited_course_dtls a "
    				 + "  LEFT JOIN tbl_occupation_master b "
    				 + "  ON a.course_id = b.id "
    				 + "  WHERE a.institute_id =?", nativeQuery = true)
	List<Tuple> getCourseByInstituteId(String instituteId);
    
    
}