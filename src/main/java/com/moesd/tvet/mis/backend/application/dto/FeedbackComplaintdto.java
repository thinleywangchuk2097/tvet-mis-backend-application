package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Public submission of feedback or a complaint. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackComplaintdto {
	/** FEEDBACK or COMPLAINT */
	private String type;
	private Boolean anonymous;
	private String name;
	private String email;
	private String contactNo;
	private String submitterRole;
	private String category;
	// feedback
	private Integer rating;
	private String experience;
	private String suggestions;
	private Boolean recommend;
	// complaint, incidentDate as yyyy-MM-dd
	private String incidentDate;
	private String againstPerson;
	private String institution;
	private String urgency;
	private String incidentDetails;
	private String desiredResolution;
	/** complaint only: supporting documents (base64, same shape as the rest of the system) */
	private List<Documentdto> attachments;
}
