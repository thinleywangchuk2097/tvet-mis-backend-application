package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import com.moesd.tvet.mis.backend.application.dto.ComplaintActiondto;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.dto.FeedbackComplaintdto;

public interface FeedbackComplaintService {

	/** public: returns the reference number */
	Map<String, Object> submit(FeedbackComplaintdto request);

	/** any logged-in user (no email / phone) */
	List<Map<String, Object>> getFeedback();

	/** TVET QC */
	List<Map<String, Object>> getComplaints();

	/** TVET QC */
	Map<String, Object> updateComplaint(Long id, ComplaintActiondto request, String actionBy);

	/** TVET QC: one attachment of a complaint */
	FileDownloaddto downloadAttachment(Long complaintId, Long fileId);
}
