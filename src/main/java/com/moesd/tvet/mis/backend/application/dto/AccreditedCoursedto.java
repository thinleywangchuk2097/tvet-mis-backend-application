package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccreditedCoursedto {

	private String applicationNo;
	private Integer instituteId;
	private String feesPerTrainee;
	private String enrolmentCapacity;
	private String applicantName;
	private Date registration_date;
	private Integer curriculumId;
	private String validity_date;
	private boolean isRenewal;
	private String LeadTrainerCidNo;
	private Integer genderId;
	private String leadTrainerName;
	private String professionalExperience;
	private Integer qualificationId;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
	private List<QualityStandardsdto> qualityStandards;
	private List<AssignedRecsDto> assignedRecs;
	private List<AssignedRecsDto> assignedAccreditors;

	// System fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String assignedUserId;
	private String userId;
	private Integer statusId;
	private String remarks;
	// Documents
	private Documentdto[] documents;
}
