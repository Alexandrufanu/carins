package com.example.carins.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClaimDto {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Claim date is required!")
    private LocalDate claimDate;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required!")
    @Positive(message = "Amount must be positive!")
    private BigDecimal amount;

    public ClaimDto() {}

    public ClaimDto(Long carId, LocalDate claimDate, String description, BigDecimal amount) {
        this.carId = carId;
        this.claimDate = claimDate;
        this.description = description;
        this.amount = amount;
    }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }


    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}