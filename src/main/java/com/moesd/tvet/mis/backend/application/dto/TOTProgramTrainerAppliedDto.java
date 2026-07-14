package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOTProgramTrainerAppliedDto {
	private String applicationNo;
	private Integer instituteId;
	private Integer trainerId;
	private Integer programAnnouncementId;
	private Integer createdBy;
	//System fields
 	private Integer serviceId; 
 	private Integer assignedRoleId;
 	private String userId; 
 	private Integer statusId; 
 	private String remarks;
}
