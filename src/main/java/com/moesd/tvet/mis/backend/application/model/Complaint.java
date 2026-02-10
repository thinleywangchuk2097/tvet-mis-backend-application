package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Column;
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
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tbl_complaint")
public class Complaint {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(nullable = false)
	private String applicationNo;

    @Column(nullable = false)
    private String applicantName;
    
    @Column(nullable = false)
    private String emaildId;
    
    @Column(nullable = false)
    private String mobileNo;
    
    @Column(nullable = false)
    private String complaintType;
    
    @Column(nullable = false)
    private String priorityLevel;
    
    @Column(nullable = false)
    private String description;
}
