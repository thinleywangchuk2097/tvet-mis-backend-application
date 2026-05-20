package com.moesd.tvet.mis.backend.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.BirmsPaymentRequestdto;
import com.moesd.tvet.mis.backend.application.model.BirmsPayment;
import com.moesd.tvet.mis.backend.application.service.BirmsPaymentService;
import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/auth/birms/payment")
public class BirmsPaymentController {

	private final BirmsPaymentService birmsPaymentService;

	@PostMapping("/create-payment-advice-no")
	public String createPaymentAdvice(@RequestBody BirmsPaymentRequestdto paymentRequest) {
		return birmsPaymentService.createPaymentAdvice(paymentRequest);
	}

	@PostMapping("/update-payment-status")
	public ResponseEntity<?> updatePaymentStatus(@RequestBody BirmsPaymentRequestdto request) {

		BirmsPayment payment = birmsPaymentService.updatePaymentStatus(request);

		if (payment != null) {
			Map<String, String> successResponse = new HashMap<>();
			successResponse.put("statusCode", "200");
			successResponse.put("statusDescription", "Payment Details received successfully");
			return ResponseEntity.ok(successResponse);
		}

		Map<String, Object> error = new HashMap<>();
		error.put("status", 404);
		error.put("message", "Data not found with paymentAdviceNo: " + request.getPaymentAdviceNo());
		error.put("timestamp", new Date());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@GetMapping("/get-payment-receipts/{receiptNo}")
	public ResponseEntity<String> getPaymentReceiptDetails(@PathVariable String receiptNo) {
		return birmsPaymentService.getPaymentReceiptDetails(receiptNo);
	}

	@PostMapping("/make-payment-cancel")
	public ResponseEntity<?> makePaymentCancel(@RequestBody BirmsPaymentRequestdto dto) {
		return birmsPaymentService.makePaymentCancel(dto);
	}

	@GetMapping("/is-application-exist/{application_no}")
	public Optional<BirmsPayment> checkDataExist(@PathVariable String application_no) {
		return birmsPaymentService.checkDataExist(application_no);
	}

	@PostMapping("/check-bounce")
	public ResponseEntity<?> updatePaymentCheckBounce(@RequestBody BirmsPaymentRequestdto dto) {

		BirmsPayment payment = birmsPaymentService.updatePaymentCheckBounce(dto);

		if (payment != null) {
			Map<String, String> successResponse = new HashMap<>();
			successResponse.put("statusCode", "200");
			successResponse.put("statusDescription", "Payment Details received successfully");
			return ResponseEntity.ok(successResponse);
		}

		Map<String, Object> error = new HashMap<>();
		error.put("status", 404);
		error.put("message", "Data not found with receiptNo: " + dto.getReceiptNo());
		error.put("timestamp", new Date());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@GetMapping("/get-penalty-application-details")
	public ResponseEntity<List<ObjectNode>> getAllPenaltyApplicationDetails() {
		return ResponseEntity.ok(birmsPaymentService.getAllPenaltyApplicationDetails());
	}

	@GetMapping("/get-user-penalty-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getByUserPenaltyApplicationDetails(@PathVariable String user_id) {
		return ResponseEntity.ok(birmsPaymentService.getByUserPenaltyApplicationDetails(user_id));
	}
}