package com.connectsphere.payment.dto;

import com.connectsphere.payment.enums.PaymentStatus;
import com.connectsphere.payment.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long userId;
    private Double amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String transactionId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}