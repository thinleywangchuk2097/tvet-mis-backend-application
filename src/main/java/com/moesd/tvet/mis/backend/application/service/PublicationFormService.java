package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.dto.PublicationFormdto;

public interface PublicationFormService {

	List<Map<String, Object>> getDocuments(String category);

	FileDownloaddto download(Long id);

	Map<String, Object> create(PublicationFormdto request, String userId);

	Map<String, Object> update(Long id, PublicationFormdto request, String userId);

	void delete(Long id);
}
