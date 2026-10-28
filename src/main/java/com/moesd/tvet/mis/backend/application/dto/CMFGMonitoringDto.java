package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Institute CMFG monitoring self-assessment (checklist = quality standards of service 55). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CMFGMonitoringDto {
	private String applicationNo;
	private String instituteId;
	private String applicantName;
	private String createdBy;
	private List<QualityStandardsdto> qualityStandards;

	// System / workflow fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String assignedUserId;
	private String userId;
	private Integer statusId;
	private String remarks;
	private Documentdto[] documents;
}
