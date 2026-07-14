package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_ojt_company_agreement_dtls")
public class OJTCompanyAgreement {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String agreementTitle;
	
	private Date agreementDate;
	
	private Date startDate;
	
	private Date endDate;
	
	private String totalTraineeNo;
	
	private String superVisorName;
	
	private String supervisorContactNo;
	
	private String description;
	
	private Integer instituteId;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "company_id")
	private OJTCompany ojtcompany;
}
