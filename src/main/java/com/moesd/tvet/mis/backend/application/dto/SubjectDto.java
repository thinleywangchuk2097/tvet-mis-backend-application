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

	private String applicationNo;

	private String subjectName;

	private Integer classLevelId;

	private Integer totalDurationHours;

	private Integer instituteId;

	private Integer statusId;

	private String description;
	
	private Integer createdBy;
	
	private Integer updatedBy;
}
