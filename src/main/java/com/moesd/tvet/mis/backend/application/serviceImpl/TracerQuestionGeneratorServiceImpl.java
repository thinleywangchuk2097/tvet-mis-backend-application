package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

	@Override
	public List<ObjectNode> getTracerQuestionDropdownType() {
		return objectTojson._toJson(tracerQuestionTypeDropdownRepository.getTracerQuestionDropdownType());
	}

	@Override
	public List<ObjectNode> getParentTracerTypes() {
		return objectTojson._toJson(tracerQuestionTypeDropdownRepository.getParentTracerTypes());
	}

	// ─────────────────────────────────────────────────────────────────────
	// saveTracerQuestions — refactored from a "Brain Method"
	// ─────────────────────────────────────────────────────────────────────
	@Override
	@Transactional
	public List<TracerQuestionGenerator> saveTracerQuestions(TracerQuestionGeneratorRequest request) {
		LocalDateTime now = LocalDateTime.now();
		String applicationNo = generateApplicationNumber.generateApplicationNumber(44);
		List<TracerQuestionDTO> dtos = request.getQuestions();

		Map<String, Long> clientIdToDbId = new HashMap<>();
		List<TracerQuestionGenerator> savedQuestions = saveParentQuestionsWithChildren(dtos, request, applicationNo,
				now, clientIdToDbId);

		resolveDependencies(dtos, savedQuestions, clientIdToDbId);

		repository.saveAll(savedQuestions);
		repository.flush();

		log.info("saveTracerQuestions — completed. {} parent row(s) saved.", savedQuestions.size());
		return savedQuestions;
	}

	/**
	 * PASS 1 — Save parents + sub-questions with no dependencies yet. Builds the
	 * clientId → DB id map for BOTH levels.
	 */
	private List<TracerQuestionGenerator> saveParentQuestionsWithChildren(List<TracerQuestionDTO> dtos,
			TracerQuestionGeneratorRequest request, String applicationNo, LocalDateTime now,
			Map<String, Long> clientIdToDbId) {

		List<TracerQuestionGenerator> savedQuestions = new ArrayList<>();

		for (TracerQuestionDTO qDto : dtos) {
			TracerQuestionGenerator entity = buildParentEntity(qDto, request, applicationNo, now);
			addParentOptions(entity, qDto.getOptions(), now);
			addSubQuestions(entity, qDto.getSubQuestions(), now);

			TracerQuestionGenerator persisted = repository.saveAndFlush(entity);
			savedQuestions.add(persisted);

			registerClientIds(clientIdToDbId, qDto, persisted);
		}

		log.info("Pass 1 — full clientIdToDbId map = {}", clientIdToDbId);
		return savedQuestions;
	}

	private TracerQuestionGenerator buildParentEntity(TracerQuestionDTO qDto, TracerQuestionGeneratorRequest request,
			String applicationNo, LocalDateTime now) {

		return TracerQuestionGenerator.builder().applicationNo(applicationNo).tracerTitle(request.getTracerTitle())
				.parentTracerTypeId(request.getParentTracerTypeId()).subTracerTypeId(request.getSubTracerTypeId())
				.questionTypeId(qDto.getQuestionTypeId()).questionText(qDto.getQuestionText())
				.isRequired(qDto.getRequired()).questionOrder(qDto.getQuestionOrder())
				.ratingScale(qDto.getRatingScale()).showCondition(defaultIfNull(qDto.getShowCondition(), "always"))
				.dependsOnQuestionId(null).showWhenOption(qDto.getShowWhenOption())
				.maxSelections(defaultIfNull(qDto.getMaxSelections(), 0))
				.multipleTextFields(joinNonBlank(qDto.getMultipleTextFields())).createdAt(now).updatedAt(now).build();
	}

	private void addParentOptions(TracerQuestionGenerator entity, List<String> options, LocalDateTime now) {

		if (options == null)
			return;
		int order = 1;
		for (String opt : options) {
			if (opt == null || opt.trim().isEmpty())
				continue;
			entity.addOption(TracerQuestionGeneratorOptionId.builder().optionText(opt.trim()).optionOrder(order++)
					.createdAt(now).updatedAt(now).build());
		}
	}

	private void addSubQuestions(TracerQuestionGenerator entity, List<TracerSubQuestionDTO> subDtos,
			LocalDateTime now) {

		if (subDtos == null)
			return;
		for (TracerSubQuestionDTO subDto : subDtos) {
			TracerSubQuestionGenerator sub = buildSubQuestionEntity(subDto, now);
			addSubQuestionOptions(sub, subDto.getOptions(), now);
			entity.addSubQuestion(sub);
		}
	}

	private TracerSubQuestionGenerator buildSubQuestionEntity(TracerSubQuestionDTO subDto, LocalDateTime now) {

		return TracerSubQuestionGenerator.builder().questionText(subDto.getQuestionText())
				.questionTypeId(subDto.getQuestionTypeId()).isRequired(subDto.getRequired())
				.subQuestionOrder(subDto.getSubQuestionOrder()).ratingScale(subDto.getRatingScale())
				.showCondition(defaultIfNull(subDto.getShowCondition(), "always")).dependsOnQuestionId(null)
				.showWhenOption(subDto.getShowWhenOption()).maxSelections(defaultIfNull(subDto.getMaxSelections(), 0))
				.multipleTextFields(joinNonBlank(subDto.getMultipleTextFields())).createdAt(now).updatedAt(now).build();
	}

	private void addSubQuestionOptions(TracerSubQuestionGenerator sub, List<String> options, LocalDateTime now) {

		if (options == null)
			return;
		int order = 1;
		for (String opt : options) {
			if (opt == null || opt.trim().isEmpty())
				continue;
			sub.addOption(TracerSubQuestionGeneratorOptionId.builder().optionText(opt.trim()).optionOrder(order++)
					.createdAt(now).updatedAt(now).build());
		}
	}

	private void registerClientIds(Map<String, Long> clientIdToDbId, TracerQuestionDTO qDto,
			TracerQuestionGenerator persisted) {

		if (qDto.getClientId() != null && !qDto.getClientId().isBlank()) {
			clientIdToDbId.put(qDto.getClientId(), persisted.getId());
		}

		if (qDto.getSubQuestions() == null)
			return;
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

	/**
	 * PASS 2 — Resolve dependsOnClientId → real DB ids.
	 */
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

		if (qDto.getDependsOnClientId() == null || qDto.getDependsOnClientId().isBlank())
			return;

		Long parentDbId = clientIdToDbId.get(qDto.getDependsOnClientId());
		log.info("  parent lookup '{}' → {}", qDto.getDependsOnClientId(), parentDbId);
		if (parentDbId != null) {
			entity.setDependsOnQuestionId(parentDbId);
		}
	}

	private void resolveSubQuestionDependencies(TracerQuestionGenerator entity, TracerQuestionDTO qDto, int parentIndex,
			Map<String, Long> clientIdToDbId) {

		if (qDto.getSubQuestions() == null || qDto.getSubQuestions().isEmpty())
			return;

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

		if (subDto.getDependsOnClientId() == null || subDto.getDependsOnClientId().isBlank())
			return;

		Long parentDbId = clientIdToDbId.get(subDto.getDependsOnClientId());
		log.info("    sub lookup '{}' → {}", subDto.getDependsOnClientId(), parentDbId);
		if (parentDbId != null) {
			sub.setDependsOnQuestionId(parentDbId);
		}
	}

	// ─────────────────────────────────────────────────────────────────────
	// Small shared helpers
	// ─────────────────────────────────────────────────────────────────────
	private static String defaultIfNull(String value, String defaultValue) {
		return value != null ? value : defaultValue;
	}

	private static Integer defaultIfNull(Integer value, Integer defaultValue) {
		return value != null ? value : defaultValue;
	}

	/**
	 * Joins a list of strings into a comma-separated string. Blank / null entries
	 * are dropped. Returns null if nothing remains.
	 */
	private String joinNonBlank(List<String> list) {
		if (list == null || list.isEmpty())
			return null;
		String joined = list.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
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
				TracerSurveySendDetails surveyDetail = TracerSurveySendDetails.builder().applicationNo(applicationNo)
						.questionApplicationNo(request.getApplicationNo()).applicationName(trainee.getName())
						.mobileNo(trainee.getMobileNo()).emailId(trainee.getEmail()).statusId(1).uniqueId(UniqueId)
						.tracerUrl(tracerUrl).parentTracerTypeId(request.getParentTracerTypeId())
						.subTracerTypeId(request.getSubTracerTypeId()).createdAt(LocalDateTime.now()).build();

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
				TracerSurveySendDetails surveyDetail = TracerSurveySendDetails.builder().applicationNo(applicationNo)
						.questionApplicationNo(request.getApplicationNo()).applicationName(employer.getName())
						.mobileNo(employer.getMobileNo()).emailId(employer.getEmail()).uniqueId(UniqueId)
						.tracerUrl(tracerUrl).statusId(1).parentTracerTypeId(request.getParentTracerTypeId())
						.subTracerTypeId(request.getSubTracerTypeId()).createdAt(LocalDateTime.now()).build();

				tracerSurveySendDetailsRepository.save(surveyDetail);

				log.info("Saved employer survey for: {} (ID: {}, Contact: {})", employer.getName(), employer.getId(),
						employer.getContactPerson());
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
	public List<TracerSurveyResponseDetails> saveSurveyResponses(String applicationNo, List<ResponseItem> responses) {

		List<TracerSurveyResponseDetails> savedResponses = new ArrayList<>();
		LocalDateTime submittedDateTime = LocalDateTime.now();
		ObjectMapper objectMapper = new ObjectMapper();

		for (ResponseItem response : responses) {
			String responseId;
			try {
				responseId = objectMapper.writeValueAsString(response.getResponse());
			} catch (Exception e) {
				responseId = String.valueOf(response.getResponse());
			}

			Integer isSubQuestionValue = response.getIsSubQuestion() != null && response.getIsSubQuestion() ? 1 : 0;

			TracerSurveyResponseDetails responseDetails = TracerSurveyResponseDetails.builder()
					.applicationNo(applicationNo).questionId(response.getQuestionId()).responseId(responseId)
					.isSubQuestion(isSubQuestionValue).statusId(1).createdAt(submittedDateTime).build();

			savedResponses.add(tracerSurveyResponseDetailsRepository.save(responseDetails));
		}

		return savedResponses;
	}
}