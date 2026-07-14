package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnCampusJobPlacementTraineeDto {
	
	private String traineeCid;
	private String traineeName;
	private Integer courseId;
	private String position;
	private Integer employmentStatusId;
	private String salary;
	private String remarks;
	private Integer instituteId;
	private LocalDate placementDate;
	private LocalDate startDate;
	private Long firmId;
	private Integer createdBy;
	private Integer updatedBy;

	
	
	
}
