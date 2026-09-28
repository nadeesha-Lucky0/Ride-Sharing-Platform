package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.model.FareRule;
import com.ridelink.payment.service.FareCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fare")
@RequiredArgsConstructor
@Tag(name = "Fare Management", description = "Endpoints for dynamic fare estimation, final fare calculation, and fare rule inquiries")
public class FareController {

    private final FareCalculationService fareCalculationService;

    @Operation(summary = "Calculate Fare Estimate", description = "Calculates an upfront estimated fare based on distance or pickup/dropoff GPS coordinates, duration, vehicle category, and surge multiplier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fare estimation calculated successfully",
                    content = @Content(schema = @Schema(implementation = FareEstimateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or missing coordinates/distance")
    })
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        return ResponseEntity.ok(fareCalculationService.estimateFare(request));
    }

    @Operation(summary = "Calculate Final Fare", description = "Calculates the finalized fare upon ride completion using actual distance, actual duration, driver waiting time, surge, and promo discounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Final fare calculated successfully",
                    content = @Content(schema = @Schema(implementation = FinalFareResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid final fare request parameters")
    })
    @PostMapping("/calculate-final")
    public ResponseEntity<FinalFareResponse> calculateFinalFare(@Valid @RequestBody FinalFareRequest request) {
        return ResponseEntity.ok(fareCalculationService.calculateFinalFare(request));
    }

    @Operation(summary = "Get All Fare Rules", description = "Retrieves configured fare rules and rates across all vehicle categories.")
    @ApiResponse(responseCode = "200", description = "List of fare rules")
    @GetMapping("/rules")
    public ResponseEntity<List<FareRule>> getAllFareRules() {
        return ResponseEntity.ok(fareCalculationService.getAllFareRules());
    }

    @Operation(summary = "Get Fare Rule by Category", description = "Retrieves specific fare rates for a given vehicle category (e.g., ECONOMY, PREMIUM, BIKE).")
    @ApiResponse(responseCode = "200", description = "Fare rule details")
    @GetMapping("/rules/{category}")
    public ResponseEntity<FareRule> getFareRuleByCategory(@PathVariable String category) {
        return ResponseEntity.ok(fareCalculationService.resolveFareRule(category));
    }
}
