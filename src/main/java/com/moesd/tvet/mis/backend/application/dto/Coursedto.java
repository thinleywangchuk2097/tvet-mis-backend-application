package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coursedto {
	
	private String sectorId;
	private String courseId;
	private String theoryHours;
	private String practicalHours;
	private String ojtHours;
	private String feesPerTrainee;
	private String enrollmentCapacity;
	private String courseLevelId;
}
