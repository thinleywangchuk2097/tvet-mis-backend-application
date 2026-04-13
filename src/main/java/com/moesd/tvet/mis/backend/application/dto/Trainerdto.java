package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trainerdto {
    private Long nationalityId;
    private String cid;
    private String workPermit;
    private String name;
    private Long genderId;
    private String qualification;
    private Integer experience;
    private Long typeId;
}
