package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import jakarta.persistence.Tuple;
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
    private final TracerSurveyResponseDetailsRepository tracerSurveyResponseDetailsRepository ;
	@Override
	public List<ObjectNode> getTracerQuestionDropdownType() {
		List<Tuple> resultList = tracerQuestionTypeDropdownRepository.getTracerQuestionDropdownType();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getParentTracerTypes() {
		List<Tuple> resultList = tracerQuestionTypeDropdownRepository.getParentTracerTypes();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public List<TracerQuestionGenerator> saveTracerQuestions(TracerQuestionGeneratorRequest request) {
		LocalDateTime now = LocalDateTime.now();
		// Generate application number (same for all questions in this tracer)
		String applicationNo = generateApplicationNumber.generateApplicationNumber(44);

		List<TracerQuestionGenerator> savedQuestions = new ArrayList<>();

		// Create a separate TracerQuestionGenerator for each question
		for (TracerQuestionDTO qDto : request.getQuestions()) {
			// Create new entity for each parent question
			TracerQuestionGenerator tracerQuestionGenerator = TracerQuestionGenerator.builder()
					.applicationNo(applicationNo).tracerTitle(request.getTracerTitle())
					.parentTracerTypeId(request.getParentTracerTypeId()).subTracerTypeId(request.getSubTracerTypeId())
					.questionTypeId(qDto.getQuestionTypeId()).questionText(qDto.getQuestionText())
					.isRequired(qDto.getRequired()) // This should be 1 or 0 from front end
					.questionOrder(qDto.getQuestionOrder()).ratingScale(qDto.getRatingScale()).createdAt(now)
					.updatedAt(now).build();

			// Add options for main question (for radio, check box, drop down, multiSelect)
			if (qDto.getOptions() != null && !qDto.getOptions().isEmpty()) {
				for (int i = 0; i < qDto.getOptions().size(); i++) {
					String opt = qDto.getOptions().get(i);
					if (opt != null && !opt.trim().isEmpty()) {
						TracerQuestionGeneratorOptionId option = TracerQuestionGeneratorOptionId.builder()
								.optionText(opt).optionOrder(i + 1).createdAt(now).updatedAt(now)
								.tracerQuestionGenerator(tracerQuestionGenerator).build();
						tracerQuestionGenerator.addOption(option);
					}
				}
			}

			// Add sub-questions if any
			if (qDto.getSubQuestions() != null && !qDto.getSubQuestions().isEmpty()) {
				for (TracerSubQuestionDTO subDto : qDto.getSubQuestions()) {
					TracerSubQuestionGenerator subQuestion = TracerSubQuestionGenerator.builder()
							.questionText(subDto.getQuestionText()).questionTypeId(subDto.getQuestionTypeId())
							.isRequired(subDto.getRequired()) // This should be 1 or 0 from frontend
							.subQuestionOrder(subDto.getSubQuestionOrder()).ratingScale(subDto.getRatingScale())
							.createdAt(now).updatedAt(now).tracerQuestionGenerator(tracerQuestionGenerator).build();

					// Add options for sub-question
					if (subDto.getOptions() != null && !subDto.getOptions().isEmpty()) {
						for (int i = 0; i < subDto.getOptions().size(); i++) {
							String opt = subDto.getOptions().get(i);
							if (opt != null && !opt.trim().isEmpty()) {
								TracerSubQuestionGeneratorOptionId option = TracerSubQuestionGeneratorOptionId.builder()
										.optionText(opt).optionOrder(i + 1).createdAt(now).updatedAt(now)
										.tracerSubQuestionGenerator(subQuestion).build();
								subQuestion.addOption(option);
							}
						}
					}

					tracerQuestionGenerator.addSubQuestion(subQuestion);
				}
			}

			// Save each question individually
			savedQuestions.add(repository.save(tracerQuestionGenerator));
		}

		return savedQuestions;
	}

	@Override
	public List<ObjectNode> getTracerDetailsByApplicationNo(String application_no) {
		List<Tuple> resultList = repository.getTracerDetailsByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getTracerAllApplications() {
		List<Tuple> resultList = repository.getTracerAllApplications();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> sendTraineeTracerSurvey(TracerSendRequestDTO request) {
		try {
			if (request.getSelectedTrainees() == null || request.getSelectedTrainees().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("message", "No trainees selected", "timestamp", LocalDateTime.now()));
			}
			// Generate application number (same for all questions in this tracer)
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
			// Generate application number (same for all questions in this tracer)
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
    public List<TracerSurveyResponseDetails> saveSurveyResponses(String applicationNo, 
            List<ResponseItem> responses) {
        
        List<TracerSurveyResponseDetails> savedResponses = new ArrayList<>();
        
        // Use server current time
        LocalDateTime submittedDateTime = LocalDateTime.now();
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (ResponseItem response : responses) {
            String responseId;
            try {
                responseId = objectMapper.writeValueAsString(response.getResponse());
            } catch (Exception e) {
                responseId = String.valueOf(response.getResponse());
            }
            
            // Convert Boolean isSubQuestion to Integer (1 for true, 0 for false)
            Integer isSubQuestionValue = response.getIsSubQuestion() != null && response.getIsSubQuestion() ? 1 : 0;
            
            TracerSurveyResponseDetails responseDetails = TracerSurveyResponseDetails.builder()
                    .applicationNo(applicationNo)
                    .questionId(response.getQuestionId())
                    .responseId(responseId)
                    .isSubQuestion(isSubQuestionValue)
                    .statusId(1)
                    .createdAt(submittedDateTime)
                    .build();
            
            savedResponses.add(tracerSurveyResponseDetailsRepository.save(responseDetails));
        }
        
        return savedResponses;
    }

}
