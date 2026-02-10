package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Complaintdto {
private String applicantName;
	
	private String applicationNo;
	
	private String emaildId;
	
	private String mobileNo;
	
	private String complaintType;

	private String priorityLevel;

	private String description;
	
	private Documentdto[] documents;
	
	private Integer serviceId;
	
	private Integer currentRoleId;
	
	private Integer locationId;
	
	private Integer statusId;
	
	private Integer userId;
	
	private String remarks;

	public Documentdto[] getDocuments() {
		return documents;
	}

	public void setDocuments(Documentdto[] documents) {
		this.documents = documents;
	}
}
