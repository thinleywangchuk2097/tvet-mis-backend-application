package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnCampusJobPlacementSessionDto {
	private String sessionName;
	private String sessionDate;
	private LocalTime sessionTime;
	private String venue;
	private String description;
	private String instituteId;
	private Integer createdBy;
	private Integer updatedBy;
	
	private Documentdto[] documents;

}
