package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
	private String userId;
	private String firstName;
	private String middleName;
	private String lastName;
	private String genderId;
	private String password;
	private Set<Integer> role;
	private String mobileNo;
	private String emailId;
	private String profilePath;
	private String statusId;
	private String locationId;
	private Integer CurrentRole;
	private Integer createdBy;
	private Date createdAt;
	private String updatedBy;
	private Date updateAt;
}
