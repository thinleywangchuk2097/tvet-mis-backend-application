package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Complaintdto;
import com.moesd.tvet.mis.backend.application.service.ComplaintService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/complaint")
public class ComplaintController {
	
	private final ComplaintService complaintService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> submit(@RequestBody Complaintdto request) {
		return (complaintService.submit(request));
	}
	
	@GetMapping("/get-complaint-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getComplaintDetails(@PathVariable String application_no){
	    List<ObjectNode> complaintLists = complaintService.getComplaintDetails(application_no);
	    return ResponseEntity.ok(complaintLists);
	}

}
