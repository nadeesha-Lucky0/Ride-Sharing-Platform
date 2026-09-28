package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRefundRequestDto;
import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.PaymentResponseDto;
import com.ridelink.payment.dto.ReceiptDto;
import com.ridelink.payment.exception.BadRequestException;
import com.ridelink.payment.exception.PaymentProcessingException;
import com.ridelink.payment.exception.ResourceNotFoundException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.Receipt;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import com.ridelink.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Happy Path: Process simulated payment successfully and generate receipt")
    void testProcessPayment_Success() {
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .rideId("RIDE-101")
                .passengerId("PASS-1")
                .driverId("DRV-1")
                .amount(300.0)
                .paymentMethod(PaymentMethod.CARD)
                .build();

        when(paymentRepository.findByRideId("RIDE-101")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId("PAY-101");
            return p;
        });
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(inv -> {
            Receipt r = inv.getArgument(0);
            r.setId("REC-ID-101");
            return r;
        });

        ReceiptDto receiptDto = paymentService.processPayment(requestDto);

        assertNotNull(receiptDto);
        assertEquals("RIDE-101", receiptDto.getRideId());
        assertEquals(300.0, receiptDto.getTotalAmount());
        assertEquals(PaymentMethod.CARD, receiptDto.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, receiptDto.getPaymentStatus());
        assertNotNull(receiptDto.getReceiptNumber());
        assertTrue(receiptDto.getReceiptNumber().startsWith("REC-"));

        verify(paymentRepository).save(any(Payment.class));
        verify(receiptRepository).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Negative Scenario: Duplicate payment for already completed ride throws BadRequestException")
    void testProcessPayment_DuplicateRidePayment_ThrowsException() {
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .rideId("RIDE-DUPLICATE")
                .passengerId("PASS-1")
                .driverId("DRV-1")
                .amount(200.0)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        Payment existingPayment = Payment.builder()
                .id("PAY-EXISTING")
                .rideId("RIDE-DUPLICATE")
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findByRideId("RIDE-DUPLICATE")).thenReturn(Optional.of(existingPayment));

        assertThrows(BadRequestException.class, () -> paymentService.processPayment(requestDto));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Negative Scenario: Simulated payment failure records FAILED status and throws PaymentProcessingException")
    void testProcessPayment_SimulatedFailure_ThrowsException() {
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .rideId("RIDE-FAIL")
                .passengerId("PASS-2")
                .driverId("DRV-2")
                .amount(150.0)
                .paymentMethod(PaymentMethod.CARD)
                .simulateFailure(true)
                .failureReason("Card declined: Insufficient funds")
                .build();

        when(paymentRepository.findByRideId("RIDE-FAIL")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentProcessingException ex = assertThrows(PaymentProcessingException.class,
                () -> paymentService.processPayment(requestDto));

        assertTrue(ex.getMessage().contains("Card declined: Insufficient funds"));
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Happy Path: Refund completed payment successfully")
    void testRefundPayment_Success() {
        Payment payment = Payment.builder()
                .id("PAY-REF-1")
                .rideId("RIDE-REF-1")
                .amount(250.0)
                .status(PaymentStatus.COMPLETED)
                .build();

        Receipt receipt = Receipt.builder()
                .paymentId("PAY-REF-1")
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById("PAY-REF-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(receiptRepository.findByPaymentId("PAY-REF-1")).thenReturn(Optional.of(receipt));

        PaymentRefundRequestDto refundDto = PaymentRefundRequestDto.builder()
                .reason("Driver canceled halfway")
                .build();

        PaymentResponseDto response = paymentService.refundPayment("PAY-REF-1", refundDto);

        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        assertEquals("Driver canceled halfway", response.getRefundReason());
        assertNotNull(response.getRefundedAt());
        assertEquals(PaymentStatus.REFUNDED, receipt.getPaymentStatus());
    }

    @Test
    @DisplayName("Negative Scenario: Refunding an already refunded payment throws BadRequestException")
    void testRefundPayment_AlreadyRefunded_ThrowsException() {
        Payment payment = Payment.builder()
                .id("PAY-ALREADY-REF")
                .status(PaymentStatus.REFUNDED)
                .build();

        when(paymentRepository.findById("PAY-ALREADY-REF")).thenReturn(Optional.of(payment));

        PaymentRefundRequestDto refundDto = PaymentRefundRequestDto.builder()
                .reason("Repeat refund request")
                .build();

        assertThrows(BadRequestException.class, () -> paymentService.refundPayment("PAY-ALREADY-REF", refundDto));
    }

    @Test
    @DisplayName("Happy Path: Retrieve receipt by ride ID")
    void testGetReceiptByRideId_Success() {
        Receipt receipt = Receipt.builder()
                .id("REC-1")
                .receiptNumber("REC-ABC123")
                .rideId("RIDE-900")
                .totalAmount(450.0)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(receiptRepository.findByRideId("RIDE-900")).thenReturn(Optional.of(receipt));

        ReceiptDto result = paymentService.getReceiptByRideId("RIDE-900");

        assertNotNull(result);
        assertEquals("REC-ABC123", result.getReceiptNumber());
        assertEquals("RIDE-900", result.getRideId());
        assertEquals(450.0, result.getTotalAmount());
    }

    @Test
    @DisplayName("Negative Scenario: Receipt not found throws ResourceNotFoundException")
    void testGetReceiptByRideId_NotFound_ThrowsException() {
        when(receiptRepository.findByRideId("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getReceiptByRideId("NON-EXISTENT"));
    }

    @Test
    @DisplayName("Happy Path: Retrieve payment history by passenger and driver")
    void testGetPaymentsByPassengerAndDriver() {
        Payment p1 = Payment.builder().id("P1").passengerId("PASS-10").driverId("DRV-20").amount(100.0).status(PaymentStatus.COMPLETED).build();
        Payment p2 = Payment.builder().id("P2").passengerId("PASS-10").driverId("DRV-20").amount(150.0).status(PaymentStatus.COMPLETED).build();

        when(paymentRepository.findByPassengerId("PASS-10")).thenReturn(List.of(p1, p2));
        when(paymentRepository.findByDriverId("DRV-20")).thenReturn(List.of(p1, p2));

        List<PaymentResponseDto> passengerPayments = paymentService.getPaymentsByPassengerId("PASS-10");
        List<PaymentResponseDto> driverPayments = paymentService.getPaymentsByDriverId("DRV-20");

        assertEquals(2, passengerPayments.size());
        assertEquals(2, driverPayments.size());
    }
}
