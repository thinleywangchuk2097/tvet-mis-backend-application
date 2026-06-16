package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
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
@Table(name = "tbl_student_dtls")
public class AddStudent {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String citizenId;
	
	private String studentCode;
	
	private String firstName;
	
	private String middleName;
	
	private String lastName;
	
	private String email;
	
	private String mobileNo;
	
	private Integer qualificationId;
	
	private Integer instituteId;
	
	private Integer genderId;
	
	private Integer statusId;
	
	private Integer dzongkhagId;
	
	private String exactLocation;
	
	private String emergencyContactName;
	
	private String emergencyContactNo;
	
	private LocalDate enrollmentDate;
	
	private String dateOfBirth;
	
	private String schoolName;
	
	private String currentClass;
	
	private String schoolExactLocation;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
	
	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentSubject> studentSubjects;
}
