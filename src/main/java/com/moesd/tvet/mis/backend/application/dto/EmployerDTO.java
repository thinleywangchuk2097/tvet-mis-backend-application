package com.moesd.tvet.mis.backend.application.dto;

import lombok.Data;

@Data
public class EmployerDTO {
	private String id; // Employer ID (EMP001, EMP002, etc.)
	private String name; // Company/Employer name
	private String contactPerson; // Contact person name
	private String mobileNo; // Mobile number
	private String email; // Email address

}
