package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
	private String userId;
    private String userName;
    private String mobileNo;
    private String emailId;
    private String profileImageBase64; // Base64 encoded string

}
