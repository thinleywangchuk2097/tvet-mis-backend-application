package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseEnrollmentAppdto {

	private String instituteId;
	private String courseId;
	private String courseFee;
	private String totalNoTrainees;
	private Date applicationStartDate;
	private Date applicationEndDate;
	private Date courseStartDate;
	private Date courseEndDate;
	private Date caStartDate;
	private Date caEndDate;
	private Integer certificationLevelId;
	private Integer fundingSourceId;
	private Integer trainingLocationId;
	private String courseDescription;
	private Integer createdBy;
	private Integer updatedBy;

	// System fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String userId;
	private Integer statusId;
	private String remarks;
	// Documents
	private Documentdto[] documents;
}
