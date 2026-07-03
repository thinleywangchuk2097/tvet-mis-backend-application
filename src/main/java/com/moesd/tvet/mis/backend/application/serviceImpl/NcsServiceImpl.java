package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Documentdto;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;
import com.moesd.tvet.mis.backend.application.dto.NcsUnitDto;
import com.moesd.tvet.mis.backend.application.model.NcsApp;
import com.moesd.tvet.mis.backend.application.model.NcsUnit;
import com.moesd.tvet.mis.backend.application.repository.NcsRepository;
import com.moesd.tvet.mis.backend.application.repository.NcsUnitRepository;
import com.moesd.tvet.mis.backend.application.service.NcsService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcsServiceImpl implements NcsService {
    
    private final NcsRepository ncsRepository;
    private final NcsUnitRepository ncsUnitRepository;
    private final ObjectToJson objectTojson;
    private final ObjectMapper objectMapper;
    private final DocumentFileUploadService documentFileUploadService;
    
    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.parse(dateString);
        } catch (Exception e) {
            log.error("Error parsing date: {}", dateString, e);
            return null;
        }
    }
    
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.format(date);
        } catch (Exception e) {
            log.error("Error formatting date: {}", date, e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> submitNcs(Ncsdto request) {
        try {
            log.info("Received NCS submission request: {}", request);
            
            Date validityDate = parseDate(request.getValidityDate());
            
            NcsApp ncsApp = NcsApp.builder()
                    .occupationId(request.getOccupationId())
                    .certificationId(request.getCertificationId())
                    .courseTitle(request.getCourseTitle())
                    .validityDate(validityDate)
                    .publicationType(request.getPublicationType())
                    .createdBy(request.getCreatedBy())
                    .build();
            
            NcsApp savedNcs = ncsRepository.save(ncsApp);
            log.info("Saved NCS with publicationId: {}", savedNcs.getPublicationId());
            
            if (request.getUnits() != null && !request.getUnits().isEmpty()) {
                List<NcsUnit> units = new ArrayList<>();
                for (NcsUnitDto unitDto : request.getUnits()) {
                    NcsUnit unit = NcsUnit.builder()
                            .unitCode(unitDto.getUnitCode())
                            .unitTitle(unitDto.getUnitTitle())
                            .ncsApp(savedNcs)
                            .createdBy(request.getCreatedBy())
                            .isActive(true)
                            .build();
                    units.add(unit);
                }
                ncsUnitRepository.saveAll(units);
                log.info("Saved {} units for publicationId: {}", units.size(), savedNcs.getPublicationId());
            }
            
            // Handle documents - Filter only documents with content
            if (request.getDocuments() != null && request.getDocuments().length > 0) {
                List<Documentdto> documentsToSave = new ArrayList<>();
                
                for (Documentdto doc : request.getDocuments()) {
                    // Check if content exists and has length > 0
                    if (doc.getContent() != null && doc.getContent().length > 0) {
                        documentsToSave.add(doc);
                        log.info("Document with content will be saved: {}", doc.getName());
                    } else {
                        log.info("Skipping document without content: {}", doc.getName());
                    }
                }
                
                if (!documentsToSave.isEmpty()) {
                    String userId = request.getCreatedBy() != null ? request.getCreatedBy().toString() : "1";
                    String publicationIdStr = savedNcs.getPublicationId().toString();
                    Documentdto[] docsArray = documentsToSave.toArray(new Documentdto[0]);
                    documentFileUploadService.saveDocument(
                        docsArray, 
                        publicationIdStr,
                        "ncs_publication",
                        21,
                        userId, 
                        "NCS_PUBLICATION"
                    );
                    log.info("Saved {} documents for publicationId: {}", docsArray.length, savedNcs.getPublicationId());
                } else {
                    log.info("No documents with content to save for publicationId: {}", savedNcs.getPublicationId());
                }
            }
            
            return ResponseEntity.status(201).body(Map.of(
                    "status", 201,
                    "message", "NCS created successfully",
                    "publicationId", savedNcs.getPublicationId()
            ));
            
        } catch (Exception e) {
            log.error("Error submitting NCS: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to submit NCS", "error", e.getMessage()));
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> updateNcs(Integer publicationId, Ncsdto request) {
        try {
            log.info("Updating NCS with publicationId: {}", publicationId);
            
            Date validityDate = parseDate(request.getValidityDate());
            
            NcsApp existingNcs = ncsRepository.findById(publicationId)
                    .orElseThrow(() -> new RuntimeException("NCS not found with id: " + publicationId));
            
            existingNcs.setOccupationId(request.getOccupationId());
            existingNcs.setCertificationId(request.getCertificationId());
            existingNcs.setCourseTitle(request.getCourseTitle());
            existingNcs.setValidityDate(validityDate);
            existingNcs.setPublicationType(request.getPublicationType());
            existingNcs.setUpdatedBy(request.getUpdatedBy());
            
            NcsApp updatedNcs = ncsRepository.save(existingNcs);
            log.info("Updated NCS with publicationId: {}", updatedNcs.getPublicationId());
            
            // Delete existing units
            ncsUnitRepository.deleteByNcsAppPublicationId(publicationId);
            log.info("Deleted existing units for publicationId: {}", publicationId);
            
            // Save new units
            if (request.getUnits() != null && !request.getUnits().isEmpty()) {
                List<NcsUnit> units = new ArrayList<>();
                for (NcsUnitDto unitDto : request.getUnits()) {
                    NcsUnit unit = NcsUnit.builder()
                            .unitCode(unitDto.getUnitCode())
                            .unitTitle(unitDto.getUnitTitle())
                            .ncsApp(updatedNcs)
                            .updatedBy(request.getUpdatedBy())
                            .isActive(true)
                            .build();
                    units.add(unit);
                }
                ncsUnitRepository.saveAll(units);
                log.info("Saved {} units for publicationId: {}", units.size(), publicationId);
            }
            
            // Handle documents - CRITICAL: Filter documents with content ONLY
            if (request.getDocuments() != null && request.getDocuments().length > 0) {
                List<Documentdto> documentsToSave = new ArrayList<>();
                
                // IMPORTANT: Only add documents that have actual content (byte array with length > 0)
                for (Documentdto doc : request.getDocuments()) {
                    // Check if content is not null and has length
                    boolean hasContent = doc.getContent() != null && doc.getContent().length > 0;
                    
                    if (hasContent) {
                        documentsToSave.add(doc);
                        log.info("Document WITH content will be saved: {}, size: {} bytes", 
                            doc.getName(), doc.getContent().length);
                    } else {
                        log.info("Document WITHOUT content - SKIPPING: {}", doc.getName());
                    }
                }
                
                // Only call saveDocument if there are documents with content
                if (!documentsToSave.isEmpty()) {
                    String userId = request.getUpdatedBy() != null ? request.getUpdatedBy().toString() : "1";
                    String publicationIdStr = publicationId.toString();
                    Documentdto[] docsArray = documentsToSave.toArray(new Documentdto[0]);
                    
                    log.info("Saving {} new documents for publicationId: {}", docsArray.length, publicationId);
                    documentFileUploadService.saveDocument(
                        docsArray, 
                        publicationIdStr,
                        "ncs_publication",
                        21,
                        userId, 
                        "NCS_PUBLICATION"
                    );
                    log.info("Successfully saved {} new documents", docsArray.length);
                } else {
                    log.info("NO new documents to save for publicationId: {}", publicationId);
                }
            } else {
                log.info("No documents in request for publicationId: {}", publicationId);
            }
            
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "NCS updated successfully",
                    "publicationId", updatedNcs.getPublicationId()
            ));
            
        } catch (Exception e) {
            log.error("Error updating NCS: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to update NCS", "error", e.getMessage()));
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> deleteNcs(Integer publicationId) {
        try {
            log.info("Deleting NCS with publicationId: {}", publicationId);
            
            NcsApp existingNcs = ncsRepository.findById(publicationId)
                    .orElseThrow(() -> new RuntimeException("NCS not found with id: " + publicationId));
            
            ncsUnitRepository.deleteByNcsAppPublicationId(publicationId);
            ncsRepository.delete(existingNcs);
            
            log.info("Deleted NCS with publicationId: {}", publicationId);
            
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "NCS deleted successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error deleting NCS: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to delete NCS", "error", e.getMessage()));
        }
    }
    
    @Override
    public List<ObjectNode> getCourseDetailsAnnouncementByUserId() {
        try {
            log.info("Fetching NCS details with units and documents");
            List<Tuple> resultList = ncsRepository.getCourseDetailsAnnouncementByUserId();
            log.info("Found {} records", resultList.size());
            
            List<ObjectNode> result = new ArrayList<>();
            
            for (Tuple tuple : resultList) {
                ObjectNode node = objectMapper.createObjectNode();
                
                // Basic fields
                node.put("publicationId", tuple.get("publicationId") != null ? 
                    Integer.parseInt(tuple.get("publicationId").toString()) : 0);
                node.put("courseTitle", tuple.get("courseTitle") != null ? 
                    tuple.get("courseTitle").toString() : "");
                node.put("publicationType", tuple.get("publicationType") != null ? 
                    tuple.get("publicationType").toString() : "");
                node.put("occupationId", tuple.get("occupationId") != null ? 
                    Integer.parseInt(tuple.get("occupationId").toString()) : 0);
                node.put("certificationId", tuple.get("certificationId") != null ? 
                    Integer.parseInt(tuple.get("certificationId").toString()) : 0);
                node.put("occupationName", tuple.get("occupationName") != null ? 
                    tuple.get("occupationName").toString() : "");
                node.put("sectorName", tuple.get("sectorName") != null ? 
                    tuple.get("sectorName").toString() : "");
                node.put("certificationName", tuple.get("certificationName") != null ? 
                    tuple.get("certificationName").toString() : "");
                
                // Handle date
                Object validityDate = tuple.get("validityDate");
                node.put("validityDate", validityDate != null ? validityDate.toString() : "");
                
                // Handle units - ensure valid JSON
                Object units = tuple.get("units");
                if (units != null) {
                    String unitsStr = units.toString();
                    try {
                        objectMapper.readTree(unitsStr);
                        node.put("units", unitsStr);
                    } catch (Exception e) {
                        log.warn("Invalid units JSON: {}", unitsStr);
                        node.put("units", "[]");
                    }
                } else {
                    node.put("units", "[]");
                }
                
                // Handle documents - ensure valid JSON
                Object documents = tuple.get("documents");
                if (documents != null) {
                    String docStr = documents.toString();
                    log.info("Documents for publication {}: {}", 
                        tuple.get("publicationId"), docStr);
                    try {
                        objectMapper.readTree(docStr);
                        node.put("documents", docStr);
                    } catch (Exception e) {
                        log.warn("Invalid documents JSON: {}", docStr);
                        node.put("documents", "[]");
                    }
                } else {
                    log.warn("No documents found for publication: {}", tuple.get("publicationId"));
                    node.put("documents", "[]");
                }
                
                result.add(node);
            }
            
            log.info("Converted {} records to JSON", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("Error fetching NCS details: ", e);
            throw new RuntimeException("Failed to fetch NCS details", e);
        }
    }
}