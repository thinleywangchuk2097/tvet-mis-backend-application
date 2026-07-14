package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OJTTraineeDto {
	private Long id;

	private String traineeCid;

	private String traineeName;

	private String courseId;

	private String position;

	private String salary;

	private String remarks;

	private Integer employmentStatusId;
	
    private Long ojtAgreementId;
    
    private Integer instituteId;
    
	private Integer createdBy;
    
	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
}
