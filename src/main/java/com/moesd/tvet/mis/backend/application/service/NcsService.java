package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;

public interface NcsService {

	ResponseEntity<?> submitNcs(Ncsdto request);

	ResponseEntity<?> updateNcs(Integer editingId, Ncsdto request);

	List<ObjectNode> getNcsDetails();

	List<ObjectNode> getAlreadyNcsDetailsExist(Integer sector_id, Integer occupation_id, Integer certification_id);
	
	List<ObjectNode> getProgrammeTitleById(Integer programmeId);
}