package com.example.carins;

import com.example.carins.service.CarService;
import com.example.carins.service.InsurancePolicyService;
import com.example.carins.service.PolicyExpiryScheduler;
import com.example.carins.web.CarController;
import com.example.carins.web.dto.ClaimDto;
import com.example.carins.web.dto.InsurancePolicyDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;

    @Autowired
    InsurancePolicyService insurancePolicyService;

    @Autowired
    CarController carController;

    @Autowired
    PolicyExpiryScheduler policyExpiryScheduler;

    @Autowired
    private Validator validator;

    /**
     * Test for A) Acceptance criteria:
     * 1. Creating/updating a policy without endDate fails with 4xx and a helpful message.
     */
    @Test
    void creatingPolicyWithoutEndDate() {

        InsurancePolicyDto invalidDto = new InsurancePolicyDto();
        invalidDto.setProvider("Euroins");
        invalidDto.setStartDate(LocalDate.parse("2023-07-07"));
        // endDate is null - this should trigger validation error

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(invalidDto, "insurancePolicyDto");
        validator.validate(invalidDto, bindingResult);

        // Test via controller to get proper HTTP response with validation
        ResponseEntity<?> response = carController.createInsurance(1L, invalidDto, bindingResult);

        // Should return 400 Bad Request
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("End date is required"));
    }

    @Test
    void validNewInsurance() {
        InsurancePolicyDto insurancePolicyDto = new InsurancePolicyDto("Euroins", LocalDate.parse("2023-07-07"), LocalDate.parse("2024-07-07"));

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(insurancePolicyDto, "insurancePolicyDto");
        validator.validate(insurancePolicyDto, bindingResult);

        ResponseEntity<?> response = carController.createInsurance(1L, insurancePolicyDto, bindingResult);

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void invalidNewInsurance() {
        //  null provider should fail validation
        InsurancePolicyDto invalidDto = new InsurancePolicyDto();
        invalidDto.setStartDate(LocalDate.parse("2023-07-07"));
        invalidDto.setEndDate(LocalDate.parse("2024-07-07"));

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(invalidDto, "insurancePolicyDto");
        validator.validate(invalidDto, bindingResult);

        ResponseEntity<?> response = carController.createInsurance(1L, invalidDto, bindingResult);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Provider is required"));
    }

    /**
     * Tests for B) Add new code: Create two new functionalities
     */

    @Test
    void validClaimRegistration() {
        ClaimDto claimDto = new ClaimDto(1L, LocalDate.parse("2024-03-15"), "Windshield damage", new BigDecimal("450.75"));

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(claimDto, "claimDto");
        validator.validate(claimDto, bindingResult);

        ResponseEntity<String> response = carController.registerClaim(1L, claimDto, bindingResult);

        assertEquals(201, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Successfully created"));
        assertNotNull(response.getHeaders().getLocation());
    }

    @Test
    void claimRegistrationWithInvalidCarId() {
        ClaimDto claimDto = new ClaimDto(999L, LocalDate.parse("2024-03-15"), "Windshield damage", new BigDecimal("450.75"));

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(claimDto, "claimDto");
        validator.validate(claimDto, bindingResult);

        ResponseEntity<String> response = carController.registerClaim(999L, claimDto, bindingResult);

        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Car with ID 999 not found"));
    }

    @Test
    void claimRegistrationWithMissingDescription() {
        ClaimDto invalidClaimDto = new ClaimDto();
        invalidClaimDto.setCarId(1L);
        invalidClaimDto.setClaimDate(LocalDate.parse("2024-03-15"));
        invalidClaimDto.setAmount(new BigDecimal("450.75"));
        // description is null - should trigger validation error

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(invalidClaimDto, "claimDto");
        validator.validate(invalidClaimDto, bindingResult);

        ResponseEntity<String> response = carController.registerClaim(1L, invalidClaimDto, bindingResult);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Description is required"));
    }

    @Test
    void claimRegistrationWithMissingClaimDate() {
        ClaimDto invalidClaimDto = new ClaimDto();
        invalidClaimDto.setCarId(1L);
        invalidClaimDto.setDescription("Windshield damage");
        invalidClaimDto.setAmount(new BigDecimal("450.75"));
        // claimDate is null

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(invalidClaimDto, "claimDto");
        validator.validate(invalidClaimDto, bindingResult);

        ResponseEntity<String> response = carController.registerClaim(1L, invalidClaimDto, bindingResult);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Claim date is required"));
    }

    @Test
    void getCarHistoryValid() {
        ResponseEntity<?> response = carController.getCarHistory(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        String responseBody = response.getBody().toString();
        assertTrue(responseBody.contains("carId"));
        assertTrue(responseBody.contains("events"));
    }

    @Test
    void getCarHistoryWithInvalidCarId() {
        ResponseEntity<?> response = carController.getCarHistory(999L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getCarHistoryContainsChronologicalEvents() {
        ResponseEntity<?> response = carController.getCarHistory(1L);

        assertEquals(200, response.getStatusCode().value());

        String responseBody = response.getBody().toString();
        assertTrue(responseBody.contains("INSURANCE_POLICY") && responseBody.contains("CLAIM"));
    }

    /**
     * Tests for C) Insurance validity validation
     */
    @Test
    void validInsuranceValidityCheck() {
        ResponseEntity<?> response = carController.isInsuranceValid(1L, "2023-07-15");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void invalidCarIdInsuranceValidityCheck() {
        ResponseEntity<?> response = carController.isInsuranceValid(999L, "2023-07-15");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void invalidDateFormatInsuranceValidityCheck() {
        ResponseEntity<?> response = carController.isInsuranceValid(1L, "invalid-date");
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Invalid date format"));
    }

    @Test
    void dateOutsideRangeInsuranceValidityCheck() {
        ResponseEntity<?> response = carController.isInsuranceValid(1L, "1900-01-01");
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("outside supported range"));
    }

    /**
     * Tests for D) Add a cron that logs within 1 hour after a policy expires
     */
    @Test
    void schedulerDoesNotLogSamePolicyTwice() {
        policyExpiryScheduler.clearLoggedPolicyIds();

        policyExpiryScheduler.checkExpiredPolicies();
        int firstRunLoggedCount = policyExpiryScheduler.getLoggedPolicyIds().size();
        policyExpiryScheduler.checkExpiredPolicies();
        int secondRunLoggedCount = policyExpiryScheduler.getLoggedPolicyIds().size();

        // We check that logs are idetical multiple runs
        assertEquals(firstRunLoggedCount, secondRunLoggedCount);
    }

    @Test
    void schedulerCanClearLoggedPolicies() {
        policyExpiryScheduler.checkExpiredPolicies();
        policyExpiryScheduler.clearLoggedPolicyIds();

        assertTrue(policyExpiryScheduler.getLoggedPolicyIds().isEmpty());
    }

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }


}
