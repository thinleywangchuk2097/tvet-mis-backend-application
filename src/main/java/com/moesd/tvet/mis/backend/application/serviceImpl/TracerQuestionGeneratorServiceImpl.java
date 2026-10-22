package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.EmployerDTO;
import com.moesd.tvet.mis.backend.application.dto.SurveyResponseRequestDTO.ResponseItem;
import com.moesd.tvet.mis.backend.application.dto.TracerQuestionDTO;
import com.moesd.tvet.mis.backend.application.dto.TracerQuestionGeneratorRequest;
import com.moesd.tvet.mis.backend.application.dto.TracerSendRequestDTO;
import com.moesd.tvet.mis.backend.application.dto.TracerSubQuestionDTO;
import com.moesd.tvet.mis.backend.application.dto.TraineeDTO;
import com.moesd.tvet.mis.backend.application.model.TracerQuestionGenerator;
import com.moesd.tvet.mis.backend.application.model.TracerQuestionGeneratorOptionId;
import com.moesd.tvet.mis.backend.application.model.TracerSubQuestionGenerator;
import com.moesd.tvet.mis.backend.application.model.TracerSubQuestionGeneratorOptionId;
import com.moesd.tvet.mis.backend.application.model.TracerSurveyResponseDetails;
import com.moesd.tvet.mis.backend.application.model.TracerSurveySendDetails;
import com.moesd.tvet.mis.backend.application.repository.TracerQuestionGeneratorRepository;
import com.moesd.tvet.mis.backend.application.repository.TracerQuestionTypeDropdownRepository;
import com.moesd.tvet.mis.backend.application.repository.TracerSurveyResponseDetailsRepository;
import com.moesd.tvet.mis.backend.application.repository.TracerSurveySendDetailsRepository;
import com.moesd.tvet.mis.backend.application.service.TracerQuestionGeneratorService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.GenerateTracerUniqueId;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TracerQuestionGeneratorServiceImpl implements TracerQuestionGeneratorService {

	private final TracerQuestionTypeDropdownRepository tracerQuestionTypeDropdownRepository;
	private final ObjectToJson objectTojson;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final GenerateTracerUniqueId generateTracerUniqueId;
	private final TracerQuestionGeneratorRepository repository;
	private final TracerSurveySendDetailsRepository tracerSurveySendDetailsRepository;
	private final TracerSurveyResponseDetailsRepository tracerSurveyResponseDetailsRepository;
	private final TracerQuestionGeneratorRepository tracerQuestionGeneratorRepository;

	@Override
	public List<ObjectNode> getTracerQuestionDropdownType() {
		return objectTojson._toJson(tracerQuestionTypeDropdownRepository.getTracerQuestionDropdownType());
	}

	@Override
	public List<ObjectNode> getParentTracerTypes() {
		return objectTojson._toJson(tracerQuestionTypeDropdownRepository.getParentTracerTypes());
	}

	// ─────────────────────────────────────────────────────────────────────
	// saveTracerQuestions — CREATE (insert) or UPDATE (diff-based)
	// ─────────────────────────────────────────────────────────────────────
	@Override
	@Transactional
	public List<TracerQuestionGenerator> saveTracerQuestions(TracerQuestionGeneratorRequest request) {
		LocalDateTime now = LocalDateTime.now();
		List<TracerQuestionDTO> dtos = request.getQuestions();

		String requestedApplicationNo = request.getApplicationNo();
		boolean isUpdate =
				requestedApplicationNo != null
						&& !requestedApplicationNo.trim().isEmpty()
						&& repository.existsByApplicationNo(requestedApplicationNo);

		String applicationNo = isUpdate
				? requestedApplicationNo
				: generateApplicationNumber.generateApplicationNumber(44);

		Map<String, Long> clientIdToDbId = new HashMap<>();

		if (!isUpdate) {
			List<TracerQuestionGenerator> created =
					insertAll(dtos, request, applicationNo, now, clientIdToDbId);
			resolveDependencies(dtos, created, clientIdToDbId);
			repository.saveAll(created);
			repository.flush();
			log.info("CREATE — {} parent row(s) saved under {}", created.size(), applicationNo);
			return created;
		}

		return diffUpdate(dtos, request, applicationNo, now, clientIdToDbId);
	}

	/* ============================================================
	   CREATE PATH
	   ============================================================ */
	private List<TracerQuestionGenerator> insertAll(
			List<TracerQuestionDTO> dtos,
			TracerQuestionGeneratorRequest request,
			String applicationNo,
			LocalDateTime now,
			Map<String, Long> clientIdToDbId) {

		List<TracerQuestionGenerator> saved = new ArrayList<>();

		for (TracerQuestionDTO qDto : dtos) {
			TracerQuestionGenerator entity =
					buildParentEntity(qDto, request, applicationNo, now);
			addParentOptions(entity, qDto.getOptions(), now);
			addSubQuestions(entity, qDto.getSubQuestions(), now);

			TracerQuestionGenerator persisted = repository.saveAndFlush(entity);
			saved.add(persisted);
			registerClientIds(clientIdToDbId, qDto, persisted);
		}
		return saved;
	}

	/* ============================================================
	   UPDATE PATH — diff-based (update / insert / delete per row)
	   ============================================================ */
	private List<TracerQuestionGenerator> diffUpdate(
			List<TracerQuestionDTO> dtos,
			TracerQuestionGeneratorRequest request,
			String applicationNo,
			LocalDateTime now,
			Map<String, Long> clientIdToDbId) {

		List<TracerQuestionGenerator> existing = repository.findAllByApplicationNo(applicationNo);
		Map<Long, TracerQuestionGenerator> existingById = existing.stream()
				.collect(Collectors.toMap(TracerQuestionGenerator::getId, q -> q));

		Set<Long> keepParentIds = new HashSet<>();
		List<TracerQuestionGenerator> result = new ArrayList<>();

		for (TracerQuestionDTO qDto : dtos) {
			TracerQuestionGenerator persisted;

			if (qDto.getId() != null && existingById.containsKey(qDto.getId())) {
				// UPDATE existing parent
				TracerQuestionGenerator entity = existingById.get(qDto.getId());
				applyParentFields(entity, qDto, request, now);
				syncParentOptions(entity, qDto.getOptions(), now);
				syncSubQuestions(entity, qDto.getSubQuestions(), now);

				persisted = repository.saveAndFlush(entity);
				keepParentIds.add(entity.getId());
				log.info("UPDATE — parent id={} order={}", entity.getId(), qDto.getQuestionOrder());

			} else {
				// INSERT new parent
				TracerQuestionGenerator entity =
						buildParentEntity(qDto, request, applicationNo, now);
				addParentOptions(entity, qDto.getOptions(), now);
				addSubQuestions(entity, qDto.getSubQuestions(), now);

				persisted = repository.saveAndFlush(entity);
				log.info("INSERT — new parent order={}", qDto.getQuestionOrder());
			}

			result.add(persisted);
			registerClientIds(clientIdToDbId, qDto, persisted);
		}

		// DELETE parents no longer present in payload
		List<TracerQuestionGenerator> toDelete = existing.stream()
				.filter(q -> !keepParentIds.contains(q.getId()))
				.collect(Collectors.toList());

		if (!toDelete.isEmpty()) {
			repository.deleteAll(toDelete);
			log.info("DELETE — removed {} parent row(s) not present in payload", toDelete.size());
		}

		resolveDependencies(dtos, result, clientIdToDbId);
		repository.saveAll(result);
		repository.flush();
		log.info("UPDATE — {} parent row(s) kept/inserted under {}", result.size(), applicationNo);
		return result;
	}

	/* ---------------- parent field copy ---------------- */
	private void applyParentFields(
			TracerQuestionGenerator entity,
			TracerQuestionDTO qDto,
			TracerQuestionGeneratorRequest request,
			LocalDateTime now) {

		entity.setTracerTitle(request.getTracerTitle());
		entity.setParentTracerTypeId(request.getParentTracerTypeId());
		entity.setSubTracerTypeId(request.getSubTracerTypeId());
		entity.setQuestionTypeId(qDto.getQuestionTypeId());
		entity.setQuestionText(qDto.getQuestionText());
		entity.setIsRequired(qDto.getRequired());
		entity.setQuestionOrder(qDto.getQuestionOrder());
		entity.setRatingScale(qDto.getRatingScale());
		entity.setShowCondition(defaultIfNull(qDto.getShowCondition(), "always"));
		entity.setShowWhenOption(qDto.getShowWhenOption());
		entity.setMaxSelections(defaultIfNull(qDto.getMaxSelections(), 0));
		entity.setMaxDigits(defaultIfNull(qDto.getMaxDigits(), 0));
		entity.setMultipleTextFields(joinNonBlank(qDto.getMultipleTextFields()));
		entity.setUpdatedAt(now);
		// dependsOnQuestionId is resolved later (Pass 2)
	}

	/* ---------------- parent options diff ---------------- */
	private void syncParentOptions(
			TracerQuestionGenerator parent,
			List<String> payloadOptions,
			LocalDateTime now) {

		List<String> incoming = normalizeOptionList(payloadOptions);

		List<TracerQuestionGeneratorOptionId> existing = parent.getOptions();
		Map<String, TracerQuestionGeneratorOptionId> existingByText =
				existing.stream().collect(Collectors.toMap(
						TracerQuestionGeneratorOptionId::getOptionText,
						o -> o,
						(a, b) -> a));

		Set<String> keepTexts = new HashSet<>(incoming);

		// Delete options no longer present
		existing.removeIf(o -> !keepTexts.contains(o.getOptionText()));

		// Update order for existing, insert new
		int order = 1;
		for (String text : incoming) {
			TracerQuestionGeneratorOptionId opt = existingByText.get(text);
			if (opt != null) {
				opt.setOptionOrder(order++);
				opt.setUpdatedAt(now);
			} else {
				parent.addOption(TracerQuestionGeneratorOptionId.builder()
						.optionText(text)
						.optionOrder(order++)
						.createdAt(now)
						.updatedAt(now)
						.build());
			}
		}
	}

	/* ---------------- sub-questions diff ---------------- */
	private void syncSubQuestions(
			TracerQuestionGenerator parent,
			List<TracerSubQuestionDTO> payloadSubs,
			LocalDateTime now) {

		if (payloadSubs == null) payloadSubs = List.of();

		List<TracerSubQuestionGenerator> existing = parent.getTracerSubQuestionGenerator();
		Map<Long, TracerSubQuestionGenerator> existingById = existing.stream()
				.filter(s -> s.getId() != null)
				.collect(Collectors.toMap(TracerSubQuestionGenerator::getId, s -> s));

		Set<Long> keepSubIds = new HashSet<>();

		for (TracerSubQuestionDTO subDto : payloadSubs) {
			if (subDto.getId() != null && existingById.containsKey(subDto.getId())) {
				// UPDATE existing sub
				TracerSubQuestionGenerator sub = existingById.get(subDto.getId());
				applySubFields(sub, subDto, now);
				syncSubOptions(sub, subDto.getOptions(), now);
				keepSubIds.add(sub.getId());
				log.info("UPDATE — sub id={} order={}", sub.getId(), subDto.getSubQuestionOrder());
			} else {
				// INSERT new sub
				TracerSubQuestionGenerator sub = buildSubQuestionEntity(subDto, now);
				addSubQuestionOptions(sub, subDto.getOptions(), now);
				parent.addSubQuestion(sub);
				log.info("INSERT — new sub order={}", subDto.getSubQuestionOrder());
			}
		}

		// Delete subs not present in payload
		List<TracerSubQuestionGenerator> subsToDelete = existing.stream()
				.filter(s -> s.getId() != null && !keepSubIds.contains(s.getId()))
				.collect(Collectors.toList());

		if (!subsToDelete.isEmpty()) {
			existing.removeAll(subsToDelete);
			log.info("DELETE — removed {} sub-question(s) not present in payload", subsToDelete.size());
		}
	}

	/* ---------------- sub field copy ---------------- */
	private void applySubFields(
			TracerSubQuestionGenerator sub,
			TracerSubQuestionDTO subDto,
			LocalDateTime now) {

		sub.setQuestionText(subDto.getQuestionText());
		sub.setQuestionTypeId(subDto.getQuestionTypeId());
		sub.setIsRequired(subDto.getRequired());
		sub.setSubQuestionOrder(subDto.getSubQuestionOrder());
		sub.setRatingScale(subDto.getRatingScale());
		sub.setShowCondition(defaultIfNull(subDto.getShowCondition(), "always"));
		sub.setShowWhenOption(subDto.getShowWhenOption());
		sub.setMaxSelections(defaultIfNull(subDto.getMaxSelections(), 0));
		sub.setMaxDigits(defaultIfNull(subDto.getMaxDigits(), 0));
		sub.setMultipleTextFields(joinNonBlank(subDto.getMultipleTextFields()));
		sub.setUpdatedAt(now);
		// dependsOnQuestionId resolved later
	}

	/* ---------------- sub options diff ---------------- */
	private void syncSubOptions(
			TracerSubQuestionGenerator sub,
			List<String> payloadOptions,
			LocalDateTime now) {

		List<String> incoming = normalizeOptionList(payloadOptions);

		List<TracerSubQuestionGeneratorOptionId> existing = sub.getOptions();
		Map<String, TracerSubQuestionGeneratorOptionId> existingByText =
				existing.stream().collect(Collectors.toMap(
						TracerSubQuestionGeneratorOptionId::getOptionText,
						o -> o,
						(a, b) -> a));

		Set<String> keepTexts = new HashSet<>(incoming);

		existing.removeIf(o -> !keepTexts.contains(o.getOptionText()));

		int order = 1;
		for (String text : incoming) {
			TracerSubQuestionGeneratorOptionId opt = existingByText.get(text);
			if (opt != null) {
				opt.setOptionOrder(order++);
				opt.setUpdatedAt(now);
			} else {
				sub.addOption(TracerSubQuestionGeneratorOptionId.builder()
						.optionText(text)
						.optionOrder(order++)
						.createdAt(now)
						.updatedAt(now)
						.build());
			}
		}
	}

	/* ============================================================
	   Entity builders (shared by create + insert path in update)
	   ============================================================ */
	private TracerQuestionGenerator buildParentEntity(TracerQuestionDTO qDto, TracerQuestionGeneratorRequest request,
			String applicationNo, LocalDateTime now) {

		return TracerQuestionGenerator.builder()
				.applicationNo(applicationNo)
				.tracerTitle(request.getTracerTitle())
				.parentTracerTypeId(request.getParentTracerTypeId())
				.subTracerTypeId(request.getSubTracerTypeId())
				.questionTypeId(qDto.getQuestionTypeId())
				.questionText(qDto.getQuestionText())
				.isRequired(qDto.getRequired())
				.questionOrder(qDto.getQuestionOrder())
				.ratingScale(qDto.getRatingScale())
				.showCondition(defaultIfNull(qDto.getShowCondition(), "always"))
				.dependsOnQuestionId(null)
				.showWhenOption(qDto.getShowWhenOption())
				.maxSelections(defaultIfNull(qDto.getMaxSelections(), 0))
				.maxDigits(defaultIfNull(qDto.getMaxDigits(), 0))
				.multipleTextFields(joinNonBlank(qDto.getMultipleTextFields()))
				.createdAt(now)
				.updatedAt(now)
				.build();
	}

	private void addParentOptions(TracerQuestionGenerator entity, List<String> options, LocalDateTime now) {
		if (options == null) return;
		int order = 1;
		for (String opt : options) {
			if (opt == null || opt.trim().isEmpty()) continue;
			entity.addOption(TracerQuestionGeneratorOptionId.builder()
					.optionText(opt.trim())
					.optionOrder(order++)
					.createdAt(now)
					.updatedAt(now)
					.build());
		}
	}

	private void addSubQuestions(TracerQuestionGenerator entity, List<TracerSubQuestionDTO> subDtos, LocalDateTime now) {
		if (subDtos == null) return;
		for (TracerSubQuestionDTO subDto : subDtos) {
			TracerSubQuestionGenerator sub = buildSubQuestionEntity(subDto, now);
			addSubQuestionOptions(sub, subDto.getOptions(), now);
			entity.addSubQuestion(sub);
		}
	}

	private TracerSubQuestionGenerator buildSubQuestionEntity(TracerSubQuestionDTO subDto, LocalDateTime now) {
		return TracerSubQuestionGenerator.builder()
				.questionText(subDto.getQuestionText())
				.questionTypeId(subDto.getQuestionTypeId())
				.isRequired(subDto.getRequired())
				.subQuestionOrder(subDto.getSubQuestionOrder())
				.ratingScale(subDto.getRatingScale())
				.showCondition(defaultIfNull(subDto.getShowCondition(), "always"))
				.dependsOnQuestionId(null)
				.showWhenOption(subDto.getShowWhenOption())
				.maxSelections(defaultIfNull(subDto.getMaxSelections(), 0))
				.maxDigits(defaultIfNull(subDto.getMaxDigits(), 0))
				.multipleTextFields(joinNonBlank(subDto.getMultipleTextFields()))
				.createdAt(now)
				.updatedAt(now)
				.build();
	}

	private void addSubQuestionOptions(TracerSubQuestionGenerator sub, List<String> options, LocalDateTime now) {
		if (options == null) return;
		int order = 1;
		for (String opt : options) {
			if (opt == null || opt.trim().isEmpty()) continue;
			sub.addOption(TracerSubQuestionGeneratorOptionId.builder()
					.optionText(opt.trim())
					.optionOrder(order++)
					.createdAt(now)
					.updatedAt(now)
					.build());
		}
	}

	/* ============================================================
	   clientId → DB id mapping (Pass 1)
	   ============================================================ */
	private void registerClientIds(Map<String, Long> clientIdToDbId, TracerQuestionDTO qDto,
			TracerQuestionGenerator persisted) {

		if (qDto.getClientId() != null && !qDto.getClientId().isBlank()) {
			clientIdToDbId.put(qDto.getClientId(), persisted.getId());
		}

		if (qDto.getSubQuestions() == null) return;

		List<TracerSubQuestionGenerator> subEntities = persisted.getTracerSubQuestionGenerator();
		for (int j = 0; j < qDto.getSubQuestions().size(); j++) {
			TracerSubQuestionDTO subDto = qDto.getSubQuestions().get(j);
			if (subDto.getClientId() != null && !subDto.getClientId().isBlank()) {
				Long subDbId = subEntities.get(j).getId();
				clientIdToDbId.put(subDto.getClientId(), subDbId);
				log.info("Pass 1 — mapped sub clientId={} → dbId={}", subDto.getClientId(), subDbId);
			}
		}
	}

	/* ============================================================
	   Pass 2 — resolve dependsOnClientId → real DB ids
	   ============================================================ */
	private void resolveDependencies(List<TracerQuestionDTO> dtos, List<TracerQuestionGenerator> savedQuestions,
			Map<String, Long> clientIdToDbId) {

		for (int i = 0; i < dtos.size(); i++) {
			TracerQuestionDTO qDto = dtos.get(i);
			TracerQuestionGenerator entity = savedQuestions.get(i);

			log.info("Pass 2 — q={} clientId={} dependsOnClientId={}", i, qDto.getClientId(),
					qDto.getDependsOnClientId());

			resolveParentDependency(entity, qDto, clientIdToDbId);
			resolveSubQuestionDependencies(entity, qDto, i, clientIdToDbId);
		}
	}

	private void resolveParentDependency(TracerQuestionGenerator entity, TracerQuestionDTO qDto,
			Map<String, Long> clientIdToDbId) {

		if (qDto.getDependsOnClientId() == null || qDto.getDependsOnClientId().isBlank()) return;

		Long parentDbId = clientIdToDbId.get(qDto.getDependsOnClientId());
		log.info("  parent lookup '{}' → {}", qDto.getDependsOnClientId(), parentDbId);
		if (parentDbId != null) {
			entity.setDependsOnQuestionId(parentDbId);
		}
	}

	private void resolveSubQuestionDependencies(TracerQuestionGenerator entity, TracerQuestionDTO qDto, int parentIndex,
			Map<String, Long> clientIdToDbId) {

		if (qDto.getSubQuestions() == null || qDto.getSubQuestions().isEmpty()) return;

		List<TracerSubQuestionGenerator> subEntities = entity.getTracerSubQuestionGenerator();
		for (int j = 0; j < qDto.getSubQuestions().size(); j++) {
			TracerSubQuestionDTO subDto = qDto.getSubQuestions().get(j);
			log.info("  sub q={}.{} clientId={} dependsOnClientId={}", parentIndex, j, subDto.getClientId(),
					subDto.getDependsOnClientId());
			resolveSingleSubDependency(subEntities.get(j), subDto, clientIdToDbId);
		}
	}

	private void resolveSingleSubDependency(TracerSubQuestionGenerator sub, TracerSubQuestionDTO subDto,
			Map<String, Long> clientIdToDbId) {

		if (subDto.getDependsOnClientId() == null || subDto.getDependsOnClientId().isBlank()) return;

		Long parentDbId = clientIdToDbId.get(subDto.getDependsOnClientId());
		log.info("    sub lookup '{}' → {}", subDto.getDependsOnClientId(), parentDbId);
		if (parentDbId != null) {
			sub.setDependsOnQuestionId(parentDbId);
		}
	}

	/* ============================================================
	   Small shared helpers
	   ============================================================ */
	private static String defaultIfNull(String value, String defaultValue) {
		return value != null ? value : defaultValue;
	}

	private static Integer defaultIfNull(Integer value, Integer defaultValue) {
		return value != null ? value : defaultValue;
	}

	private List<String> normalizeOptionList(List<String> payloadOptions) {
		if (payloadOptions == null) return List.of();
		return payloadOptions.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}

	private String joinNonBlank(List<String> list) {
		if (list == null || list.isEmpty()) return null;
		String joined = list.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.joining(","));
		return joined.isEmpty() ? null : joined;
	}

	// ─────────────────────────────────────────────────────────────────────
	// Remaining service methods (unchanged)
	// ─────────────────────────────────────────────────────────────────────
	@Override
	public List<ObjectNode> getTracerDetailsByApplicationNo(String applicationNo) {
		return objectTojson._toJson(repository.getTracerDetailsByApplicationNo(applicationNo));
	}

	@Override
	public List<ObjectNode> getTracerAllApplications() {
		return objectTojson._toJson(repository.getTracerAllApplications());
	}

	@Override
	@Transactional
	public ResponseEntity<?> sendTraineeTracerSurvey(TracerSendRequestDTO request) {
		try {
			if (request.getSelectedTrainees() == null || request.getSelectedTrainees().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "No trainees selected", "timestamp", LocalDateTime.now()));
			}
			String applicationNo = generateApplicationNumber.generateApplicationNumber(45);

			for (TraineeDTO trainee : request.getSelectedTrainees()) {
				String UniqueId = generateTracerUniqueId.generateTracerUniqueId(applicationNo);
				String tracerUrl = "http://localhost:5173/tracer/trainee-survey/" + UniqueId;
				TracerSurveySendDetails surveyDetail = TracerSurveySendDetails.builder()
						.applicationNo(applicationNo)
						.questionApplicationNo(request.getApplicationNo())
						.applicationName(trainee.getName())
						.mobileNo(trainee.getMobileNo())
						.emailId(trainee.getEmail())
						.statusId(1)
						.uniqueId(UniqueId)
						.tracerUrl(tracerUrl)
						.parentTracerTypeId(request.getParentTracerTypeId())
						.subTracerTypeId(request.getSubTracerTypeId())
						.createdAt(LocalDateTime.now())
						.build();

				tracerSurveySendDetailsRepository.save(surveyDetail);
				log.info("Saved trainee survey for: {} with application no: {}", trainee.getName(),
						trainee.getApplicationNo());
			}

			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "message",
					"Trainee tracer survey sent successfully", "timestamp", LocalDateTime.now()));

		} catch (Exception e) {
			log.error("Error sending trainee tracer survey", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message",
					"Failed to send trainee tracer survey", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> sendEmployerTracerSurvey(TracerSendRequestDTO request) {
		try {
			if (request.getSelectedEmployers() == null || request.getSelectedEmployers().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "No employers selected", "timestamp", LocalDateTime.now()));
			}
			String applicationNo = generateApplicationNumber.generateApplicationNumber(45);

			for (EmployerDTO employer : request.getSelectedEmployers()) {
				String UniqueId = generateTracerUniqueId.generateTracerUniqueId(applicationNo);
				String tracerUrl = "http://localhost:5173/tracer/employer-survey/" + UniqueId;
				TracerSurveySendDetails surveyDetail = TracerSurveySendDetails.builder()
						.applicationNo(applicationNo)
						.questionApplicationNo(request.getApplicationNo())
						.applicationName(employer.getName())
						.mobileNo(employer.getMobileNo())
						.emailId(employer.getEmail())
						.uniqueId(UniqueId)
						.tracerUrl(tracerUrl)
						.statusId(1)
						.parentTracerTypeId(request.getParentTracerTypeId())
						.subTracerTypeId(request.getSubTracerTypeId())
						.createdAt(LocalDateTime.now())
						.build();

				tracerSurveySendDetailsRepository.save(surveyDetail);
				log.info("Saved employer survey for: {} (ID: {}, Contact: {})",
						employer.getName(), employer.getId(), employer.getContactPerson());
			}

			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "message",
					"Employer tracer survey sent successfully", "timestamp", LocalDateTime.now()));

		} catch (Exception e) {
			log.error("Error sending employer tracer survey", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Failed to send employer tracer survey", "error",
							e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public TracerSurveySendDetails getSurveyByUniqueId(String uniqueId) {
		return tracerSurveySendDetailsRepository.findByUniqueId(uniqueId).orElse(null);
	}

	@Override
	@Transactional
	public List<TracerSurveyResponseDetails> saveSurveyResponses(
	        String uniqueId,
	        List<ResponseItem> responses) {

	    // 1) Look up the send row so we can store the traceable applicationNo
	    TracerSurveySendDetails sendDetails =
	            tracerSurveySendDetailsRepository.findByUniqueId(uniqueId)
	                    .orElseThrow(() -> new IllegalArgumentException(
	                            "Invalid survey link: " + uniqueId));

	    // 2) Duplicate-submission guard — one submission per uniqueId
	    boolean alreadySubmitted =
	            tracerSurveyResponseDetailsRepository.existsByUniqueId(uniqueId);
	    if (alreadySubmitted) {
	        throw new IllegalStateException(
	                "Survey " + uniqueId + " has already been submitted.");
	    }

	    // 3) Determine the tracer application number
	    //    questionApplicationNo is what was set when the survey was sent
	    String tracerApplicationNo = sendDetails.getQuestionApplicationNo();
	    if (tracerApplicationNo == null || tracerApplicationNo.isBlank()) {
	        tracerApplicationNo = sendDetails.getApplicationNo();
	    }

	    LocalDateTime submittedDateTime = LocalDateTime.now();
	    ObjectMapper objectMapper = new ObjectMapper();

	    List<TracerSurveyResponseDetails> savedResponses = new ArrayList<>();

	    for (ResponseItem response : responses) {
	        if (response == null || response.getQuestionId() == null) {
	            continue; // skip empty responses defensively
	        }

	        // Serialize the response value (String, List, Map, etc.)
	        String serializedResponse;
	        try {
	            serializedResponse = objectMapper.writeValueAsString(response.getResponse());
	        } catch (Exception e) {
	            serializedResponse = String.valueOf(response.getResponse());
	        }

	        Integer isSubQuestionValue =
	                Boolean.TRUE.equals(response.getIsSubQuestion()) ? 1 : 0;

	        TracerSurveyResponseDetails entity = TracerSurveyResponseDetails.builder()
	                .uniqueId(uniqueId)                                  // <-- NEW
	                .applicationNo(tracerApplicationNo)                  // <-- the TRACER app no
	                .questionId(response.getQuestionId())
	                .responseId(serializedResponse)
	                .isSubQuestion(isSubQuestionValue)
	                .statusId(1)
	                .createdAt(submittedDateTime)
	                .build();

	        savedResponses.add(tracerSurveyResponseDetailsRepository.save(entity));
	    }

	    // 4) Mark the send row as completed
	    sendDetails.setStatusId(2); // 2 = submitted
	    tracerSurveySendDetailsRepository.save(sendDetails);

	    return savedResponses;
	}

	@Override
	public Set<Long> getQuestionIdsByApplicationNo(String applicationNo) {
		return new HashSet<>(
				tracerQuestionGeneratorRepository.findQuestionIdsByApplicationNo(applicationNo));
	}
}