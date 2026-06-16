package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssessorAccreditorQMSAuditordto {
	private Long id;
	private String applicationNo;
	private String referenceNo;
	private String citizenId;
	private String dateOfBirth;
	private String fullName;
	private String genderId;
	private String genderName;
	private String mobileNo;
	private String email;
	private String dzongkhagId;
	private String organizationName;
	private Long sectorId;
	private String sectorName;
	private Long occupationId;
	private String occupationName;
	private Long certificationLevelId;
	private String certificationLevelName;
	private String designation;
	private Integer yearsOfExperience;
	private String responsibility;
	private String qmsTraining;
	private String academicBackground;
	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime updatedAt;
	private String updatedBy;

	// List of work experiences
	private List<WorkExperiencedto> workExperiences;

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
