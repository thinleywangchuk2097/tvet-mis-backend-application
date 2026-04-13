package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstituteNonAccreditedCoursedto {
	
	private String instituteId;
	private String courseTitle;
	private String theoryHour;
	private String practicalHour;
	private String ojtHour;
	private String feesPerTrainee;
	private String enrolmentCapacity;
	private String certificateLevelId;
	private String curriculumTypeId;
	private Date registrationDate;
	private Date validityDate;
	private String createdBy;
	private Integer updatedBy;
	private String ApplicationNo;
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
