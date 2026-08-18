package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "tbl_ncs_app_dtls")
public class NcsApp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;
    
    private String applicationNo;
    
    private Integer sectorId;
    
    private Integer serviceId;
    
    private Integer occupationId;
    
    private Integer certificationId;
    
    private String programmeTitle;
    
    private Date validityDate;
        
    private Integer createdBy;
    
    private Integer updatedBy;
    
    @OneToMany(mappedBy = "ncsApp", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<NcsUnit> units;
}