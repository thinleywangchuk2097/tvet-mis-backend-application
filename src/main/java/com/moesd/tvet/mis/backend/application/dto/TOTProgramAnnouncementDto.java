package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOTProgramAnnouncementDto {
	
	private Long id;
	
	private String applicationNo;

	private LocalDate applicationStartDate;

	private LocalDate applicationEndDate;

	private LocalDate programStartDate;

	private LocalDate programEndDate;
	
	private Integer programTypeId;
	
    private Long programId;
    
	private String maxParticipants;

	private String venue;

	private String eligibilityCriteria;

	private String remarks;

	private Integer createdBy;

	private Integer updatedBy;

}
