package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
@Table(name = "tbl_curriculum_development_audit")
public class CurriculumDevelopmentAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no")
	private String applicationNo;

	private String curriculumTitle;

	private String curriculumTypeId;

	private Long programmeTypeId;
	
	private Long programmeId;
	
	private String description;

	private String totalTheoryDuration;

	private String totalPracticalDuration;

	private String totalOjtDuration;

	private String totalProgramDuration;

	private String instituteId;

	private Integer statusId;

	private Integer certificateLevelId;

	private String entryRequirement;

	private String createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;

	private LocalDateTime updatedAt;

	@ManyToOne
	@JoinColumn(name = "curriculum_id")
	private CurriculumDevelopment curriculum;

}
