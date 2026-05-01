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
public class CourseEnrollmentTraineeAppdto {
	private String applicationNo;
	private String cidNo;
	private String referenceNo;
	private String name;
	private Date dob;
	private Integer genderId;
	private String email;
	private String mobileNo;
	private Integer traineeTypeId;
	private Integer employmentStatusId;
	private Integer academicQualificationId;
	private Integer presentDzongkhagId;
	private Integer presentGewogId;
	private Integer parentOccupationId;
	private Integer parentMaritalStatusId;
	private Integer statusId;
	private Integer serviceId;
	private String remarks;
	private Integer createdBy;
	private Integer updatedBy;

	private Documentdto[] documents;
}
