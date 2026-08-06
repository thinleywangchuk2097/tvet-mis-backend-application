package com.moesd.tvet.mis.backend.application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.model.Gewog;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.model.Occupation;
import com.moesd.tvet.mis.backend.application.model.Sector;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.service.CommonService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/common")
public class CommonController {

	private final CommonService commonService;
    
	@GetMapping("/get-dzongkhags")
	public ResponseEntity<List<Dzongkhag>> getAllDzongkhags() {
		List<Dzongkhag> dzongkhagLists = commonService.getAllDzongkhags();
		return ResponseEntity.ok(dzongkhagLists);
	}
	
	@GetMapping("/get-gewog/{dzongkhagId}")
	public ResponseEntity<List<Gewog>> getGewogByDzongkhagId(@PathVariable Integer dzongkhagId) {
		List<Gewog> gewogLists = commonService.getGewogByDzongkhagId(dzongkhagId);
		return ResponseEntity.ok(gewogLists);
	}

	@GetMapping("/get-sectors")
	public ResponseEntity<List<Sector>> getAllSectors() {
		List<Sector> sectorLists = commonService.getAllSectors();
		return ResponseEntity.ok(sectorLists);
	}

	@GetMapping("/get-occupations")
	public ResponseEntity<List<Occupation>> getAllOccupations() {
		List<Occupation> occupationLists = commonService.getAllOccupations();
		return ResponseEntity.ok(occupationLists);
	}
	
	@GetMapping("/get-service-announcement")
	public ResponseEntity<List<ServiceMaster>> getServiceNameCourseAnnouncement() {
		List<ServiceMaster> data = commonService.getServiceNameCourseAnnouncement();
		return ResponseEntity.ok(data);
	}

	@GetMapping("/get-child-dropdown/{parentId}")
	public ResponseEntity<List<Map<String, Object>>> getByParentId(@PathVariable Integer parentId) {
	    List<Map<String, Object>> children = commonService.getByParentId(parentId);
	    return ResponseEntity.ok(children);
	}
	
	@GetMapping("/get-curriculum-service-types")
	public ResponseEntity<List<Map<String, Object>>> getCurriculumServiceType() {
	    List<Map<String, Object>> children = commonService.getCurriculumServiceType();
	    return ResponseEntity.ok(children);
	}
	
	@GetMapping("/get-service-name/{id}")
	public ResponseEntity<Optional<ServiceMaster>> getServiceName(@PathVariable Integer id) {
		Optional<ServiceMaster> data = commonService.getServiceName(id);
	    return ResponseEntity.ok(data);
	}
	
	@GetMapping("/get-quality-standards/{serviceId}")
	public ResponseEntity<List<InstituteRegistrationQualityStandard>> getAllQualitystandards(@PathVariable Integer serviceId) {
		List<InstituteRegistrationQualityStandard> qualitystandards = commonService.getAllQualitystandards(serviceId);
		return ResponseEntity.ok(qualitystandards);
	}
	
	@GetMapping("/get-occupations/{sectorId}")
	public ResponseEntity<List<Occupation>> getOccupationsBySectorId(@PathVariable Integer sectorId) {
		List<Occupation> occupations = commonService.getOccupationsBySectorId(sectorId);
		return ResponseEntity.ok(occupations);
	}
	
	@GetMapping("/get-announcement-application-details")
	public ResponseEntity<List<ObjectNode>> getAllCourseAnnouncement() {
		List<ObjectNode> Details = commonService.getAllCourseAnnouncement();
		return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-all-certificate-levels")
	public ResponseEntity<List<ObjectNode>> getAllCertificateLevels() {
		List<ObjectNode> Details = commonService.getAllCertificateLevels();
		return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-announcement-course/{application_no}")
	public ResponseEntity<List<ObjectNode>> getCourseAnnouncementByApplicationNo(@PathVariable String application_no) {
		List<ObjectNode> Details = commonService.getCourseAnnouncementByApplicationNo(application_no);
		return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-institute-name/{instituteId}")
	public ResponseEntity<List<ObjectNode>> getInstituteNameByInstituteId(@PathVariable String instituteId) {
		List<ObjectNode> Details = commonService.getInstituteNameByInstituteId(instituteId);
		return ResponseEntity.ok(Details);
	}
	
	
	@GetMapping("/get-reassessment-announcement-course/{application_no}")
	public ResponseEntity<List<ObjectNode>> getReAssessmentAnnouncementByApplicationNo(@PathVariable String application_no) {
		List<ObjectNode> Details = commonService.getReAssessmentAnnouncementByApplicationNo(application_no);
		return ResponseEntity.ok(Details);
	}
	
	
	

	@GetMapping("/download-document")
	public void downloadFile(@RequestParam String upload_url, @RequestParam String fileName,
			HttpServletResponse response) throws IOException {

		// Decode URL-encoded path
		String decodedPath = URLDecoder.decode(upload_url, StandardCharsets.UTF_8);
		File file = new File(decodedPath);

		if (!file.exists() || !file.isFile()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
			return;
		}

		// Detect MIME type
		String mimeType = Files.probeContentType(file.toPath());
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}

		// Set response headers
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		response.setContentLengthLong(file.length());

		// Stream file content
		Files.copy(file.toPath(), response.getOutputStream());
		response.flushBuffer();
	}
	
}
