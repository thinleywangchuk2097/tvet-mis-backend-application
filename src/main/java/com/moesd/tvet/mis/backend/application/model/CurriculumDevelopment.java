package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
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
@Table(name = "tbl_curriculum_development")
public class CurriculumDevelopment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "application_no")
	private String applicationNo;
	
	@Column(name = "curriculum_name")
	private String curriculumName;
	
	@Column(name = "curriculum_type_id")
	private String curriculumTypeId;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "institute_id")
	private String instituteId;
	
	@Column(name = "status_id")
	private Integer statusId;
	
	private String createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;

	private LocalDateTime updatedAt;
}
