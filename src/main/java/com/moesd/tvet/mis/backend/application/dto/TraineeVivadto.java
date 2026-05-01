package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TraineeVivadto {
	private Long traineeId;
	private Integer vivaAssessment;
	private Integer practicalAssessment;
}
