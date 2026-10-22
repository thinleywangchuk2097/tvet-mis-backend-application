package com.moesd.tvet.mis.backend.application.dto;

import lombok.Data;

@Data
public class PublicCitizenDetailsResponse {
	private String cid;
	private String firstName;
	private String middleName;
	private String lastName;
	private String firstDzoName;
	private String middleDzoName;
	private String lastDzoName;
	private String dob;
	private String gender;
}
