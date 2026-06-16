package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringAssessmentDto {
	private Long id;

	private String applicationNo;

	private Integer dzongkhagId;
	
	private Integer instituteId;

	private String exactLocation;

	private String instituteName;

	private LocalDate monitoringDate;

	private Long registrationNo;

	private Integer createdBy;
	
	private Integer updatedBy;
	
    private String description;
	
	private List<MonitoringAssessmentChecklistDto> qualityStandards;
	// System fields
	private Integer serviceId; 
 	private Integer assignedRoleId;
 	private String assignedUserId; 
 	private String userId; 
 	private Integer statusId; 
 	private String remarks;

}
