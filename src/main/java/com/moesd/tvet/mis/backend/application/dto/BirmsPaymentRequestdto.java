package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BirmsPaymentRequestdto {
	private String platform;
	private String refNo;
	private String applicationNo;
	private String taxPayerNo;
	private String taxPayerName;
	private String agencyCode;
	private String taxPayerEmail;
	private String taxPayerMobileNo;
	private String totalPayableAmount;
	private String serviceCode;
	private String description;
	private String paymentAdviceNo;
	private String paymentStatus;
	private String redirectUrl;
	private Date updatedAt;
	private String message;
	private String taxPayerDocumentNo;
	private String paymentRequestDate;
	private String paymentDueDate;
	private String reason;
	private String cancelledBy;
	private String receiptNo;
	private String receiptStatus;
	private Date cancelledDate;
	private String cancelledReason;
	private String remarks;
	private String instrumentNo;
	private String instrumentDate;
	private String issuingBank;
	private String paymentMode;
	private String journalNo;
	private Integer currentRoleId;
	private Long userId;
	private Integer statusId;
	private String user;
	private String totalReceiptAmount;
	private List<BirmsPaymentReceiptDto> receiptList;
}
