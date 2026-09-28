package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRefundRequestDto;
import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.PaymentResponseDto;
import com.ridelink.payment.dto.ReceiptDto;
import com.ridelink.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment & Receipt Management", description = "Endpoints for simulated payment processing, transaction records, refunds, and receipt retrieval")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process or Simulate Payment", description = "Records a simulated payment transaction for a ride (CARD, CASH, WALLET, UPI). Generates a digital receipt upon success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed and receipt generated successfully",
                    content = @Content(schema = @Schema(implementation = ReceiptDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment request payload or duplicate payment"),
            @ApiResponse(responseCode = "422", description = "Simulated payment failure (e.g. card declined)")
    })
    @PostMapping("/process")
    public ResponseEntity<ReceiptDto> processPayment(@Valid @RequestBody PaymentRequestDto dto) {
        ReceiptDto receipt = paymentService.processPayment(dto);
        return new ResponseEntity<>(receipt, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Payment by ID", description = "Retrieves payment transaction record by its unique database ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment details found"),
            @ApiResponse(responseCode = "404", description = "Payment record not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "Get Payment by Ride ID", description = "Retrieves the payment transaction associated with a specific ride ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment record found"),
            @ApiResponse(responseCode = "404", description = "No payment record found for the given ride ID")
    })
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(paymentService.getPaymentByRideId(rideId));
    }

    @Operation(summary = "Get Passenger Payment History", description = "Retrieves all payments made by a passenger.")
    @ApiResponse(responseCode = "200", description = "List of payments")
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByPassengerId(@PathVariable String passengerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByPassengerId(passengerId));
    }

    @Operation(summary = "Get Driver Payment History", description = "Retrieves all earnings/payments credited to a driver.")
    @ApiResponse(responseCode = "200", description = "List of driver payments")
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByDriverId(@PathVariable String driverId) {
        return ResponseEntity.ok(paymentService.getPaymentsByDriverId(driverId));
    }

    @Operation(summary = "Refund Payment", description = "Initiates a refund for a previously COMPLETED payment transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment successfully refunded"),
            @ApiResponse(responseCode = "400", description = "Payment is not in COMPLETED state or already refunded"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentRefundRequestDto refundDto) {
        return ResponseEntity.ok(paymentService.refundPayment(id, refundDto));
    }

    @Operation(summary = "Get Receipt by Ride ID", description = "Retrieves the digital receipt issued for a specific ride.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt found"),
            @ApiResponse(responseCode = "404", description = "Receipt not found for ride ID")
    })
    @GetMapping("/receipt/{rideId}")
    public ResponseEntity<ReceiptDto> getReceiptByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(paymentService.getReceiptByRideId(rideId));
    }

    @Operation(summary = "Get Receipt by Receipt Number", description = "Retrieves the digital receipt by its unique receipt number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt found"),
            @ApiResponse(responseCode = "404", description = "Receipt not found with number")
    })
    @GetMapping("/receipt/number/{receiptNumber}")
    public ResponseEntity<ReceiptDto> getReceiptByReceiptNumber(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(paymentService.getReceiptByReceiptNumber(receiptNumber));
    }

    @Operation(summary = "Get Passenger Receipts", description = "Retrieves all digital receipts issued for a passenger.")
    @ApiResponse(responseCode = "200", description = "List of passenger receipts")
    @GetMapping("/receipt/passenger/{passengerId}")
    public ResponseEntity<List<ReceiptDto>> getReceiptsByPassengerId(@PathVariable String passengerId) {
        return ResponseEntity.ok(paymentService.getReceiptsByPassengerId(passengerId));
    }

    @Operation(summary = "Get Driver Receipts", description = "Retrieves all digital receipts issued for a driver.")
    @ApiResponse(responseCode = "200", description = "List of driver receipts")
    @GetMapping("/receipt/driver/{driverId}")
    public ResponseEntity<List<ReceiptDto>> getReceiptsByDriverId(@PathVariable String driverId) {
        return ResponseEntity.ok(paymentService.getReceiptsByDriverId(driverId));
    }
}
