package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {
	private Long id;

	private String subjectCode;

	private String subjectName;

	private String creditHours;

	private String theoryHours;

	private Integer instituteId;

	private String practicalHours;

	private Integer statusId;

	private String description;
	
	private Integer createdBy;
	
	private Integer updatedBy;
}
