package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class BirmsPaymentServiceImpl implements BirmsPaymentService {

	@Value("${birms.token-api}")
	private String tokenUrl;

	@Value("${birms.clientkey}")
	private String username;

	@Value("${birms.clientSecret}")
	private String password;

	@Value("${birms.paymentAdviceCreateUrl}")
	private String paymentUrl;

	@Value("${birms.platform}")
	private String platform;

	@Value("${birms.agencyCode}")
	private String agencyCode;

	@Value("${birms.serviceCode}")
	private String serviceCode;

	@Value("${birms.description}")
	private String description;
	
	private final BirmsPaymentRepository birmsPaymentRepository;
	private final RestTemplate restTemplate = new RestTemplate();
	
	
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
			//Create the pay load dynamically with request body data and static values
			String payload = "{\n" +
			        "  \"platform\": \"" + platform + "\",\n" +
			        "  \"refNo\": \"TS" + req.getRefNo() + "\",\n" +
			        "  \"taxPayerNo\": \"" + req.getTaxPayerNo() + "\",\n" +
			        "  \"taxPayerDocumentNo\": \"" + req.getTaxPayerDocumentNo() + "\",\n" +
			        "  \"paymentRequestDate\": \"" + req.getPaymentRequestDate() + "\",\n" +
			        "  \"agencyCode\": \"" + agencyCode + "\",\n" +
			        "  \"payerEmail\": \"" + req.getPayerEmail() + "\",\n" +
			        "  \"mobileNo\": \"" + req.getMobileNo() + "\",\n" +
			        "  \"totalPayableAmount\": \"" + req.getTotalPayableAmount() + "\",\n" +
			        "  \"paymentDueDate\": " +
			            (req.getPaymentDueDate() != null 
			                ? "\"" + req.getPaymentDueDate() + "\"" 
			                : null) + ",\n" +
			        "  \"taxPayerName\": \"" + req.getTaxPayerName() + "\",\n" +
			        "  \"paymentLists\": [\n" +
			        "    {\n" +
			        "      \"serviceCode\": \"" + serviceCode + "\",\n" +
			        "      \"description\": \"" + description + "\",\n" +
			        "      \"payableAmount\": \"" + req.getTotalPayableAmount() + "\"\n" +
			        "    }\n" +
			        "  ]\n" +
			        "}";

			// Build the request
			HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
			try {
				ResponseEntity<String> response = restTemplate.exchange(paymentUrl, HttpMethod.POST,
						requestEntity, String.class);
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
					birmsPaymentDetails.setPayerEmail(req.getPayerEmail());
					birmsPaymentDetails.setTaxPayerName(content.path("taxPayerName").asText());
					birmsPaymentDetails.setMobileNo(content.path("mobileNo").asText());
					// birmsPaymentDetails.setPaymentStatus(content.path("paymentStatus").asText());
					birmsPaymentDetails.setPaymentStatus("pending");
					birmsPaymentDetails.setRedirectUrl(content.path("redirectUrl").asText());
					birmsPaymentDetails.setServiceCode(serviceCode);
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
			p.setReceiptNo(dto.getReceiptNo());
			p.setUpdatedAt(new Date());
			return birmsPaymentRepository.save(p);
		}
		return null;
	}

	@Override
	public ResponseEntity<String> getPaymentReceiptDetails(String receiptNo) {
		return ResponseEntity.ok("Receipt API call here");
	}

	@Override
	public ResponseEntity<?> makePaymentCancel(BirmsPaymentRequestdto dto) {
		updatePaymentStatus(dto);
		return ResponseEntity.ok("Cancelled");
	}

	@Override
	public Optional<BirmsPayment> checkDataExist(String application_no) {
		return birmsPaymentRepository.findByApplicationNo(application_no);
	}

	@Override
	public BirmsPayment updatePaymentCheckBounce(BirmsPaymentRequestdto dto) {
		Optional<BirmsPayment> data = birmsPaymentRepository.findByReceiptNo(dto.getReceiptNo());

		if (data.isPresent()) {
			BirmsPayment p = data.get();
			p.setPaymentStatus("chequebounce");
			p.setCancelledReason(dto.getCancelledReason());
			return birmsPaymentRepository.save(p);
		}
		return null;
	}

	@Override
	public List<ObjectNode> getAllPenaltyApplicationDetails() {
		return new ArrayList<>();
	}

	@Override
	public List<ObjectNode> getByUserPenaltyApplicationDetails(String user_id) {
		return new ArrayList<>();
	}
}