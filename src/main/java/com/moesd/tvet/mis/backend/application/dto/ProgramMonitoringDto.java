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
public class ProgramMonitoringDto {
	private Long id;

	private String applicationNo;

	private Integer dzongkhagId;
	
	private Integer instituteId;
	
	private Integer courseTypeId;
	
	private Integer courseId;
	
	private String exactLocation;

	private String instituteName;

	private LocalDate monitoringDate;

	private Long registrationNo;

	private Integer createdBy;
	
	private Integer updatedBy;
	
    private String description;
    // Documents
 	private Documentdto[] documents;
 	
	private List<ProgramMonitoringChecklistDto> qualityStandards;
	// System fields
	private Integer actionId;
	private Integer serviceId; 
 	private Integer assignedRoleId;
 	private String assignedUserId; 
 	private String userId; 
 	private Integer statusId; 
 	private String remarks;
}
