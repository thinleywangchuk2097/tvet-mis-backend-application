package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_ojt_company_dtls")
public class OJTCompany {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String registrationNo;
	
	private String companyName;
	
    private Integer instituteId;
    
	private Integer dzongkhagId;
	
	private String contactPersonName;
	
	private String contactPersonMobileNo;
	
	private String contactPersonEmail;
	
	private String address;
	
	private String description;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
	
	

}
