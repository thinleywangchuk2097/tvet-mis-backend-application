package com.moesd.tvet.mis.backend.application.model;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Module-level result of a trainer: total marks, max marks, module weightage
 * and the weighted score = (moduleTotal / moduleMax) * weightage.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tot_trainer_module_result_dtls",
	uniqueConstraints = @UniqueConstraint(columnNames = { "application_no", "trainer_id", "module_id" }))
public class TOTTrainerModuleResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "application_no")
	private String applicationNo;
	private Long appliedId;
	@Column(name = "trainer_id")
	private Integer trainerId;
	@Column(name = "module_id")
	private Long moduleId;
	@Column(precision = 10, scale = 2)
	private BigDecimal moduleTotal;
	@Column(precision = 10, scale = 2)
	private BigDecimal moduleMax;
	@Column(precision = 10, scale = 2)
	private BigDecimal weightage;
	@Column(precision = 10, scale = 2)
	private BigDecimal weightedScore;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
}
