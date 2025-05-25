package com.khaled.smart_diagnosis.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.rest.lookups.v1.PhoneNumber;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class PhoneValidationService {
    private static final Logger LOGGER = Logger.getLogger(PhoneValidationService.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();


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


    public boolean isValidPhoneNumber(String phoneNumber, String countryIsoCode) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        if ("UNKNOWN".equals(countryIsoCode)) {
            LOGGER.warning("Country code is UNKNOWN, cannot validate phone number correctly.");
            return false;
        }

        try {
            if (phoneNumber.startsWith("+")) {
                Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, null);
                return phoneUtil.isValidNumber(parsedNumber);
            }

            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, countryIsoCode);
            return phoneUtil.isValidNumberForRegion(parsedNumber, countryIsoCode);

        } catch (NumberParseException e) {
            LOGGER.warning("Phone number parsing failed: " + e.getMessage());
            return false;
        }
    }

}
