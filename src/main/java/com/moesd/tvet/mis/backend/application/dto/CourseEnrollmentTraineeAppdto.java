package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import java.util.List;

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
	private String guardianName;
	private String guardianMobileNo;
	private Integer guardianMaritalStatusId;
	private Integer guardianOccupationId;
	private Integer statusId;
	private Integer serviceId;
	private Integer createdBy;
	private Integer updatedBy;
	private String examYear;
	private String schoolName;
	private String stream;
	
	private Long parentFailedId;

	// Gyalsung (National Service) details from GyalsungApiService, as received
	private String completed; // "Yes" / "No"
	private String year; // e.g. "2026"
	private String academy;
	private String trainingStartDate; // yyyy-MM-dd (or null)
	private String trainingEndDate; // yyyy-MM-dd (or null)
	
	private List<TraineeAppDto>traineeMarks;

	private Documentdto[] documents;
}
