package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.web.dto.ClaimDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ClaimService {

    public final ClaimRepository claimRepository;
    public final CarRepository carRepository;

    public ClaimService(ClaimRepository claimRepository, CarRepository carRepository) {
        this.claimRepository = claimRepository;
        this.carRepository = carRepository;
    }

    public long createClaim(Long carId, ClaimDto claimDto) throws NoSuchElementException {

        // Check required fields
        String description = claimDto.getDescription();
        LocalDate claimDate = claimDto.getClaimDate();
        BigDecimal amount = claimDto.getAmount();

        Optional<Car> car = carRepository.findById(carId);

        if (car.isEmpty()) {
            throw new NoSuchElementException("Could not find the car ID");
        }

        Claim claim = new Claim(car.get(), claimDate, description, amount);
        claim = this.claimRepository.save(claim);

        return claim.getId();
    }

    public List<Claim> findClaimsByCarId(Long carId) {
        return claimRepository.findByCarId(carId);
    }

    public Claim saveClaim(Claim claim) {
        return claimRepository.save(claim);
    }
}
