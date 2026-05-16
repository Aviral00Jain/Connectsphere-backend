package com.connectsphere.payment.dto;

import com.connectsphere.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;
}
