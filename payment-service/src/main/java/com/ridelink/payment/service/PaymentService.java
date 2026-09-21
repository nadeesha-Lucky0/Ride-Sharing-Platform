package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.ReceiptDto;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.Receipt;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    public ReceiptDto processPayment(PaymentRequestDto dto) {
        String txRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .rideId(dto.getRideId())
                .passengerId(dto.getPassengerId())
                .driverId(dto.getDriverId())
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .transactionReference(txRef)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        double baseFare = 50.0;
        double taxAmount = Math.round(dto.getAmount() * 0.05 * 100.0) / 100.0;
        double distanceFare = Math.max(0.0, dto.getAmount() - baseFare - taxAmount);

        Receipt receipt = Receipt.builder()
                .receiptNumber("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .rideId(dto.getRideId())
                .paymentId(savedPayment.getId())
                .totalAmount(dto.getAmount())
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(0.0)
                .taxAmount(taxAmount)
                .paymentMethod(dto.getPaymentMethod())
                .issuedAt(LocalDateTime.now())
                .build();

        Receipt savedReceipt = receiptRepository.save(receipt);

        return ReceiptDto.builder()
                .receiptNumber(savedReceipt.getReceiptNumber())
                .rideId(savedReceipt.getRideId())
                .paymentId(savedReceipt.getPaymentId())
                .totalAmount(savedReceipt.getTotalAmount())
                .baseFare(savedReceipt.getBaseFare())
                .distanceFare(savedReceipt.getDistanceFare())
                .timeFare(savedReceipt.getTimeFare())
                .taxAmount(savedReceipt.getTaxAmount())
                .paymentMethod(savedReceipt.getPaymentMethod())
                .issuedAt(savedReceipt.getIssuedAt())
                .build();
    }

    public ReceiptDto getReceiptByRideId(String rideId) {
        Receipt receipt = receiptRepository.findByRideId(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found for ride: " + rideId));

        return ReceiptDto.builder()
                .receiptNumber(receipt.getReceiptNumber())
                .rideId(receipt.getRideId())
                .paymentId(receipt.getPaymentId())
                .totalAmount(receipt.getTotalAmount())
                .baseFare(receipt.getBaseFare())
                .distanceFare(receipt.getDistanceFare())
                .timeFare(receipt.getTimeFare())
                .taxAmount(receipt.getTaxAmount())
                .paymentMethod(receipt.getPaymentMethod())
                .issuedAt(receipt.getIssuedAt())
                .build();
    }
}
