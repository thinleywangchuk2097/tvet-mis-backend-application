package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.BirmsPayment;

import jakarta.persistence.Tuple;

public interface BirmsPaymentRepository extends JpaRepository<BirmsPayment, Long> {

    Optional<BirmsPayment> findByPaymentAdviceNo(String paymentAdviceNo);

    Optional<BirmsPayment> findByApplicationNo(String applicationNo);

    Optional<BirmsPayment> findByReceiptNo(String receiptNo);
    
    @NativeQuery("SELECT "
    				 + "  a.* "
    				 + "FROM "
    				 + "  tbl_birms_payment_details a")
	List<Tuple> getAllPaymentDetails();

    // Payments of one institute only (institute payment page)
    @NativeQuery("SELECT "
    				 + "  a.* "
    				 + "FROM "
    				 + "  tbl_birms_payment_details a "
    				 + "WHERE a.institute_id = ? "
    				 + "ORDER BY a.created_at DESC")
	List<Tuple> getPaymentDetailsByInstituteId(Integer instituteId);

    // Institute payment page search: the institute's own payments (institute_id),
    // optionally matching an Application No / Payment Advice No.
    @NativeQuery("SELECT "
    				 + "  a.* "
    				 + "FROM "
    				 + "  tbl_birms_payment_details a "
    				 + "WHERE a.institute_id = ?1 "
    				 + "  AND (?2 IS NULL OR ?2 = '' "
    				 + "       OR a.application_no LIKE CONCAT('%', ?2, '%') "
    				 + "       OR a.payment_advice_no LIKE CONCAT('%', ?2, '%')) "
    				 + "ORDER BY a.created_at DESC")
	List<Tuple> searchInstitutePayments(Integer instituteId, String query);
    
    @NativeQuery("SELECT "
    				+ "  c.id AS course_id, "
    				+ "  c.programme_title AS course_name "
    				+ "FROM "
    				+ "  tbl_accredited_course_dtls a "
    				+ "  LEFT JOIN tbl_curriculum_development b "
    				+ "    ON a.curriculum_id = b.id "
    				+ "  LEFT JOIN tbl_ncs_app_dtls c "
    				+ "    ON c.id = b.programme_id "
    				+ "WHERE a.institute_id = ?")
	List<Tuple> getCourseByInstituteId(String instituteId);
    
    
}