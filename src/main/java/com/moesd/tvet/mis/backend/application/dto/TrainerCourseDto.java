package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCourseDto {
	private Long id;
	private Integer courseId;
	private Integer trainerId;
	private Integer courseTypeId;
	
}
