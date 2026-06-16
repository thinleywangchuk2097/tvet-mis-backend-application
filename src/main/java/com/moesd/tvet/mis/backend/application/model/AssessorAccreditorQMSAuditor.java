package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_assessor_accreditor_qmsauditor_registration")
public class AssessorAccreditorQMSAuditor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_no", nullable = false, unique = true, length = 50)
    private String applicationNo;

    // Common fields for all services
    @Column(name = "service_id")
    private Integer serviceId; 
    
    @Column(name = "registration_no", unique = true)
	private String RegistrationNo;
    
    @Column(name = "reference_no")
    private String referenceNo;
    
    private String citizenId;
    
	private String dateOfBirth;

    @Column(name = "full_name")
    private String fullName;

    // Gender with ID
    @Column(name = "gender_id")
    private String genderId;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "email")
    private String email;

    @Column(name = "dzongkhag_id")
    private String dzongkhagId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "sector_name")
    private String sectorName;

    @Column(name = "occupation_id")
    private Long occupationId;

    @Column(name = "occupation_name")
    private String occupationName;

    @Column(name = "certification_level_id")
    private Long certificationLevelId;

    @Column(name = "certification_level_name")
    private String certificationLevelName;


    @Column(name = "designation")
    private String designation;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "responsibility", columnDefinition = "TEXT")
    private String responsibility;

    @Column(name = "qms_training")
    private String qmsTraining; // Yes or No
    
    @Column(name = "academic_background")
    private String academicBackground;

    // Status field
    @Column(name = "status_id")
    private Integer statusId;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Relationships - Work Experiences using application_no
    @Builder.Default
    @OneToMany(mappedBy = "assessorAccreditorQMSAuditor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperience> workExperiences = new ArrayList<>();
}