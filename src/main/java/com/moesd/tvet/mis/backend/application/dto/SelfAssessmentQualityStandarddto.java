package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelfAssessmentQualityStandarddto {
	private Long standardId;
	private Integer selfPoint;
	private Integer earnedPoint;
	private String justification;
}
