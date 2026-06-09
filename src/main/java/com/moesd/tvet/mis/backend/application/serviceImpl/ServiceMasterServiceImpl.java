package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.ServiceMasterRequestDTO;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.ServiceMasterService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service 
@RequiredArgsConstructor
public class ServiceMasterServiceImpl implements ServiceMasterService{
	
	private final ServiceMasterRepository serviceMasterRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public ResponseEntity<?> submitServiceMaster(ServiceMasterRequestDTO request) {
		try {
			log.info("Submitting new service master: {}", request.getServiceName());
			
			// Validate required fields
			if (request.getServiceName() == null || request.getServiceName().trim().isEmpty()) {
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("error", "Service name is required");
				return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(errorResponse);
			}
			
			if (request.getValidityDate() == null || request.getValidityDate().trim().isEmpty()) {
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("error", "Validity date is required");
				return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(errorResponse);
			}
			
			if (request.getRoute() == null || request.getRoute().trim().isEmpty()) {
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("error", "Route is required");
				return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(errorResponse);
			}
			
			// Check if service name already exists
			if (serviceMasterRepository.existsByServiceName(request.getServiceName())) {
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("error", "Service name already exists: " + request.getServiceName());
				return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(errorResponse);
			}
			
			// Create new entity
			ServiceMaster serviceMaster = new ServiceMaster();
			serviceMaster.setServiceName(request.getServiceName().trim());
			serviceMaster.setValidityDate(request.getValidityDate());
			serviceMaster.setRoute(request.getRoute().trim());
			serviceMaster.setDepartmentId("1");
			
			// Set optional fields
			if (request.getLastApplicationNo() != null) {
				serviceMaster.setLastApplicationNo(request.getLastApplicationNo());
			}
			
			if (request.getLicenseLastSequence() != null) {
				serviceMaster.setLicenseLastSequence(request.getLicenseLastSequence());
			}
			
			// Set default values if not provided
			if (request.getHasCertificate() != null && !request.getHasCertificate().isEmpty()) {
				serviceMaster.setHasCertificate(request.getHasCertificate().charAt(0));
			} else {
				serviceMaster.setHasCertificate('Y'); // Default value
			}
			
			if (request.getIsActive() != null && !request.getIsActive().isEmpty()) {
				serviceMaster.setIsActive(request.getIsActive().charAt(0));
			} else {
				serviceMaster.setIsActive('Y'); // Default value
			}
			
			// Save to database
			ServiceMaster savedService = serviceMasterRepository.save(serviceMaster);
			
			log.info("Service master saved successfully with ID: {}", savedService.getId());
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Service master submitted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedService);
			
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
				
		} catch (Exception e) {
			log.error("Error submitting service master: {}", e.getMessage(), e);
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Failed to submit service master: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getAllServiceMaster() {
		List<Tuple> resultList= serviceMasterRepository.getAllServiceMaster();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}
	
	@Override
	public ResponseEntity<?> updateServiceMaster(ServiceMasterRequestDTO request) {
	    try {
	        log.info("Updating service master with ID: {}", request.getId());
	        
	        Optional<ServiceMaster> existingService = serviceMasterRepository.findById(request.getId());
	        
	        if (existingService.isEmpty()) {
	            Map<String, String> errorResponse = new HashMap<>();
	            errorResponse.put("error", "Service not found with ID: " + request.getId());
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	        }
	        
	        ServiceMaster serviceMaster = existingService.get();
	        serviceMaster.setServiceName(request.getServiceName());
	        serviceMaster.setValidityDate(request.getValidityDate());
	        serviceMaster.setRoute(request.getRoute());
	        serviceMaster.setLastApplicationNo(request.getLastApplicationNo());
	        serviceMaster.setLicenseLastSequence(request.getLicenseLastSequence());
	        serviceMaster.setHasCertificate(request.getHasCertificate() != null ? 
	            request.getHasCertificate().charAt(0) : 'Y');
	        serviceMaster.setIsActive(request.getIsActive() != null ? 
	            request.getIsActive().charAt(0) : 'Y');
	        
	        ServiceMaster savedService = serviceMasterRepository.save(serviceMaster);
	        
	        Map<String, Object> successResponse = new HashMap<>();
	        successResponse.put("message", "Service updated successfully");
	        successResponse.put("status", "SUCCESS");
	        successResponse.put("data", savedService);
	        
	        return ResponseEntity.ok(successResponse);
	        
	    } catch (Exception e) {
	        log.error("Error updating service master: {}", e.getMessage(), e);
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "Failed to update service: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	    }
	}

	@Override
	public ResponseEntity<?> softDeleteServiceMaster(Integer serviceId) {
	    try {
	        log.info("Soft deleting service master with ID: {}", serviceId);
	        
	        Optional<ServiceMaster> existingService = serviceMasterRepository.findById(serviceId);
	        
	        if (existingService.isEmpty()) {
	            Map<String, String> errorResponse = new HashMap<>();
	            errorResponse.put("error", "Service not found with ID: " + serviceId);
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	        }
	        
	        ServiceMaster serviceMaster = existingService.get();
	        serviceMaster.setIsActive('N'); // Soft delete
	        serviceMasterRepository.save(serviceMaster);
	        
	        Map<String, String> successResponse = new HashMap<>();
	        successResponse.put("message", "Service deleted successfully");
	        successResponse.put("status", "SUCCESS");
	        
	        return ResponseEntity.ok(successResponse);
	        
	    } catch (Exception e) {
	        log.error("Error soft deleting service master: {}", e.getMessage(), e);
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "Failed to delete service: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	    }
	}
	
	

}
