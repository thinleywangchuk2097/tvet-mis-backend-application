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
@Table(name = "tbl_ojt_trainee_dtls")
public class OJTTraineeDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String traineeCid;

	private String traineeName;

	private String courseId;

	private String position;

	private String salary;

	private String remarks;

	private Integer employmentStatusId;

	private Integer createdBy;
	
	private Integer instituteId;
	
	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	@ManyToOne
	@JoinColumn(name = "agreement_id")
	private OJTCompanyAgreement ojtcompanyagreement;

}
