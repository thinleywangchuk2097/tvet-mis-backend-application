package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.BirmsPaymentRequestdto;
import com.moesd.tvet.mis.backend.application.model.BirmsPayment;
import com.moesd.tvet.mis.backend.application.repository.BirmsPaymentRepository;
import com.moesd.tvet.mis.backend.application.service.BirmsPaymentService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class BirmsPaymentServiceImpl implements BirmsPaymentService {

	@Value("${birms.token-api}")
	private String tokenUrl;

	@Value("${birms.client-key}")
	private String username;

	@Value("${birms.client-secret}")
	private String password;

	@Value("${birms.payment-advice-create-url}")
	private String paymentUrl;

	@Value("${birms.platform}")
	private String platform;

	@Value("${birms.agency-code}")
	private String agencyCode;

	@Value("${birms.description}")
	private String description;

	@Value("${birms.payment-cancel-url}")
	private String cancelUrl;

	@Value("${birms.receipt-encoded-pdf-url}")
	private String receiptUrl;
	
	@Autowired
	private RestTemplate restTemplate;
	
	private final BirmsPaymentRepository birmsPaymentRepository;
	private final ObjectToJson objectTojson;
	

	// newly added
	@Override
	public ResponseEntity<?> createToken() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> body = new HashMap<>();
			body.put("username", username);
			body.put("password", password);

			HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

			System.out.println("RAW RESPONSE: " + response.getBody());

			// Parse JSON from external API
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(response.getBody());

			// RETURN EXACT SAME STRUCTURE
			return ResponseEntity.ok(jsonNode);

		} catch (Exception e) {
			e.printStackTrace();

			Map<String, Object> error = new HashMap<>();
			error.put("statusCode", 500);
			error.put("message", "Token generation failed");
			error.put("error", e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	private String getAccessToken() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> body = new HashMap<>();
			body.put("username", username);
			body.put("password", password);

			HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());

			return json.path("content").path("tokenDto").path("accessToken").asText();

		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String createPaymentAdvice(BirmsPaymentRequestdto req) {

		String token = getAccessToken();

		if (token == null) {
			return "Token creation failed due to incorrect user credentials provided during the authentication process or api is down right now";
		}

		return createPaymentAdviceWithRequest(token, req);
	}

	private String createPaymentAdviceWithRequest(String token, BirmsPaymentRequestdto req) {
		try {
			// Set headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + token); // Append the access token
			// Create the pay load dynamically with request body data and static values
			String payload = "{\n" + "  \"platform\": \"" + platform + "\",\n" + "  \"refNo\": \"TS" + req.getRefNo()
					+ "\",\n" + "  \"taxPayerNo\": \"" + req.getTaxPayerNo() + "\",\n" + "  \"taxPayerDocumentNo\": \""
					+ req.getTaxPayerDocumentNo() + "\",\n" + "  \"paymentRequestDate\": \""
					+ req.getPaymentRequestDate() + "\",\n" + "  \"agencyCode\": \"" + agencyCode + "\",\n"
					+ "  \"payerEmail\": \"" + req.getTaxPayerEmail() + "\",\n" + "  \"mobileNo\": \""
					+ req.getTaxPayerMobileNo() + "\",\n" + "  \"totalPayableAmount\": \"" + req.getTotalPayableAmount()
					+ "\",\n" + "  \"paymentDueDate\": "
					+ (req.getPaymentDueDate() != null ? "\"" + req.getPaymentDueDate() + "\"" : null) + ",\n"
					+ "  \"taxPayerName\": \"" + req.getTaxPayerName() + "\",\n" + "  \"paymentLists\": [\n" + "    {\n"
					+ "      \"serviceCode\": \"" + req.getServiceCode() + "\",\n" + "      \"description\": \""
					+ description + "\",\n" + "      \"payableAmount\": \"" + req.getTotalPayableAmount() + "\"\n"
					+ "    }\n" + "  ]\n" + "}";

			// Build the request
			HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
			try {
				ResponseEntity<String> response = restTemplate.exchange(paymentUrl, HttpMethod.POST, requestEntity,
						String.class);
				if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
					// Parse response body (assuming it's JSON)
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode responseBody = objectMapper.readTree(response.getBody());
					JsonNode content = responseBody.path("content");
					String paymentAdviceNo = content.path("paymentAdviceNo").asText();
					// Check if record already exists
					Optional<BirmsPayment> existingRecord = birmsPaymentRepository
							.findByPaymentAdviceNo(paymentAdviceNo);
					if (existingRecord.isPresent()) {
						return "Payment advice already exists with paymentAdviceNo: " + paymentAdviceNo;
					}
					BirmsPayment birmsPaymentDetails = new BirmsPayment();

					birmsPaymentDetails.setPlatform(content.path("platform").asText());
					birmsPaymentDetails.setTaxPayerNo(content.path("taxPayerNo").asText());
					birmsPaymentDetails.setRefNo(content.path("refNo").asText());
					birmsPaymentDetails.setApplicationNo(req.getRefNo());
					birmsPaymentDetails.setAgencyCode(content.path("agencyCode").asText());
					birmsPaymentDetails.setPayerEmail(req.getTaxPayerEmail());
					birmsPaymentDetails.setTaxPayerName(content.path("taxPayerName").asText());
					birmsPaymentDetails.setMobileNo(content.path("mobileNo").asText());
					// birmsPaymentDetails.setPaymentStatus(content.path("paymentStatus").asText());
					birmsPaymentDetails.setPaymentStatus("pending");
					birmsPaymentDetails.setRedirectUrl(content.path("redirectUrl").asText());
					birmsPaymentDetails.setServiceCode(req.getServiceCode());
					birmsPaymentDetails.setDescription(description);
					birmsPaymentDetails.setTaxPayerDocumentNo(content.path("taxPayerDocumentNo").asText());
					birmsPaymentDetails.setPaymentRequestDate(content.path("paymentRequestDate").asText());
					// birmsPaymentDetails.setPaymentDueDate(content.path("paymentDueDate").asText());
					birmsPaymentDetails.setPaymentDueDate(req.getPaymentDueDate());
					birmsPaymentDetails.setTotalPayableAmount(content.path("totalPayableAmount").asText());
					birmsPaymentDetails.setPaymentAdviceNo(content.path("paymentAdviceNo").asText());
					birmsPaymentDetails.setCreatedAt(new Date());

					birmsPaymentRepository.save(birmsPaymentDetails);

				}

				return response.getBody();

			} catch (HttpClientErrorException e) {
				return "Client error: " + e.getMessage();
			}
		} catch (Exception e) {
			log.error("Error creating payment advice", e);
			return "Error creating payment advice";
		}
	}

	@Override
	public BirmsPayment updatePaymentStatus(BirmsPaymentRequestdto dto) {
		
		Optional<BirmsPayment> data = birmsPaymentRepository.findByPaymentAdviceNo(dto.getPaymentAdviceNo());

		if (data.isPresent()) {
			BirmsPayment p = data.get();
			p.setPaymentStatus("paid");
			p.setReceiptNo(dto.getReceiptList().get(0).getReceiptNo());
			p.setIssuingBank(dto.getIssuingBank());
			p.setJournalNo(dto.getJournalNo());
			p.setPaymentMode(dto.getPaymentMode());
			p.setUpdatedAt(new Date());
			return birmsPaymentRepository.save(p);
		}
		return null;
	}

	@Override
	public ResponseEntity<String> getPaymentReceiptDetails(String receiptNo) {
		try {
			// First, check if the payment exists and update status if needed
			Optional<BirmsPayment> paymentOpt = birmsPaymentRepository.findByReceiptNo(receiptNo);
			if (paymentOpt.isPresent()) {
				BirmsPayment payment = paymentOpt.get();
				if (!"paid".equals(payment.getPaymentStatus())) {
					payment.setPaymentStatus("paid");
					payment.setUpdatedAt(new Date());
					birmsPaymentRepository.save(payment);
				}
			}

			String token = getAccessToken();

			if (token == null) {
				log.error("Failed to get access token for receipt: {}", receiptNo);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get access token");
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + token);

			HttpEntity<String> entity = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(receiptUrl, HttpMethod.GET, entity, String.class,
					receiptNo);

			// Log successful retrieval
			log.info("Successfully fetched payment receipt for receiptNo: {}", receiptNo);

			// Return the response from the external API
			return ResponseEntity.ok(response.getBody());

		} catch (HttpClientErrorException e) {
			log.error("Client error while fetching payment receipt for {}: {}", receiptNo, e.getMessage());
			return ResponseEntity.status(e.getStatusCode()).body("Client error: " + e.getMessage());
		} catch (Exception e) {
			log.error("Error fetching payment receipt details for receiptNo: {}", receiptNo, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> makePaymentCancel(BirmsPaymentRequestdto dto) {
		try {
			String token = getAccessToken();

			if (token == null) {
				log.error("Failed to get access token for payment cancellation");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to get access token");
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + token);

			Map<String, Object> payload = new HashMap<>();
			payload.put("paymentAdviceNumber", dto.getPaymentAdviceNo());
			payload.put("reason", dto.getCancelledReason() != null ? dto.getCancelledReason() : "Cancelled by user");
			payload.put("cancelledBy", dto.getCancelledBy() != null ? dto.getCancelledBy() : "System");
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

			ResponseEntity<String> response = restTemplate.exchange(cancelUrl, HttpMethod.POST, entity, String.class);

			// Parse the JSON response
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(response.getBody());

			log.info("Payment cancellation response rootNode: {}", rootNode);

			int statusCode = rootNode.path("statusCode").asInt();

			log.info("Payment cancellation response status code: {}", statusCode);

			if (statusCode == 200) {
				// Update payment status in local database
				Optional<BirmsPayment> paymentOpt = birmsPaymentRepository
						.findByPaymentAdviceNo(dto.getPaymentAdviceNo());

				if (paymentOpt.isPresent()) {
					BirmsPayment payment = paymentOpt.get();
					payment.setPaymentStatus("cancelled");
					payment.setCancelledReason(dto.getCancelledReason());
					payment.setCancelledBy(dto.getCancelledBy());
					payment.setUpdatedAt(new Date());
					birmsPaymentRepository.save(payment);
					log.info("Payment cancelled successfully for advice no: {}", dto.getPaymentAdviceNo());
				} else {
					log.warn("Payment record not found for advice no: {}", dto.getPaymentAdviceNo());
				}
			}

			// Return the response from the external API
			return ResponseEntity.ok(response.getBody());

		} catch (HttpClientErrorException e) {
			log.error("Client error while cancelling payment: {}", e.getMessage());
			return ResponseEntity.status(e.getStatusCode()).body("Client error: " + e.getMessage());
		} catch (Exception e) {
			log.error("Error cancelling payment for advice no: {}", dto.getPaymentAdviceNo(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
		}
	}

	@Override
	public BirmsPayment updatePaymentCheckBounce(BirmsPaymentRequestdto dto) {
		try {
			// Retrieve the entity using the receiptNo
			Optional<BirmsPayment> paymentData = birmsPaymentRepository.findByReceiptNo(dto.getReceiptNo());

			if (paymentData.isPresent()) {
				// Get the entity
				BirmsPayment existingPaymentData = paymentData.get();

				// Update the entity fields with data from DTO
				existingPaymentData.setCancelledReason(dto.getCancelledReason());
				existingPaymentData.setIssuingBank(dto.getIssuingBank());
				existingPaymentData.setPaymentStatus("chequebounce");
				existingPaymentData.setReceiptStatus(dto.getReceiptStatus());
				existingPaymentData.setRemarks(dto.getRemarks());
				existingPaymentData.setCancelledDate(dto.getCancelledDate());
				existingPaymentData.setUpdatedAt(new Date());

				// Save the updated entity
				BirmsPayment savedPayment = birmsPaymentRepository.save(existingPaymentData);
				log.info("Payment check bounce updated successfully for receiptNo: {}", dto.getReceiptNo());

				return savedPayment;
				
			} else {
				log.warn("Payment record not found with receiptNo: {}", dto.getReceiptNo());
				return null;
			}
		} catch (Exception e) {
			log.error("Error updating payment check bounce for receiptNo: {}", dto.getReceiptNo(), e);
			return null;
		}
	}

	@Override
	public Optional<BirmsPayment> getPaymentByApplicationNo(String application_no) {
		return birmsPaymentRepository.findByApplicationNo(application_no);
	}

	@Override
	public List<ObjectNode> getAllPaymentDetails() {
		List<Tuple> resultList = birmsPaymentRepository.getAllPaymentDetails();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getByUserPenaltyApplicationDetails(String user_id) {
		return new ArrayList<>();
	}

	@Override
	public List<ObjectNode> getCourseByInstituteId(String instituteId) {
		List<Tuple> resultList = birmsPaymentRepository.getCourseByInstituteId(instituteId);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public Optional<BirmsPayment> getPaymentByPaymentAdviceNo(String paymentAdviceNo) {
		return birmsPaymentRepository.findByPaymentAdviceNo(paymentAdviceNo);
	}

}