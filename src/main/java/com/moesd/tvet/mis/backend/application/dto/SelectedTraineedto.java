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
public class SelectedTraineedto {
	private String applicationNo;
	private Integer statusId;
	private String courseId;
	private Integer certificationlevelId;
	private String courseName;
	private Date caStartDate;
	private Date caEndDate;
	private List<TraineeStatusdto> traineeIds;
	private List<TraineeMarksdto> traineeMarks;
	private List<TraineeVivadto> traineeVivaAssessments;
	private List<TraineeInternaldto> traineeInternalAssessments;
	private List<AssignedAssessorsDto>assignedAssessors;
	// System fields
	private Integer serviceId;
	private Integer assignedRoleId;
	private String assignedUserId;
	private String userId;
	private String remarks;

}
