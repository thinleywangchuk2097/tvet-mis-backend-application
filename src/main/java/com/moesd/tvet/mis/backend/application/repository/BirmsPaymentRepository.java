package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moesd.tvet.mis.backend.application.model.BirmsPayment;

public interface BirmsPaymentRepository extends JpaRepository<BirmsPayment, Long> {

    Optional<BirmsPayment> findByPaymentAdviceNo(String paymentAdviceNo);

    Optional<BirmsPayment> findByApplicationNo(String applicationNo);

    Optional<BirmsPayment> findByReceiptNo(String receiptNo);
}