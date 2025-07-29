package com.bajaj.rishabhdev2156;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@SpringBootApplication
public class Rishabhdev2156Application {

	public static void main(String[] args) {
		SpringApplication.run(Rishabhdev2156Application.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

@Component
@Profile("!test")
class WebhookClient implements CommandLineRunner {

	private final RestTemplate restTemplate;

	// Constructor Injection of RestTemplate
	public WebhookClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public void run(String... args) throws Exception {

		// Step 1: Generate webhook URL & Access Token
		String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		Map<String, String> requestBody = Map.of(
				"name", "Rishabhdev",
				"regNo", "2210992156",
				"email", "rishabhdev2156.be22@chitkara.edu.in");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		System.out.println("Requesting webhook generation...");

		ResponseEntity<Map> response = restTemplate.postForEntity(generateWebhookUrl, request, Map.class);

		if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
			String webhookUrl = (String) response.getBody().get("webhook");
			String accessToken = (String) response.getBody().get("accessToken");

			System.out.println("Received webhook URL: " + webhookUrl);
			System.out.println("Received access token.");

			// Step 2: Compose final SQL query (Question 2 solution)
			String finalSqlQuery = """
					SELECT
					    e1.EMP_ID,
					    e1.FIRST_NAME,
					    e1.LAST_NAME,
					    d.DEPARTMENT_NAME,
					    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
					FROM EMPLOYEE e1
					JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
					LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT
					    AND e2.DOB > e1.DOB
					GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
					ORDER BY e1.EMP_ID DESC;
					""";

			// Step 3: Submit final SQL query to webhook URL with JWT Authorization header
			HttpHeaders postHeaders = new HttpHeaders();
			postHeaders.setContentType(MediaType.APPLICATION_JSON);
			postHeaders.setBearerAuth(accessToken);

			Map<String, String> finalAnswerBody = Map.of("finalQuery", finalSqlQuery);

			HttpEntity<Map<String, String>> finalRequest = new HttpEntity<>(finalAnswerBody, postHeaders);

			System.out.println("Submitting final SQL query...");

			ResponseEntity<String> finalResponse = restTemplate.postForEntity(webhookUrl, finalRequest, String.class);

			if (finalResponse.getStatusCode().is2xxSuccessful()) {
				System.out.println("Final SQL query submitted successfully.");
			} else {
				System.err.println("Failed to submit final SQL query. HTTP status: " + finalResponse.getStatusCode());
				System.err.println("Response body: " + finalResponse.getBody());
			}

		} else {
			System.err.println("Failed to get webhook URL and access token.");
			System.err.println("Status code: " + response.getStatusCode());
			System.err.println("Response body: " + response.getBody());
		}
	}
}