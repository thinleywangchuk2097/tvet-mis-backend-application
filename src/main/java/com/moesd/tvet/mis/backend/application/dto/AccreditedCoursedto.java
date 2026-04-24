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
	private String instituteId;
	private String courseId;
	private String courseFee;
	private String applicantName;
	private String is_active;
	private String sectorId;
	private Date registration_date;
	private String validity_date;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
	private List<QualityStandardsdto> qualityStandards;
    
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
