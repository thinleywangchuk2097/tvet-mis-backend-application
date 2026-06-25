package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tbl_auditor_work_experience")
public class AuditorWorkExperience {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with AssessorAccreditorQMSAuditor using application_no
    @ManyToOne
    @JoinColumn(name = "application_no", // FK column in work_experiences table
            referencedColumnName = "application_no" // column in AssessorAccreditorQMSAuditor
    )
    private AssessorAccreditorQMSAuditor assessorAccreditorQMSAuditor;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "year")
    private Integer year;

    @Column(name = "responsibility", columnDefinition = "TEXT")
    private String responsibility;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
