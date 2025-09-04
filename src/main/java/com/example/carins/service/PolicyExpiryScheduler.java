package com.example.carins.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/*
For D) Add a cron that logs within 1 hour after a policy expires
 */
@Service
public class PolicyExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PolicyExpiryScheduler.class);
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final HashSet<Long> loggedPolicyIds = new HashSet<>();

    public PolicyExpiryScheduler(InsurancePolicyRepository insurancePolicyRepository) {
        this.insurancePolicyRepository = insurancePolicyRepository;
    }

    // Scheduled every day after midnight
    // Was fixedRate but we dont need it every hour as all insurances expire at 00:00
    @Scheduled(cron="0 5 0 * * *", zone="GMT+3")
    public void checkExpiredPolicies() {
        LocalDate today = LocalDate.now();

        // Find policies that expired today or before (all expired policies)
        List<InsurancePolicy> expiredPolicies = insurancePolicyRepository.findByEndDateBefore(today);


        if (expiredPolicies.isEmpty()) {
            logger.debug("No expired policies found");
            return;
        }
        
        logger.info("Found {} expired policies to check", expiredPolicies.size());
        
        for (InsurancePolicy policy : expiredPolicies) {
            // Only log if we haven't logged this policy before
            if (!loggedPolicyIds.contains(policy.getId())) {
                logger.info("Policy {} for car {} expired on {}", policy.getId(), policy.getCar().getId(), policy.getEndDate());
                loggedPolicyIds.add(policy.getId());
            }
        }
    }

    public Set<Long> getLoggedPolicyIds() {
        return new HashSet<>(loggedPolicyIds);
    }

    public void clearLoggedPolicyIds() {
        loggedPolicyIds.clear();
    }
}
