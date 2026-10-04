package com.ridelink.driver.dto;

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

    @Schema(description = "Error occurrence timestamp", example = "2026-09-28T10:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status description", example = "Bad Request")
    private String error;

    @Schema(description = "Descriptive error message", example = "Driver with ID 'xyz' was not found.")
    private String message;

    @Schema(description = "Request URI path", example = "/api/drivers/xyz")
    private String path;

    @Schema(description = "Field validation errors map, if applicable")
    private Map<String, String> fieldErrors;
}
