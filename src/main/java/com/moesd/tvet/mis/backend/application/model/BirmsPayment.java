package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tbl_birms_payment_details")
public class BirmsPayment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String platform;
	private String refNo;
	private String applicationNo;
	private String taxPayerNo;
	private String taxPayerName;
	private String agencyCode;
	private String payerEmail;
	private String mobileNo;
	private String totalPayableAmount;
	private String serviceCode;
	private String description;
	private String paymentAdviceNo;
	private String taxPayerDocumentNo;
	private String paymentRequestDate;
	private String redirectUrl;
	private String paymentDueDate;
	private String paymentStatus;
	private Integer instituteId;
	private Date updatedAt;
	private Date createdAt;
	private String journalNo;
	private String cancelledReason;
	private String remarks;
	private String issuingBank;
	private String receiptStatus;
	private Date cancelledDate;
	private String cancelledBy;
	private String paymentMode;
	private String receiptNo;
	
}
