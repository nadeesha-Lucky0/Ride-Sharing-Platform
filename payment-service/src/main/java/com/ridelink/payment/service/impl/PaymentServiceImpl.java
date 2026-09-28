package com.ridelink.payment.service.impl;

import com.ridelink.payment.dto.PaymentRefundRequestDto;
import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.PaymentResponseDto;
import com.ridelink.payment.dto.ReceiptDto;
import com.ridelink.payment.exception.BadRequestException;
import com.ridelink.payment.exception.PaymentProcessingException;
import com.ridelink.payment.exception.ResourceNotFoundException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.Receipt;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import com.ridelink.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enterprise implementation of PaymentService managing Payment Processing, Payment Lifecycles, and Digital Receipts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    @Override
    @Transactional
    public ReceiptDto processPayment(PaymentRequestDto dto) {
        log.info("Processing payment for ride: {}, amount: {}, method: {}", dto.getRideId(), dto.getAmount(), dto.getPaymentMethod());

        // Check if a completed payment already exists for this ride
        var existingCompleted = paymentRepository.findByRideId(dto.getRideId())
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED);

        if (existingCompleted.isPresent()) {
            throw new BadRequestException("Ride " + dto.getRideId() + " has already been paid and completed.");
        }

        String txRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Check for simulated failure scenario (e.g. card declined, insufficient funds, network timeout)
        if (Boolean.TRUE.equals(dto.getSimulateFailure())) {
            String failureMsg = (dto.getFailureReason() != null && !dto.getFailureReason().isBlank())
                    ? dto.getFailureReason()
                    : "Simulated payment failure: Card declined by issuing bank.";

            Payment failedPayment = Payment.builder()
                    .rideId(dto.getRideId())
                    .passengerId(dto.getPassengerId())
                    .driverId(dto.getDriverId())
                    .amount(dto.getAmount())
                    .currency("INR")
                    .paymentMethod(dto.getPaymentMethod())
                    .status(PaymentStatus.FAILED)
                    .transactionReference(txRef)
                    .failureReason(failureMsg)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(failedPayment);
            log.warn("Payment simulation failed for ride {}: {}", dto.getRideId(), failureMsg);
            throw new PaymentProcessingException("Payment transaction failed: " + failureMsg);
        }

        // Process successful payment
        Payment payment = Payment.builder()
                .rideId(dto.getRideId())
                .passengerId(dto.getPassengerId())
                .driverId(dto.getDriverId())
                .amount(dto.getAmount())
                .currency("INR")
                .paymentMethod(dto.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .transactionReference(txRef)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Calculate breakdown for receipt
        double baseFare = Math.min(50.0, dto.getAmount() * 0.3);
        double taxAmount = Math.round(dto.getAmount() * 0.05 * 100.0) / 100.0;
        double distanceFare = Math.max(0.0, Math.round((dto.getAmount() - baseFare - taxAmount) * 100.0) / 100.0);

        Receipt receipt = Receipt.builder()
                .receiptNumber("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .rideId(dto.getRideId())
                .paymentId(savedPayment.getId())
                .passengerId(dto.getPassengerId())
                .driverId(dto.getDriverId())
                .totalAmount(dto.getAmount())
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(0.0)
                .surgeMultiplier(1.0)
                .taxAmount(taxAmount)
                .discountAmount(0.0)
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.COMPLETED)
                .issuedAt(LocalDateTime.now())
                .build();

        Receipt savedReceipt = receiptRepository.save(receipt);
        log.info("Payment succeeded. Receipt generated: {}", savedReceipt.getReceiptNumber());

        return mapToReceiptDto(savedReceipt);
    }

    @Override
    public PaymentResponseDto getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return mapToPaymentResponseDto(payment);
    }

    @Override
    public PaymentResponseDto getPaymentByRideId(String rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ride ID: " + rideId));
        return mapToPaymentResponseDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByPassengerId(String passengerId) {
        return paymentRepository.findByPassengerId(passengerId).stream()
                .map(this::mapToPaymentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByDriverId(String driverId) {
        return paymentRepository.findByDriverId(driverId).stream()
                .map(this::mapToPaymentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponseDto refundPayment(String paymentId, PaymentRefundRequestDto refundDto) {
        log.info("Initiating refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment " + paymentId + " has already been refunded.");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only COMPLETED payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(refundDto.getReason());
        payment.setRefundedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update corresponding receipt status if present
        receiptRepository.findByPaymentId(paymentId).ifPresent(receipt -> {
            receipt.setPaymentStatus(PaymentStatus.REFUNDED);
            receiptRepository.save(receipt);
        });

        log.info("Payment ID: {} successfully refunded.", paymentId);
        return mapToPaymentResponseDto(updatedPayment);
    }

    @Override
    public ReceiptDto getReceiptByRideId(String rideId) {
        Receipt receipt = receiptRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for ride ID: " + rideId));
        return mapToReceiptDto(receipt);
    }

    @Override
    public ReceiptDto getReceiptByReceiptNumber(String receiptNumber) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with number: " + receiptNumber));
        return mapToReceiptDto(receipt);
    }

    @Override
    public List<ReceiptDto> getReceiptsByPassengerId(String passengerId) {
        return receiptRepository.findByPassengerId(passengerId).stream()
                .map(this::mapToReceiptDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDto> getReceiptsByDriverId(String driverId) {
        return receiptRepository.findByDriverId(driverId).stream()
                .map(this::mapToReceiptDto)
                .collect(Collectors.toList());
    }

    private ReceiptDto mapToReceiptDto(Receipt receipt) {
        return ReceiptDto.builder()
                .receiptNumber(receipt.getReceiptNumber())
                .rideId(receipt.getRideId())
                .paymentId(receipt.getPaymentId())
                .passengerId(receipt.getPassengerId())
                .driverId(receipt.getDriverId())
                .totalAmount(receipt.getTotalAmount())
                .baseFare(receipt.getBaseFare())
                .distanceFare(receipt.getDistanceFare())
                .timeFare(receipt.getTimeFare())
                .surgeMultiplier(receipt.getSurgeMultiplier())
                .taxAmount(receipt.getTaxAmount())
                .discountAmount(receipt.getDiscountAmount())
                .paymentMethod(receipt.getPaymentMethod())
                .paymentStatus(receipt.getPaymentStatus())
                .issuedAt(receipt.getIssuedAt())
                .build();
    }

    private PaymentResponseDto mapToPaymentResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .driverId(payment.getDriverId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .failureReason(payment.getFailureReason())
                .refundReason(payment.getRefundReason())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
