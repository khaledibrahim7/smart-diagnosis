package com.khaled.smart_diagnosis.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class PhoneValidationService {
    private static final Logger LOGGER = Logger.getLogger(PhoneValidationService.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();

    public String getCountryCode(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip;
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            return json.optString("countryCode", "UNKNOWN");
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch country code for IP: " + ip + " - " + e.getMessage());
            return "UNKNOWN";
        }
    }

    public boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        return phoneNumber.matches("\\+\\d{10,15}");
    }
}
