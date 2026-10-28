package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moesd.tvet.mis.backend.application.dto.QualityStandardCheckListDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.repository.QualityStandardRepository;
import com.moesd.tvet.mis.backend.application.service.QualityStandardCheckListService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Master data for quality standard checklists (e.g. CMFG monitoring = service 55).
 * Structure: PARENT -> CHECKLIST, or PARENT -> SUB_PARENT -> CHECKLIST.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityStandardCheckListServiceImpl implements QualityStandardCheckListService {

	public static final String PARENT = "PARENT";
	public static final String SUB_PARENT = "SUB_PARENT";
	public static final String CHECKLIST = "CHECKLIST";

	private final QualityStandardRepository qualityStandardRepository;
	private final JdbcTemplate jdbcTemplate;
	private final ObjectToJson objectTojson;

	/** Tables that store answers (standard_id) for checklists. */
	private static final String[] RESPONSE_TABLES = { "tbl_cmfg_monitoring_dtl",
			"tbl_accredited_course_quality_standard_Response", "tbl_non_accredited_course_quality_standard_Response",
			"tbl_institute_registration_quality_response_app" };

	// ---------------------------------------------------------------- read

	@Override
	public List<ObjectNode> getServices() {
		return objectTojson._toJson(qualityStandardRepository.getServiceNameForQualityCheckList());
	}

	@Override
	public ResponseEntity<?> getByService(Integer serviceId) {
		List<InstituteRegistrationQualityStandard> list = qualityStandardRepository.findByServiceId(serviceId);
		list.sort(Comparator.comparing((InstituteRegistrationQualityStandard q) -> nz(q.getParentId()))
				.thenComparing(q -> orderOf(q.getDisplayOrder()))
				.thenComparing(InstituteRegistrationQualityStandard::getId));
		return ResponseEntity.ok(list);
	}

	// ---------------------------------------------------------------- create / update

	@Override
	@Transactional
	public ResponseEntity<?> save(QualityStandardCheckListDto r) {
		try {
			if (r == null) throw new RecordNotFoundException("Request is required");
			if (r.getServiceId() == null) throw new RecordNotFoundException("serviceId is required");
			if (r.getDropdownName() == null || r.getDropdownName().isBlank())
				throw new RecordNotFoundException("Title is required");
			String type = r.getItemType() == null ? "" : r.getItemType().trim().toUpperCase();
			if (!Set.of(PARENT, SUB_PARENT, CHECKLIST).contains(type))
				throw new RecordNotFoundException("Type must be PARENT, SUB_PARENT or CHECKLIST");

			Integer parentId = nz(r.getParentId()) == 0 ? 0 : r.getParentId();

			// ---- parent rules ----
			if (PARENT.equals(type)) {
				parentId = 0;
			} else {
				if (parentId == 0)
					throw new RecordNotFoundException(
							SUB_PARENT.equals(type) ? "Select the parent" : "Select the parent or sub parent");
				InstituteRegistrationQualityStandard parent = qualityStandardRepository.findById(parentId)
						.orElseThrow(() -> new RecordNotFoundException("Selected parent does not exist"));
				if (!r.getServiceId().equals(parent.getServiceId()))
					throw new RecordNotFoundException("Parent belongs to a different service");
				String parentType = typeOf(parent);
				if (SUB_PARENT.equals(type) && !PARENT.equals(parentType))
					throw new RecordNotFoundException("A sub parent must be placed under a parent");
				if (CHECKLIST.equals(type) && !(PARENT.equals(parentType) || SUB_PARENT.equals(parentType)))
					throw new RecordNotFoundException("A checklist item must be under a parent or sub parent");
			}

			InstituteRegistrationQualityStandard item;
			if (r.getId() != null) {
				item = qualityStandardRepository.findById(r.getId())
						.orElseThrow(() -> new RecordNotFoundException("Item not found: " + r.getId()));
				if (r.getId().equals(parentId)) throw new RecordNotFoundException("An item cannot be its own parent");
				if (isDescendant(r.getId(), parentId))
					throw new RecordNotFoundException("Cannot move an item under its own sub item");

				List<InstituteRegistrationQualityStandard> children = qualityStandardRepository.findByParentId(r.getId());
				if (!children.isEmpty()) {
					if (CHECKLIST.equals(type))
						throw new RecordNotFoundException("This item has sub items, so it cannot be a checklist item");
					if (SUB_PARENT.equals(type)
							&& children.stream().anyMatch(c -> !CHECKLIST.equals(typeOf(c))))
						throw new RecordNotFoundException("A sub parent can only contain checklist items");
				}
			} else {
				item = InstituteRegistrationQualityStandard.builder()
						.serviceId(r.getServiceId())
						.createdAt(LocalDateTime.now())
						.build();
			}

			item.setParentId(parentId);
			item.setItemType(type);
			item.setDropdownName(r.getDropdownName().trim());
			item.setDescription(r.getDescription() == null || r.getDescription().isBlank()
					? null : r.getDescription().trim());
			item.setIsActive(r.getIsActive() == null ? 1 : (r.getIsActive() == 0 ? 0 : 1));
			item.setUpdatedBy(r.getUpdatedBy());
			item.setDisplayOrder(r.getDisplayOrder() == null || r.getDisplayOrder().isBlank()
					? String.valueOf(nextOrder(r.getServiceId(), parentId, item.getId()))
					: r.getDisplayOrder().trim());

			InstituteRegistrationQualityStandard saved = qualityStandardRepository.save(item);

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("status", r.getId() == null ? 201 : 200);
			body.put("message", r.getId() == null ? "Item created successfully" : "Item updated successfully");
			body.put("data", saved);
			return ResponseEntity.status(r.getId() == null ? HttpStatus.CREATED : HttpStatus.OK).body(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.badRequest().body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to save quality standard checklist item", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to save item"));
		}
	}

	// ---------------------------------------------------------------- delete

	/**
	 * Deletes an item. Items with sub items must be emptied first. Items already answered in
	 * any submission are deactivated instead, so past submissions keep their history.
	 */
	@Override
	@Transactional
	public ResponseEntity<?> delete(Integer id) {
		try {
			InstituteRegistrationQualityStandard item = qualityStandardRepository.findById(id)
					.orElseThrow(() -> new RecordNotFoundException("Item not found: " + id));

			List<InstituteRegistrationQualityStandard> children = qualityStandardRepository.findByParentId(id);
			if (!children.isEmpty()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "message",
						"This item has " + children.size() + " sub item(s). Delete or move them first."));
			}

			long used = usageCount(id);
			if (used > 0) {
				item.setIsActive(0);
				qualityStandardRepository.save(item);
				return ResponseEntity.ok(Map.of("status", 200, "deactivated", true, "message",
						"This item is used in " + used
								+ " submitted answer(s), so it was deactivated instead of deleted."));
			}

			qualityStandardRepository.delete(item);
			return ResponseEntity.ok(Map.of("status", 200, "deactivated", false, "message",
					"Item deleted successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to delete quality standard checklist item", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to delete item"));
		}
	}

	// ---------------------------------------------------------------- helpers

	/** Explicit type, or inferred for older rows: top level = parent, has children = sub parent, else checklist. */
	private String typeOf(InstituteRegistrationQualityStandard q) {
		if (q.getItemType() != null && !q.getItemType().isBlank()) return q.getItemType().toUpperCase();
		if (nz(q.getParentId()) == 0) return PARENT;
		return qualityStandardRepository.findByParentId(q.getId()).isEmpty() ? CHECKLIST : SUB_PARENT;
	}

	private boolean isDescendant(Integer ancestorId, Integer candidateId) {
		Set<Integer> seen = new HashSet<>();
		Integer current = candidateId;
		while (current != null && current != 0 && seen.add(current)) {
			if (current.equals(ancestorId)) return true;
			current = qualityStandardRepository.findById(current).map(InstituteRegistrationQualityStandard::getParentId)
					.orElse(null);
		}
		return false;
	}

	private int nextOrder(Integer serviceId, Integer parentId, Integer selfId) {
		return qualityStandardRepository.findByServiceId(serviceId).stream()
				.filter(q -> nz(q.getParentId()) == nz(parentId))
				.filter(q -> selfId == null || !selfId.equals(q.getId()))
				.mapToInt(q -> orderOf(q.getDisplayOrder()))
				.max().orElse(0) + 1;
	}

	private long usageCount(Integer standardId) {
		long total = 0;
		for (String table : RESPONSE_TABLES) {
			try {
				Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE standard_id = ?",
						Long.class, standardId);
				total += n == null ? 0 : n;
			} catch (Exception e) {
				log.debug("Usage check skipped for {}: {}", table, e.getMessage());
			}
		}
		return total;
	}

	private static int nz(Integer v) {
		return v == null ? 0 : v;
	}

	private static int orderOf(String v) {
		try {
			return v == null ? 0 : Integer.parseInt(v.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
