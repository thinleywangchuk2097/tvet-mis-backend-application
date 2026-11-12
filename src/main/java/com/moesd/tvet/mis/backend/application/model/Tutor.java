package com.moesd.tvet.mis.backend.application.model;

import java.util.ArrayList;
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
@Table(name = "tbl_tutor_dtls")
public class Tutor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String citizenId;
	
	private String name;
	
	private Integer genderId;
	
	/** Qualification Level: Academic Qualification (dropdown child of parent 18) */
	private Integer qualificationId;
	
	private String email;
	
	private String mobileNo;
	
	private Integer instituteId;
	
	private Integer statusId;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	// Relationships - Using mappedBy to indicate the child owns the relationship
	@Builder.Default
	@OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TutorSubject> subjects = new ArrayList<>();
}
