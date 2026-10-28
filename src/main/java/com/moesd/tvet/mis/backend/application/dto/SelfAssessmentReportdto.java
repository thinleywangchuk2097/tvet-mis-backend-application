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
public class SelfAssessmentReportdto {
	private String applicationNo;
	private Integer instituteId;
	private String applicantName;
	private Date submittedDate;
	private Double totalScore;
	private String grade;
	private Integer createdBy;
	private Integer updatedBy;

	// Quality standards (points per sub-indicator)
	private List<SelfAssessmentQualityStandarddto> qualityStandards;

	// ACCREDITATORS section (verification page only)
	private List<SelfAssessmentMemberdto> accreditors;
	private List<SelfAssessmentMemberdto> coordinators;

	// System / workflow fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String assignedUserId;
	private String userId;
	private Integer statusId;
	private String remarks;

	// Documents
	private Documentdto[] businessLicenseDocuments;
	private Documentdto[] trainerCvDocuments;
	private Documentdto[] documents;
}
