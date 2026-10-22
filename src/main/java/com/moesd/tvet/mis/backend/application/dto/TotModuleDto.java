package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotModuleDto {
	
	private String moduleName;

	private String moduleCode;
	
	private String duration;
	
	private String description;

	private String prerequisites;

	private String learningOutcomes;
	
	private String weightage;
	
	private Integer order;
	
	private List<TotCourseContentDto> courseContents;
	
}
