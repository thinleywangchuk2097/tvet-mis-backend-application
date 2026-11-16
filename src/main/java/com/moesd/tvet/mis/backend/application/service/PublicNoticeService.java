package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import com.moesd.tvet.mis.backend.application.dto.PublicNoticedto;

public interface PublicNoticeService {

	List<Map<String, Object>> getNotices();

	Map<String, Object> create(PublicNoticedto request, Integer userId);

	Map<String, Object> update(Long id, PublicNoticedto request, Integer userId);

	void delete(Long id);
}
