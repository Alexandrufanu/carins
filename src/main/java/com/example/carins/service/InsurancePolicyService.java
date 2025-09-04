package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyDto;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
public class InsurancePolicyService {

    public final InsurancePolicyRepository insurancePolicyRepository;
    public final CarRepository carRepository;

    public InsurancePolicyService(InsurancePolicyRepository insurancePolicyRepository, CarRepository carRepository){
        this.insurancePolicyRepository = insurancePolicyRepository;
        this.carRepository = carRepository;
    }


    public long createInsurance(Long carId, InsurancePolicyDto insurancePolicyDto) throws NoSuchElementException{


        String provider = insurancePolicyDto.getProvider();
        LocalDate startDate, endDate;
        try {
            startDate = insurancePolicyDto.getStartDate();
        }catch (DateTimeParseException e){
            throw new NoSuchElementException("startDate is not valid, please format the text like: \"2007-12-03\" ");
        }

        try {
            endDate = insurancePolicyDto.getEndDate();
        }catch (DateTimeParseException e){
            throw new NoSuchElementException("endDate is not valid, please format the text like: \"2007-12-03\" ");
        }


        Optional<Car> car = carRepository.findById(carId);

        if(car.isEmpty()){
            throw new NoSuchElementException("Could not find the card ID");
        }

        InsurancePolicy insurancePolicy = new InsurancePolicy(car.get(), provider, startDate, endDate);
        insurancePolicy = this.insurancePolicyRepository.save(insurancePolicy);

        return insurancePolicy.getId();
    }

    public List<InsurancePolicy> findPoliciesByCarId(Long carId) {
        return insurancePolicyRepository.findByCarId(carId);
    }
}
