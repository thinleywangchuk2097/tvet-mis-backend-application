package com.moesd.tvet.mis.backend.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOTModuleMarksDto {
	private String applicationNo;
	private Integer trainerId;
	private Long moduleId;
	private Integer createdBy;
	private List<ContentMark> contents;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ContentMark {
		private Long contentId;
		private BigDecimal marks;
	}
}
