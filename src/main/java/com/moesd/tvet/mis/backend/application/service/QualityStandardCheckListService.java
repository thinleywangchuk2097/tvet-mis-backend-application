package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.QualityStandardCheckListDto;

public interface QualityStandardCheckListService {

	/** Services that have a quality standard checklist (for the service dropdown). */
	List<ObjectNode> getServices();

	ResponseEntity<?> getByService(Integer serviceId);

	ResponseEntity<?> save(QualityStandardCheckListDto request);

	ResponseEntity<?> delete(Integer id);
}
