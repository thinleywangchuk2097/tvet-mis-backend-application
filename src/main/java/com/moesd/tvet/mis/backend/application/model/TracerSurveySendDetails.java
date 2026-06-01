package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tracer_survey_send_dtls")
public class TracerSurveySendDetails {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "application_no", nullable = false)
	    private String applicationNo;
	    
	    private String questionApplicationNo;
	    
	    private String tracerUrl;
	    
	    private String uniqueId;
	    
	    private String applicationName;
	    
	    private String mobileNo;
	    
	    private String emailId;
	    
	    private Integer statusId;
	    
	    private Integer parentTracerTypeId;
	    
	    private Integer subTracerTypeId;
	    
	    @Column(name = "created_at")
	    private LocalDateTime createdAt;
	    
	    @Column(name = "updated_at")
	    private LocalDateTime updatedAt;
	    
	    private Integer updatedBy;
	    
	    private Integer createdBy;
	

	    
}
