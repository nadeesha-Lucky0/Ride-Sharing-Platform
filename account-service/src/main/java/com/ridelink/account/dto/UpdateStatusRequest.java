package com.ridelink.account.dto;

import com.ridelink.account.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account status update payload")
public class UpdateStatusRequest {

    @NotNull(message = "Status is required (ACTIVE, PENDING_VERIFICATION, SUSPENDED)")
    @Schema(description = "New account status", example = "SUSPENDED", requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountStatus status;
}
