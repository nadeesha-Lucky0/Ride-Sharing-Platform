package com.ridelink.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ridelink.payment.dto.PaymentRefundRequestDto;
import com.ridelink.payment.dto.PaymentRequestDto;
import com.ridelink.payment.dto.PaymentResponseDto;
import com.ridelink.payment.dto.ReceiptDto;
import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/payments/process - Process payment and return 201 Created")
    void testProcessPayment_Success() throws Exception {
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .rideId("RIDE-5001")
                .passengerId("PASS-200")
                .driverId("DRV-300")
                .amount(250.0)
                .paymentMethod(PaymentMethod.WALLET)
                .build();

        ReceiptDto receiptDto = ReceiptDto.builder()
                .receiptNumber("REC-X1Y2Z3")
                .rideId("RIDE-5001")
                .paymentId("PAY-99")
                .passengerId("PASS-200")
                .driverId("DRV-300")
                .totalAmount(250.0)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.COMPLETED)
                .issuedAt(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(PaymentRequestDto.class))).thenReturn(receiptDto);

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receiptNumber").value("REC-X1Y2Z3"))
                .andExpect(jsonPath("$.totalAmount").value(250.0))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/payments/process - Validation failure when missing rideId and negative amount")
    void testProcessPayment_ValidationFailure() throws Exception {
        PaymentRequestDto invalidDto = PaymentRequestDto.builder()
                .amount(-50.0)
                .build();

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("GET /api/payments/receipt/{rideId} - Retrieve receipt")
    void testGetReceiptByRideId_Success() throws Exception {
        ReceiptDto receiptDto = ReceiptDto.builder()
                .receiptNumber("REC-8899")
                .rideId("RIDE-777")
                .totalAmount(180.0)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(paymentService.getReceiptByRideId("RIDE-777")).thenReturn(receiptDto);

        mockMvc.perform(get("/api/payments/receipt/RIDE-777"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value("REC-8899"))
                .andExpect(jsonPath("$.rideId").value("RIDE-777"));
    }

    @Test
    @DisplayName("POST /api/payments/{id}/refund - Refund payment successfully")
    void testRefundPayment_Success() throws Exception {
        PaymentRefundRequestDto refundDto = PaymentRefundRequestDto.builder()
                .reason("Driver canceled")
                .build();

        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .id("PAY-100")
                .rideId("RIDE-55")
                .amount(300.0)
                .status(PaymentStatus.REFUNDED)
                .refundReason("Driver canceled")
                .refundedAt(LocalDateTime.now())
                .build();

        when(paymentService.refundPayment(eq("PAY-100"), any(PaymentRefundRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/payments/PAY-100/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.refundReason").value("Driver canceled"));
    }
}
