package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramMonitoringChecklistDto {
	
	private Long id;
	private Long standardId;
	private String responseId;
	private String remarks;
}
