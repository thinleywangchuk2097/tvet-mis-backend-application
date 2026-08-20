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
	private String curriculumTitle;
	private String curriculumTypeId;
	private Long programmeTypeId;
	private Integer sectorId;
	private Integer occupationId;
	private String programmeTitle;
	private Long programmeId;
	private Integer ncsId;
	private String description;
	private String instituteId;
	private Integer certificateLevelId;
	private String entryRequirement;
	private String totalTheoryDuration;
	private String totalPracticalDuration;
	private String totalOjtDuration;
	private String totalProgramDuration;
	
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
