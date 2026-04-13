package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkExperiencedto {
	private Long id;
	private String organizationName;
	private String designation;
	private Integer year;
	private String responsibility;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
}
