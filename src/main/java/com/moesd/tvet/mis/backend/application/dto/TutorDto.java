package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDto {
	private Long id;

	private String citizenId;

	private String name;

	private Integer genderId;

	private Integer qualificationId;

	/** ids of approved subjects (multi-select) */
	private List<Long> subjectIds;

	private String email;

	private String mobileNo;

	private Integer instituteId;

	private Integer statusId;

	private Integer createdBy;

	private Integer updatedBy;
}
