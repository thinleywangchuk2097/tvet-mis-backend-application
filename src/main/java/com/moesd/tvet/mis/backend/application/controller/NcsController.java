package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;
import com.moesd.tvet.mis.backend.application.service.NcsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/ncs")
public class NcsController {
    
    private final NcsService ncsService;
    
    @PostMapping("/ncs-create")
    public ResponseEntity<?> submitNcs(@RequestBody Ncsdto request) {
        log.info("Received POST request to create NCS");
        return ncsService.submitNcs(request);
    }
    
    @PutMapping("/ncs-update/{publicationId}")
    public ResponseEntity<?> updateNcs(@PathVariable Integer publicationId, @RequestBody Ncsdto request) {
        log.info("Received PUT request to update NCS with id: {}", publicationId);
        return ncsService.updateNcs(publicationId, request);
    }
    
    @DeleteMapping("/ncs-delete/{publicationId}")
    public ResponseEntity<?> deleteNcs(@PathVariable Integer publicationId) {
        log.info("Received DELETE request for NCS with id: {}", publicationId);
        return ncsService.deleteNcs(publicationId);
    }
    
    @GetMapping("/get-application-details")
    public ResponseEntity<List<ObjectNode>> getCourseDetailsAnnouncementByUserId() {
        log.info("Received GET request for NCS details");
        List<ObjectNode> details = ncsService.getCourseDetailsAnnouncementByUserId();
        log.info("Returning {} records", details.size());
        return ResponseEntity.ok(details);
    }
}