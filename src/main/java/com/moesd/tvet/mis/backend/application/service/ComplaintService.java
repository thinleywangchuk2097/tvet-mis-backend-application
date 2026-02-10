package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Complaintdto;

public interface ComplaintService {
	
	ResponseEntity<?> submit(Complaintdto request);
	
	List<ObjectNode> getComplaintDetails(String application_no);
}
