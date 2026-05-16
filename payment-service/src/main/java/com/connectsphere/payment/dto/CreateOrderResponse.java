package com.connectsphere.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderResponse {
    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Integer amountInPaise;
    private String currency;
    private String paymentType;
    private String description;
}
