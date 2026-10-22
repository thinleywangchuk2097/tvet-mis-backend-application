package com.moesd.tvet.mis.backend.application.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moesd.tvet.mis.backend.application.model.BirmsPayment;
import com.moesd.tvet.mis.backend.application.service.BirmsPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/birms/payment")
public class BirmsPaymentPublicController {

	private final BirmsPaymentService birmsPaymentService;

	@GetMapping("/get-payment/{paymentAdviceNo}")
	public Optional<BirmsPayment> getPaymentByPaymentAdviceNo(@PathVariable String paymentAdviceNo) {
		return birmsPaymentService.getPaymentByPaymentAdviceNo(paymentAdviceNo);
	}
}
