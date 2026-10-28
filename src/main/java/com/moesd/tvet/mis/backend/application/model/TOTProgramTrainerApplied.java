package com.moesd.tvet.mis.backend.application.model;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tot_program_trainer_applied_dtls")
public class TOTProgramTrainerApplied {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String applicationNo;
	private Integer instituteId;
	private Integer trainerId;
	private Integer programAnnouncementId;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
	// Selection by reviewer (1 = selected, 0 = not selected, null = not yet reviewed)
	private Integer isSelected;
	private Integer selectedBy;
	private Date selectedAt;
	// Mark sheet / certificate result
	@Column(precision = 10, scale = 2)
	private BigDecimal overallPercentage;
	private String grade;
	private String result;
	private String certificateNo;
	private Integer certificateGeneratedBy;
	private Date certificateGeneratedAt;
	
	
}
