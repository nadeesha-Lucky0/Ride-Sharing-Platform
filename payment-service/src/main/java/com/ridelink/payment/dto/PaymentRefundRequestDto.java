package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for initiating a refund on a completed payment")
public class PaymentRefundRequestDto {

    @NotBlank(message = "Refund reason is required")
    @Schema(description = "Reason for requesting the refund", example = "Driver did not show up after payment capture")
    private String reason;

    @Positive(message = "Refund amount must be positive if specified")
    @Schema(description = "Optional partial refund amount (defaults to full payment amount if omitted)", example = "350.50")
    private Double refundAmount;
}
