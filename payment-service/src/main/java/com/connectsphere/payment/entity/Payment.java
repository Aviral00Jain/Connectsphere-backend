package com.connectsphere.payment.entity;

import com.connectsphere.payment.enums.PaymentStatus;
import com.connectsphere.payment.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(unique = true)
    private String transactionId;

    // Razorpay fields
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}