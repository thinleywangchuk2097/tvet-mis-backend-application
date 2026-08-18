package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "tbl_ncs_units")
public class NcsUnit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id", nullable = false, unique = true)
    private Integer unitId;
    
    @Column(name = "unit_code", nullable = false, length = 50)
    private String unitCode;
    
    @Column(name = "unit_title", nullable = false, length = 255)
    private String unitTitle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncs_id", referencedColumnName = "id", nullable = false)
    private NcsApp ncsApp;
}