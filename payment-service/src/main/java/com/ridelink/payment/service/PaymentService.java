package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRefundRequestDto;
import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.PaymentResponseDto;
import com.ridelink.payment.dto.ReceiptDto;

import java.util.List;

/**
 * Service interface defining contract for RideLink payment processing, refunds, and receipt generation.
 */
public interface PaymentService {

    /**
     * Processes or simulates a payment transaction for a ride.
     */
    ReceiptDto processPayment(PaymentRequestDto dto);

    /**
     * Retrieves payment details by internal payment ID.
     */
    PaymentResponseDto getPaymentById(String id);

    /**
     * Retrieves payment details by ride ID.
     */
    PaymentResponseDto getPaymentByRideId(String rideId);

    /**
     * Retrieves all payments associated with a passenger.
     */
    List<PaymentResponseDto> getPaymentsByPassengerId(String passengerId);

    /**
     * Retrieves all payments associated with a driver.
     */
    List<PaymentResponseDto> getPaymentsByDriverId(String driverId);

    /**
     * Processes a refund for an existing completed payment transaction.
     */
    PaymentResponseDto refundPayment(String paymentId, PaymentRefundRequestDto refundDto);

    /**
     * Retrieves receipt by ride ID.
     */
    ReceiptDto getReceiptByRideId(String rideId);

    /**
     * Retrieves receipt by its unique receipt number.
     */
    ReceiptDto getReceiptByReceiptNumber(String receiptNumber);

    /**
     * Retrieves all receipts issued for a passenger.
     */
    List<ReceiptDto> getReceiptsByPassengerId(String passengerId);

    /**
     * Retrieves all receipts issued for a driver.
     */
    List<ReceiptDto> getReceiptsByDriverId(String driverId);
}
