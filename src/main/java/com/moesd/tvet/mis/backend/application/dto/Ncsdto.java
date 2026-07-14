package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ncsdto {
    private Integer occupationId;
    private Integer certificationId;
    private String courseTitle;
    private String validityDate;
    private String publicationType;
    private Integer createdBy;
    private Integer updatedBy;
    private List<NcsUnitDto> units;
    private Documentdto[] documents;
}