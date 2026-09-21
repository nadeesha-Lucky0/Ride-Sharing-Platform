package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.ReceiptDto;
import com.ridelink.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ReceiptDto> processPayment(@Valid @RequestBody PaymentRequestDto dto) {
        ReceiptDto receipt = paymentService.processPayment(dto);
        return new ResponseEntity<>(receipt, HttpStatus.CREATED);
    }

    @GetMapping("/receipt/{rideId}")
    public ResponseEntity<ReceiptDto> getReceiptByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(paymentService.getReceiptByRideId(rideId));
    }
}
