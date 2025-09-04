package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.CarService;
import com.example.carins.service.ClaimService;
import com.example.carins.service.InsurancePolicyService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import com.example.carins.web.dto.HistoryEventDto;
import com.example.carins.web.dto.InsurancePolicyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.jpa.domain.AbstractAuditable_;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;
    private final InsurancePolicyService insurancePolicyService;
    private final ClaimService claimService;

    public CarController(CarService service, InsurancePolicyService insurancePolicyService, ClaimService claimService) {
        this.service = service;
        this.insurancePolicyService = insurancePolicyService;
        this.claimService = claimService;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }


    /*
    For A) - Acceptance criteria - Creating a policy without endDate fails with 4xx and a helpful message.
     */
    @PostMapping("/cars/{carId}/create-insurance")
    public ResponseEntity<?> createInsurance(@PathVariable Long carId, @RequestBody @Valid InsurancePolicyDto insurancePolicyDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }

            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }

        URI uri;
        try {

            long id = insurancePolicyService.createInsurance(carId, insurancePolicyDto);

            uri = new URI("/api/"+ carId + "/insurance/" + id);
        }catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().body("Something went wrong, we could not generate the URI: " + e);
        }catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("Something went wrong, some data provided is not as requested: " + e);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Something went wrong: " + e);

        }

        return ResponseEntity.created(uri).body("Insurance created successfully!");
    }

    /*
    For B) 1. - Register an insurance claim for a car
     */
    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<String> registerClaim(@PathVariable @Positive Long carId, @RequestBody @Valid ClaimDto claimDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }

        URI uri;
        try {
            Optional<Car> carOptional = service.findCarById(claimDto.getCarId());
            if (carOptional.isEmpty()) {
                return ResponseEntity.status(404).body("Car with ID " + claimDto.getCarId() + " not found");
            }

            long id = claimService.createClaim(carId, claimDto);

            uri = new URI("/api/cars/" + carId + "/claims/" + id);
        } catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().body("Something went wrong, we could not generate the URI: " + e);
        }


        return ResponseEntity.created(uri).body("Successfully created!");
    }

    /*
    For B) 2. - Get the history of a car (regardless of owner)
     */
    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<?> getCarHistory(@PathVariable Long carId) {
        try {
            // Check if car exists
            Optional<Car> car = service.findCarById(carId);
            if (car.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<InsurancePolicy> policies = insurancePolicyService.findPoliciesByCarId(carId);
            List<Claim> claims = claimService.findClaimsByCarId(carId);

            List<HistoryEventDto> events = new ArrayList<>();
            
            // Add insurance policies as events
            for (InsurancePolicy policy : policies) {
                events.add(new HistoryEventDto(
                    "INSURANCE_POLICY", 
                    policy.getStartDate(), 
                    "Insurance policy started with " + policy.getProvider(),
                    policy.getId()
                ));
                if (policy.getEndDate() != null) {
                    events.add(new HistoryEventDto(
                        "INSURANCE_POLICY", 
                        policy.getEndDate(), 
                        "Insurance policy ended with " + policy.getProvider(),
                        policy.getId()
                    ));
                }
            }
            
            // Add claims as events
            for (Claim claim : claims) {
                events.add(new HistoryEventDto(
                    "CLAIM", 
                    claim.getClaimDate(), 
                    claim.getDescription() + " - Amount: $" + claim.getAmount(),
                    claim.getId()
                ));
            }
            
            // Sort by date to create chronological history
            events.sort((a, b) -> a.date().compareTo(b.date()));

            return ResponseEntity.ok(new CarHistoryResponse(carId, events));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong: " + e);
        }
    }

    /*
    For C) - Add validation: Protect the insurance validity check against invalid values
     */
    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@Valid @PathVariable Long carId,@Valid @RequestParam String date ) {
        try {
            // Check if car exists
            Optional<Car> car = service.findCarById(carId);
            if (car.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LocalDate d = LocalDate.parse(date);

            // Validate date is not too far in past or future (simple range check)
            LocalDate now = LocalDate.now();
            if (d.isBefore(now.minusYears(50)) || d.isAfter(now.plusYears(50))) {
                return ResponseEntity.badRequest().body("Date is outside supported range (50 years from today)");
            }

            boolean valid = service.isInsuranceValid(carId, d);
            return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use YYYY-MM-DD format: " + e.getMessage());
        }
    }




    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
    public record CarHistoryResponse(Long carId, List<HistoryEventDto> events) {}

}
