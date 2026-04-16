package ca.sheridancollege.shar1440.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class PayPalService {

    // application.properties
    @Value("${paypal.client.id}")
    private String clientId;

    
    @Value("${paypal.client.secret}")
    private String clientSecret;

   
    @Value("${paypal.base.url}")
    private String baseUrl;

   
    private final RestTemplate restTemplate = new RestTemplate();

    // STEP 1: GET ACCESS TOKEN
    public String getAccessToken() {
        try {
            String credentials = clientId + ":" + clientSecret;

            // Encode credentials in Base64 (required by PayPal)
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();

            // Add Authorization header (Basic Auth)
            headers.set("Authorization", "Basic " + encodedCredentials);

            // Content type must be form URL encoded
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Body data for token request
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            // Grant type required by PayPal OAuth
            formData.add("grant_type", "client_credentials");

            // Combine headers + body
            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(formData, headers);

            // Send POST request to PayPal to get access token
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v1/oauth2/token",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Check if token exists
            if (response.getBody() == null ||
                response.getBody().get("access_token") == null) {

                throw new RuntimeException("PayPal access token was not returned.");
            }

            // Return access token string
            return response.getBody().get("access_token").toString();

        } catch (HttpStatusCodeException e) {
            // If PayPal returns error (like 401)
            throw new RuntimeException(
                    "PayPal token error: " + e.getStatusCode() +
                    " - " + e.getResponseBodyAsString()
            );
        }
    }


    // STEP 2: CREATE ORDER
    public Map<String, Object> createOrder() {
        try {
            // Get access token first
            String accessToken = getAccessToken();

            // Create headers
            HttpHeaders headers = new HttpHeaders();

            // Add Bearer token (required for PayPal API)
            headers.setBearerAuth(accessToken);

            // Set JSON content type
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create amount object
            Map<String, Object> amount = new HashMap<>();
            amount.put("currency_code", "CAD"); // currency
            amount.put("value", "1.00");        // payment amount

            // Create purchase unit (what user is paying for)
            Map<String, Object> purchaseUnit = new HashMap<>();
            purchaseUnit.put("amount", amount);
            purchaseUnit.put("description", "Group Project PayPal Payment");

            // Create main request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("intent", "CAPTURE"); // capture immediately
            requestBody.put("purchase_units", List.of(purchaseUnit));

            // Combine body + headers
            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            // Send POST request to create order
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v2/checkout/orders",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Check response
            if (response.getBody() == null) {
                throw new RuntimeException("PayPal order was not created.");
            }

            // Return order JSON (contains order ID)
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            // Handle PayPal API errors
            throw new RuntimeException(
                    "PayPal create order error: " + e.getStatusCode() +
                    " - " + e.getResponseBodyAsString()
            );
        }
    }


    // STEP 3: CAPTURE ORDER
    public Map<String, Object> captureOrder(String orderId) {
        try {
            // Get access token
            String accessToken = getAccessToken();

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Empty JSON body required by PayPal
            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            // Send POST request to capture payment
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Check response
            if (response.getBody() == null) {
                throw new RuntimeException("PayPal order was not captured.");
            }

            // Return payment result
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            // Handle errors (like invalid orderId)
            throw new RuntimeException(
                    "PayPal capture error: " + e.getStatusCode() +
                    " - " + e.getResponseBodyAsString()
            );
        }
    }
}