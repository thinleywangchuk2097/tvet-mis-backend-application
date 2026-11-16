package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollmentDto {
	private Long id;

	private String name;

	private Integer genderId;

	private Integer schoolLevelId;

	private Long subjectId;

	private Integer enrollmentYear;

	private Integer instituteId;

	private Integer createdBy;

	private Integer updatedBy;
}
