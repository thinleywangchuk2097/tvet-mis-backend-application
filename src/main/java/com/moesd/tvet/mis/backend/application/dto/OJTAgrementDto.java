package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OJTAgrementDto {

	private String agreementTitle;

	private Date agreementDate;

	private Date startDate;

	private Date endDate;

	private String totalTraineeNo;
	
    private Long companyId;
    
	private String superVisorName;

	private String supervisorContactNo;

	private String description;
	
    private Integer instituteId;
    
	private Integer createdBy;

	private Integer updatedBy;
	
	// Documents
 	private Documentdto[] documents;

}
