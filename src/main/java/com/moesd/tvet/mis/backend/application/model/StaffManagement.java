package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "tbl_staff_management_dtls")
public class StaffManagement {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String hasCitizenId;
	private String citizenId;
	private String name;
	private String email;
	private String mobileNo;
	private String referenceNo;
	private Integer genderId;
	private String dob;
	private Integer instituteId;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
	private Integer statusId;
	
	@OneToMany(mappedBy = "Staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffTrainingHistory> stafftraininghistory;
	
	@OneToMany(mappedBy = "Staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffEmploymentHistory> Staffemploymenthistory;

}
