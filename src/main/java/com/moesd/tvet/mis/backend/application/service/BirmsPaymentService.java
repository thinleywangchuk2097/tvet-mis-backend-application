package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.BirmsPaymentRequestdto;
import com.moesd.tvet.mis.backend.application.model.BirmsPayment;

public interface BirmsPaymentService {

    String createPaymentAdvice(BirmsPaymentRequestdto paymentRequest);

    BirmsPayment updatePaymentStatus(BirmsPaymentRequestdto dto);

    ResponseEntity<String> getPaymentReceiptDetails(String receiptNo);

    ResponseEntity<?> makePaymentCancel(BirmsPaymentRequestdto dto);

    Optional<BirmsPayment> checkDataExist(String application_no);

    BirmsPayment updatePaymentCheckBounce(BirmsPaymentRequestdto dto);

    List<ObjectNode> getAllPenaltyApplicationDetails();

    List<ObjectNode> getByUserPenaltyApplicationDetails(String user_id);
}