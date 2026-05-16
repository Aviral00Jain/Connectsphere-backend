package com.connectsphere.payment.service;

import com.connectsphere.payment.dto.*;

import java.util.List;

public interface PaymentService {

    CreateOrderResponse createOrder(CreateOrderRequest request);

    PaymentResponse verifyPayment(VerifyPaymentRequest request);

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    List<PaymentResponse> getPaymentsByUserId(Long userId);

    List<PaymentResponse> getAllPayments();

    PaymentResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request);
}