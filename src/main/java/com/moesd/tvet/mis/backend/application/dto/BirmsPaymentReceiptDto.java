package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BirmsPaymentReceiptDto {
	private String receiptNo;
	private String receiptDate;
	private String totalReceiptAmount;
	private String paymentAdviceAmount;
	private String paymentAdviceAmountPaid;
	private String balanceAmount;
	private String penaltyAmount;
	private String penalityDescription;
	private String paymentAdviceStatus;
}
