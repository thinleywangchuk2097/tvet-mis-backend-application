package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import com.moesd.tvet.mis.backend.application.dto.FileDownloaddto;
import com.moesd.tvet.mis.backend.application.dto.TvetDocumentdto;

public interface TvetDocumentService {

	List<Map<String, Object>> getDocuments(String category);

	FileDownloaddto download(Long id);

	Map<String, Object> create(TvetDocumentdto request, String userId);

	Map<String, Object> update(Long id, TvetDocumentdto request, String userId);

	void delete(Long id);
}
