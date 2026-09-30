package com.ridelink.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to update a driver's operating service region and city")
public class ServiceAreaUpdateDto {

    @NotBlank(message = "Service city is required")
    @Schema(description = "Primary city or metropolitan operating area", example = "Colombo")
    private String serviceCity;

    @Schema(description = "Operating district or sub-zone", example = "Colombo District")
    private String serviceArea;
}
