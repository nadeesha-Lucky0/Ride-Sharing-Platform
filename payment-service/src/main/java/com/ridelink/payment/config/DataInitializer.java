package com.ridelink.payment.config;

import com.ridelink.payment.model.FareRule;
import com.ridelink.payment.repository.FareRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FareRuleRepository fareRuleRepository;

    @Override
    public void run(String... args) {
        if (fareRuleRepository.count() == 0) {
            log.info("Seeding default RideLink vehicle fare rules into MongoDB...");

            List<FareRule> defaultRules = List.of(
                    FareRule.builder()
                            .vehicleCategory("ECONOMY")
                            .baseFare(50.0)
                            .perKmRate(15.0)
                            .perMinuteRate(2.0)
                            .minimumFare(60.0)
                            .taxRatePercent(5.0)
                            .build(),
                    FareRule.builder()
                            .vehicleCategory("PREMIUM")
                            .baseFare(100.0)
                            .perKmRate(25.0)
                            .perMinuteRate(3.5)
                            .minimumFare(120.0)
                            .taxRatePercent(5.0)
                            .build(),
                    FareRule.builder()
                            .vehicleCategory("BIKE")
                            .baseFare(25.0)
                            .perKmRate(8.0)
                            .perMinuteRate(1.0)
                            .minimumFare(30.0)
                            .taxRatePercent(5.0)
                            .build(),
                    FareRule.builder()
                            .vehicleCategory("AUTO")
                            .baseFare(35.0)
                            .perKmRate(12.0)
                            .perMinuteRate(1.5)
                            .minimumFare(40.0)
                            .taxRatePercent(5.0)
                            .build(),
                    FareRule.builder()
                            .vehicleCategory("XL")
                            .baseFare(150.0)
                            .perKmRate(30.0)
                            .perMinuteRate(4.0)
                            .minimumFare(180.0)
                            .taxRatePercent(5.0)
                            .build()
            );

            fareRuleRepository.saveAll(defaultRules);
            log.info("Successfully seeded {} default fare rules.", defaultRules.size());
        }
    }
}
