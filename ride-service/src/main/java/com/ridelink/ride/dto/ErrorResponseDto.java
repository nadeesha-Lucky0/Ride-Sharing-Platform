package com.ridelink.ride.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public class ErrorResponseDto {

    @Schema(description = "Error timestamp", example = "2026-09-28T09:15:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status description", example = "Bad Request")
    private String error;

    @Schema(description = "Descriptive error message", example = "Invalid ride status transition from 'REQUESTED' to 'COMPLETED'.")
    private String message;

    @Schema(description = "Request URI path where the error occurred", example = "/api/rides/123/status")
    private String path;

    @Schema(description = "Map of field validation errors, if applicable")
    private Map<String, String> fieldErrors;
}
