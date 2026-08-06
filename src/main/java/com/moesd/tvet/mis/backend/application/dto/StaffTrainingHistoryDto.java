package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffTrainingHistoryDto {
	private Long id;
	private String trainingName;
	private LocalDate trainingStart;
	private LocalDate trainingEnd;
	private String providerName;
	private LocalDate resignationDate;
	private Integer fundingSourceId;
	private String trainingCost;
}
