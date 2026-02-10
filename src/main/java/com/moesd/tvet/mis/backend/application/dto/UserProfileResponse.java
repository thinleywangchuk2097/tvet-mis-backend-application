package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
	private String userId;
	private String firstName;
	private String middleName;
	private String lastName;
	private String mobileNo;
	private String emailId;
	private String profileImageUrl;
}
