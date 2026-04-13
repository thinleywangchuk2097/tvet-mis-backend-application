package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumDevelopmentdto {

	private String applicationNo;
	private String curriculumName;
	private String curriculumTypeId;
	private String description;
	private String instituteId;
	//System fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String assignedUserId;
	private String userId;
	private Integer statusId;
	private String remarks;
	private String createdBy;
	private Integer updatedBy;

	//Documents
	private Documentdto[] documents;
}
